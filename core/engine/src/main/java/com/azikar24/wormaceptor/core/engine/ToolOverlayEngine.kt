/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.azikar24.wormaceptor.core.engine.ui.ToolOverlayContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.ref.WeakReference

/**
 * Engine for displaying a floating toolbar overlay with View Borders and Measurement toggles.
 *
 * This engine uses a WindowManager overlay to display toggle buttons that persist across
 * activity navigation within the host app. The toolbar appears when either View Borders
 * or Measurement tool is enabled, and hides when both are disabled.
 *
 * Features:
 * - Combines state from ViewBordersEngine and MeasurementEngine
 * - Draggable floating toolbar with position persistence
 * - Auto-shows/hides based on tool states
 * - Cross-activity persistence using application context
 */
class ToolOverlayEngine(
    private val context: Context,
) : KoinComponent, LifecycleOwner, SavedStateRegistryOwner {

    // Injected engines via Koin
    private val viewBordersEngine: ViewBordersEngine by inject()
    private val measurementEngine: MeasurementEngine by inject()

    // WindowManager for overlay
    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var activityRef: WeakReference<Activity>? = null

    // Activity lifecycle observer - keeps overlay visible across activities
    // Following the same simple pattern as PerformanceOverlayEngine
    private var applicationRef: WeakReference<Application>? = null
    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {
            // Update activity reference to the currently resumed activity
            // This is needed for toggle callbacks to work on the current activity
            activityRef = WeakReference(activity)
        }
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }
    private var isLifecycleCallbacksRegistered = false

    // Lifecycle management for ComposeView
    private var lifecycleRegistry = LifecycleRegistry(this)
    private var savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    // Coroutine scope
    private var scope: CoroutineScope? = null

    // State
    private val _state = MutableStateFlow(ToolOverlayState.EMPTY)
    val state: StateFlow<ToolOverlayState> = _state.asStateFlow()

    private val _isVisible = MutableStateFlow(false)
    val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

    // SharedPreferences for position persistence
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Callbacks for overlay interactions.
     */
    data class OverlayCallbacks(
        val onDragStart: () -> Unit,
        val onDrag: (Offset) -> Unit,
        val onDragEnd: () -> Unit,
        val onToggleViewBorders: () -> Unit,
        val onToggleMeasurement: () -> Unit,
    )

    /**
     * Shows the toolbar overlay for the given activity.
     */
    fun show(activity: Activity) {
        if (_isVisible.value) {
            Log.d(TAG, "show() called but already visible")
            return
        }

        // Check permission before showing
        if (!canDrawOverlays(context)) {
            Log.w(TAG, "Cannot show overlay - SYSTEM_ALERT_WINDOW permission not granted")
            return
        }

        Log.d(TAG, "Showing tool overlay for activity: ${activity.javaClass.simpleName}")

        // Set visible early to prevent re-entrant calls from state observation
        _isVisible.value = true

        activityRef = WeakReference(activity)
        windowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        registerActivityLifecycleCallbacks(activity)

        // Recreate lifecycle components for fresh state
        lifecycleRegistry = LifecycleRegistry(this)
        savedStateRegistryController = SavedStateRegistryController.create(this)

        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        startStateObservation()
        createOverlayView(activity)

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        Log.d(TAG, "Tool overlay shown successfully")
    }

    /**
     * Clears activity references but keeps the overlay visible.
     * The overlay uses application context so it survives activity destruction.
     */
    fun clearActivityReferences() {
        activityRef = null
    }

    /**
     * Hides the toolbar overlay and cleans up resources.
     */
    fun hide() {
        if (!_isVisible.value) return

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        stopStateObservation()
        removeOverlayView()
        unregisterActivityLifecycleCallbacks()

        activityRef = null
        windowManager = null

        _isVisible.value = false
    }

    /**
     * Toggles the View Borders tool.
     */
    fun toggleViewBorders() {
        val activity = activityRef?.get() ?: return

        if (viewBordersEngine.isEnabled.value) {
            viewBordersEngine.disable()
        } else {
            viewBordersEngine.enable(activity)
        }
        // Don't call updateVisibility - toolbar stays visible for toggling
    }

    /**
     * Toggles the Measurement tool.
     */
    fun toggleMeasurement() {
        val activity = activityRef?.get() ?: return

        if (measurementEngine.isEnabled.value) {
            measurementEngine.disable()
        } else {
            measurementEngine.enable(activity)
        }
        // Don't call updateVisibility - toolbar stays visible for toggling
    }

    /**
     * Called when View Borders is enabled/disabled from its feature screen.
     * Shows the toolbar if not visible, but never hides it (user dismisses via drag).
     */
    fun onViewBordersStateChanged(enabled: Boolean, activity: Activity) {
        Log.d(TAG, "onViewBordersStateChanged: enabled=$enabled, activity=${activity.javaClass.simpleName}")
        activityRef = WeakReference(activity)
        _state.value = _state.value.copy(viewBordersEnabled = enabled)
        // Only show if enabling and not visible - never auto-hide
        if (enabled && !_isVisible.value) {
            show(activity)
        }
    }

    /**
     * Called when Measurement is enabled/disabled from its feature screen.
     * Shows the toolbar if not visible, but never hides it (user dismisses via drag).
     */
    fun onMeasurementStateChanged(enabled: Boolean, activity: Activity) {
        Log.d(TAG, "onMeasurementStateChanged: enabled=$enabled, activity=${activity.javaClass.simpleName}")
        activityRef = WeakReference(activity)
        _state.value = _state.value.copy(measurementEnabled = enabled)
        // Only show if enabling and not visible - never auto-hide
        if (enabled && !_isVisible.value) {
            show(activity)
        }
    }

    /**
     * Updates overlay visibility based on current tool states.
     */
    private fun updateVisibility() {
        val activity = activityRef?.get()
        if (activity == null) {
            Log.w(TAG, "updateVisibility: No activity reference")
            return
        }

        val shouldShow = _state.value.shouldShow()
        Log.d(TAG, "updateVisibility: shouldShow=$shouldShow, isVisible=${_isVisible.value}")

        if (shouldShow && !_isVisible.value) {
            show(activity)
        } else if (!shouldShow && _isVisible.value) {
            hide()
        }
    }

    /**
     * Updates the position of the overlay (as screen percentage).
     */
    fun updatePosition(positionPercent: Offset) {
        val clampedPosition = Offset(
            positionPercent.x.coerceIn(0f, 1f),
            positionPercent.y.coerceIn(0f, 1f),
        )
        _state.value = _state.value.copy(positionPercent = clampedPosition)
        updateWindowPosition()
    }

    /**
     * Saves the current position to SharedPreferences.
     */
    fun savePosition() {
        scope?.launch(Dispatchers.IO) {
            val position = _state.value.positionPercent
            prefs.edit()
                .putFloat(PREF_POSITION_X, position.x)
                .putFloat(PREF_POSITION_Y, position.y)
                .apply()
        }
    }

    private suspend fun loadSavedPosition(): Offset = withContext(Dispatchers.IO) {
        val x = prefs.getFloat(PREF_POSITION_X, ToolOverlayState.DEFAULT_POSITION_PERCENT.x)
        val y = prefs.getFloat(PREF_POSITION_Y, ToolOverlayState.DEFAULT_POSITION_PERCENT.y)
        Offset(x, y)
    }

    fun setDragging(isDragging: Boolean) {
        _state.value = _state.value.copy(isDragging = isDragging)
    }

    /**
     * Handles drag end - dismisses toolbar if in dismiss zone, otherwise saves position.
     */
    private fun handleDragEnd() {
        setDragging(false)

        if (_state.value.isInDismissZone()) {
            // User dropped in dismiss zone - hide toolbar and disable both tools
            Log.d(TAG, "Dropped in dismiss zone - hiding toolbar")
            viewBordersEngine.disable()
            measurementEngine.disable()
            hide()
        } else {
            savePosition()
        }
    }

    private fun startStateObservation() {
        scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        scope?.launch {
            val savedPosition = loadSavedPosition()
            _state.value = _state.value.copy(positionPercent = savedPosition)
            updateWindowPosition()
        }

        // Observe View Borders state
        // Note: We only update the state (for button appearance), but don't auto-hide
        // the toolbar. The toolbar should persist until both tools are manually disabled.
        scope?.launch {
            viewBordersEngine.isEnabled.collect { enabled ->
                _state.value = _state.value.copy(viewBordersEnabled = enabled)
            }
        }

        // Observe Measurement state
        // Note: We only update the state (for button appearance), but don't auto-hide
        // the toolbar. The toolbar should persist until both tools are manually disabled.
        scope?.launch {
            measurementEngine.isEnabled.collect { enabled ->
                _state.value = _state.value.copy(measurementEnabled = enabled)
            }
        }
    }

    private fun stopStateObservation() {
        scope?.cancel()
        scope = null
    }

    private fun createOverlayView(activity: Activity) {
        // Use application context so the overlay survives activity changes
        // This matches the pattern used by PerformanceOverlayEngine
        val composeView = ComposeView(context.applicationContext).apply {
            setViewTreeLifecycleOwner(this@ToolOverlayEngine)
            setViewTreeSavedStateRegistryOwner(this@ToolOverlayEngine)

            setContent {
                val currentState by state.collectAsState()

                val callbacks = OverlayCallbacks(
                    onDragStart = { setDragging(true) },
                    onDrag = { offset -> handleDrag(offset) },
                    onDragEnd = { handleDragEnd() },
                    onToggleViewBorders = { toggleViewBorders() },
                    onToggleMeasurement = { toggleMeasurement() },
                )

                ToolOverlayContent(
                    state = currentState,
                    callbacks = callbacks,
                )
            }
        }

        val displayMetrics = activity.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val savedPosition = _state.value.positionPercent
        val overlayWidthPx = (OVERLAY_WIDTH_DP * displayMetrics.density).toInt()
        val overlayHeightPx = (OVERLAY_HEIGHT_DP * displayMetrics.density).toInt()

        val initialX = (savedPosition.x * screenWidth).toInt() - overlayWidthPx
        val initialY = (savedPosition.y * screenHeight).toInt() - (overlayHeightPx / 2)

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val layoutParams = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = windowType
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
            x = initialX.coerceAtLeast(0)
            y = initialY.coerceAtLeast(0)
        }

        overlayView = composeView

        try {
            windowManager?.addView(composeView, layoutParams)
            Log.d(TAG, "Overlay view added successfully at position (${layoutParams.x}, ${layoutParams.y})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay view", e)
            overlayView = null
            _isVisible.value = false
        }
    }

    private fun removeOverlayView() {
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                // View might already be removed
            }
        }
        overlayView = null
    }

    private fun registerActivityLifecycleCallbacks(activity: Activity) {
        if (isLifecycleCallbacksRegistered) return

        val application = activity.application
        applicationRef = WeakReference(application)
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        isLifecycleCallbacksRegistered = true
    }

    private fun unregisterActivityLifecycleCallbacks() {
        if (!isLifecycleCallbacksRegistered) return

        applicationRef?.get()?.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
        applicationRef = null
        isLifecycleCallbacksRegistered = false
    }

    private fun handleDrag(deltaOffset: Offset) {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val deltaPercentX = deltaOffset.x / screenWidth
        val deltaPercentY = deltaOffset.y / screenHeight

        val currentPosition = _state.value.positionPercent
        val newPosition = Offset(
            (currentPosition.x + deltaPercentX).coerceIn(0f, 1f),
            (currentPosition.y + deltaPercentY).coerceIn(0f, 1f),
        )

        _state.value = _state.value.copy(positionPercent = newPosition)
        updateWindowPosition()
    }

    private fun updateWindowPosition() {
        val view = overlayView ?: return
        val wm = windowManager ?: return

        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val position = _state.value.positionPercent
        val overlayWidthPx = (OVERLAY_WIDTH_DP * displayMetrics.density).toInt()
        val overlayHeightPx = (OVERLAY_HEIGHT_DP * displayMetrics.density).toInt()

        val newX = (position.x * screenWidth).toInt() - overlayWidthPx
        val newY = (position.y * screenHeight).toInt() - (overlayHeightPx / 2)

        try {
            val params = view.layoutParams as WindowManager.LayoutParams
            params.x = newX.coerceIn(0, screenWidth - overlayWidthPx)
            params.y = newY.coerceIn(0, screenHeight - overlayHeightPx)
            wm.updateViewLayout(view, params)
        } catch (e: Exception) {
            // View might have been removed
        }
    }

    companion object {
        private const val TAG = "ToolOverlayEngine"
        private const val PREFS_NAME = "wormaceptor_tool_overlay_prefs"
        private const val PREF_POSITION_X = "tool_overlay_position_x"
        private const val PREF_POSITION_Y = "tool_overlay_position_y"

        // Toolbar dimensions
        private const val OVERLAY_WIDTH_DP = 56
        private const val OVERLAY_HEIGHT_DP = 120

        /**
         * Check if the app can draw overlays.
         */
        fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.provider.Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
    }
}

package com.azikar24.wormaceptor.core.engine

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.azikar24.wormaceptor.core.engine.ui.DismissZoneContent
import com.azikar24.wormaceptor.core.engine.ui.PerformanceOverlayContent
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import com.azikar24.wormaceptor.domain.entities.FpsInfo
import com.azikar24.wormaceptor.domain.entities.MemoryInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.ref.WeakReference

/**
 * Engine for displaying a floating performance overlay on top of the application.
 *
 * This engine uses a WindowManager overlay to display real-time performance metrics
 * including FPS, memory usage, and CPU usage. The overlay is draggable.
 *
 * Features:
 * - Combines data from FpsMonitorEngine, MemoryMonitorEngine, and CpuMonitorEngine
 * - Draggable floating badge with position persistence
 * - Position stored as percentage for rotation handling
 *
 */
@Suppress("TooManyFunctions")
class PerformanceOverlayEngine(
    private val context: Context,
) : KoinComponent, LifecycleOwner, SavedStateRegistryOwner {

    // Injected monitoring engines via Koin
    private val fpsMonitorEngine: FpsMonitorEngine by inject()
    private val memoryMonitorEngine: MemoryMonitorEngine by inject()
    private val cpuMonitorEngine: CpuMonitorEngine by inject()

    // WindowManager for overlay
    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var dismissZoneView: ComposeView? = null
    private var activityRef: WeakReference<Activity>? = null

    // Activity lifecycle observer - keeps overlay visible across activities
    // and hides overlay when host app goes to background
    private var applicationRef: WeakReference<Application>? = null
    private var startedActivityCount = 1
    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(
            activity: Activity,
            savedInstanceState: Bundle?,
        ) = Unit
        override fun onActivityStarted(activity: Activity) {
            startedActivityCount++
            if (startedActivityCount == 1) {
                overlayView?.visibility = View.VISIBLE
                dismissZoneView?.visibility = View.VISIBLE
            }
        }

        override fun onActivityResumed(activity: Activity) {
            // Update activity reference to the currently resumed activity
            activityRef = WeakReference(activity)
        }

        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) {
            startedActivityCount--
            if (startedActivityCount == 0) {
                overlayView?.visibility = View.GONE
                dismissZoneView?.visibility = View.GONE
            }
        }

        override fun onActivitySaveInstanceState(
            activity: Activity,
            outState: Bundle,
        ) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }
    private var isLifecycleCallbacksRegistered = false

    // Lifecycle management for ComposeView
    private var lifecycleRegistry = LifecycleRegistry(this)
    private var savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    // Coroutine scope for combining flows
    private var scope: CoroutineScope? = null

    // State
    private val _state = MutableStateFlow(PerformanceOverlayState.EMPTY)

    /** The current overlay state including position, metric values, and visibility. */
    val state: StateFlow<PerformanceOverlayState> = _state.asStateFlow()

    private val _isVisible = MutableStateFlow(false)

    // SharedPreferences for position persistence
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Composable content provider - set externally
    private var contentProvider: (@Composable (PerformanceOverlayState, OverlayCallbacks) -> Unit)? = null

    /**
     * Callbacks for overlay interactions.
     *
     * @property onDragStart Called when the user begins dragging the overlay.
     * @property onDrag Called during a drag gesture with the delta offset.
     * @property onDragEnd Called when the user releases the overlay.
     * @property onHideOverlay Called when the user requests to hide the overlay.
     * @property onRemoveFps Called when the user removes the FPS metric from the overlay.
     * @property onRemoveMemory Called when the user removes the memory metric from the overlay.
     * @property onRemoveCpu Called when the user removes the CPU metric from the overlay.
     */
    data class OverlayCallbacks(
        val onDragStart: () -> Unit,
        val onDrag: (Offset) -> Unit,
        val onDragEnd: () -> Unit,
        val onHideOverlay: () -> Unit,
        val onRemoveFps: () -> Unit,
        val onRemoveMemory: () -> Unit,
        val onRemoveCpu: () -> Unit,
    )

    /**
     * Shows the performance overlay for the given activity.
     *
     * @param activity The activity to attach the overlay to
     */
    fun show(activity: Activity) {
        if (_isVisible.value) return

        activityRef = WeakReference(activity)
        // Use application context for WindowManager so overlay survives activity changes
        windowManager = context.applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Register activity lifecycle callbacks to hide overlay when host activity is destroyed
        registerActivityLifecycleCallbacks(activity)

        // Recreate lifecycle components for fresh state
        lifecycleRegistry = LifecycleRegistry(this)
        savedStateRegistryController = SavedStateRegistryController.create(this)

        // Initialize lifecycle
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        // Start monitoring engines if not already running
        startMonitoringEngines()

        // Start collecting metrics
        startMetricsCollection()

        // Create and add overlay view
        createOverlayView(activity)

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        _isVisible.value = true
    }

    /**
     * Shows the overlay using the stored activity reference.
     * Should be called when activity is available.
     */
    fun show() {
        val activity = activityRef?.get()
        if (activity != null) {
            show(activity)
        }
    }

    /**
     * Hides the performance overlay and cleans up resources.
     */
    fun hide() {
        if (!_isVisible.value) return

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        // Stop metrics collection
        stopMetricsCollection()

        // Remove overlay view
        removeOverlayView()

        // Unregister activity lifecycle callbacks
        unregisterActivityLifecycleCallbacks()

        activityRef = null
        windowManager = null

        _isVisible.value = false
    }

    /**
     * Saves the current position to SharedPreferences.
     * Call this after drag operations complete.
     */
    fun savePosition() {
        val position = _state.value.positionPercent
        prefs.edit {
            putFloat(PREF_POSITION_X, position.x)
            putFloat(PREF_POSITION_Y, position.y)
        }
    }

    /**
     * Loads the saved position from SharedPreferences.
     * This is a suspend function to avoid blocking the main thread during disk I/O.
     */
    private suspend fun loadSavedPosition(): Offset = withContext(Dispatchers.IO) {
        val x = prefs.getFloat(PREF_POSITION_X, PerformanceOverlayState.DEFAULT_POSITION_PERCENT.x)
        val y = prefs.getFloat(PREF_POSITION_Y, PerformanceOverlayState.DEFAULT_POSITION_PERCENT.y)
        Offset(x, y)
    }

    /**
     * Sets the dragging state.
     */
    fun setDragging(isDragging: Boolean) {
        _state.value = _state.value.copy(isDragging = isDragging)
    }

    /**
     * Handles drag end - dismisses overlay if in dismiss zone, otherwise saves position.
     */
    private fun handleDragEnd() {
        setDragging(false)

        if (_state.value.isInDismissZone()) {
            // User dropped in dismiss zone - hide overlay and disable
            setOverlayEnabled(false)
        } else {
            savePosition()
        }
    }

    /**
     * Toggles the FPS metric on/off.
     * When enabled, starts the FPS monitor and shows the overlay.
     * When disabled, stops the FPS monitor if no other metrics need it.
     *
     * @param activity The activity to attach the overlay to (required when enabling first metric)
     */
    fun toggleFps(activity: Activity? = null) {
        val newEnabled = !_state.value.fpsEnabled
        _state.value = _state.value.copy(fpsEnabled = newEnabled)
        saveFpsEnabled(newEnabled)

        if (newEnabled) {
            if (!fpsMonitorEngine.isRunning.value) {
                fpsMonitorEngine.start()
            }
            ensureOverlayVisible(activity)
        } else {
            // Stop FPS monitor if not needed
            fpsMonitorEngine.stop()
            hideOverlayIfNoMetrics()
        }
    }

    /**
     * Toggles the Memory metric on/off.
     * When enabled, starts the Memory monitor and shows the overlay.
     * When disabled, stops the Memory monitor if no other metrics need it.
     *
     * @param activity The activity to attach the overlay to (required when enabling first metric)
     */
    fun toggleMemory(activity: Activity? = null) {
        val newEnabled = !_state.value.memoryEnabled
        _state.value = _state.value.copy(memoryEnabled = newEnabled)
        saveMemoryEnabled(newEnabled)

        if (newEnabled) {
            if (!memoryMonitorEngine.isMonitoring.value) {
                memoryMonitorEngine.start()
            }
            ensureOverlayVisible(activity)
        } else {
            // Stop Memory monitor if not needed
            memoryMonitorEngine.stop()
            hideOverlayIfNoMetrics()
        }
    }

    /**
     * Toggles the CPU metric on/off.
     * When enabled, starts the CPU monitor and shows the overlay.
     * When disabled, stops the CPU monitor if no other metrics need it.
     *
     * @param activity The activity to attach the overlay to (required when enabling first metric)
     */
    fun toggleCpu(activity: Activity? = null) {
        val newEnabled = !_state.value.cpuEnabled
        _state.value = _state.value.copy(cpuEnabled = newEnabled)
        saveCpuEnabled(newEnabled)

        if (newEnabled) {
            if (!cpuMonitorEngine.isMonitoring.value) {
                cpuMonitorEngine.start()
            }
            ensureOverlayVisible(activity)
        } else {
            // Stop CPU monitor if not needed
            cpuMonitorEngine.stop()
            hideOverlayIfNoMetrics()
        }
    }

    /**
     * Ensures the overlay is visible, showing it if not already visible.
     */
    private fun ensureOverlayVisible(activity: Activity? = null) {
        if (!_isVisible.value) {
            val targetActivity = activity ?: activityRef?.get()
            targetActivity?.let { show(it) }
        }
    }

    /**
     * Hides the overlay if no metrics are enabled.
     */
    private fun hideOverlayIfNoMetrics() {
        if (!_state.value.hasAnyMetricEnabled()) {
            hide()
        }
    }

    /**
     * Loads and applies saved metric enabled states from SharedPreferences.
     * The overlay master toggle is always reset to OFF on app restart since the overlay
     * window cannot survive process death. Individual metric toggles are preserved so
     * re-enabling the overlay restores the same configuration.
     */
    suspend fun loadSavedMetricStates() {
        // If the overlay is already visible, skip resetting - this avoids
        // toggling the state to OFF when recomposition triggers a re-load
        // (e.g. returning from background while the overlay is showing).
        if (_isVisible.value) return

        val (fpsEnabled, memoryEnabled, cpuEnabled) = withContext(Dispatchers.IO) {
            // Clear persisted overlay enabled state since it can't be restored without an Activity
            prefs.edit { putBoolean(PREF_OVERLAY_ENABLED, false) }

            MetricPrefsState(
                fpsEnabled = prefs.getBoolean(PREF_FPS_ENABLED, false),
                memoryEnabled = prefs.getBoolean(PREF_MEMORY_ENABLED, false),
                cpuEnabled = prefs.getBoolean(PREF_CPU_ENABLED, false),
            )
        }

        _state.value = _state.value.copy(
            isOverlayEnabled = false,
            fpsEnabled = fpsEnabled,
            memoryEnabled = memoryEnabled,
            cpuEnabled = cpuEnabled,
        )
    }

    private data class MetricPrefsState(
        val fpsEnabled: Boolean,
        val memoryEnabled: Boolean,
        val cpuEnabled: Boolean,
    )

    /**
     * Toggles the overlay with all metrics enabled/disabled together.
     * When enabling: shows overlay with all metrics (CPU, Memory, FPS) enabled.
     * When disabling: hides overlay and disables all metrics.
     *
     * @param activity The activity to attach the overlay to (required when enabling)
     */
    fun toggleOverlayWithAllMetrics(activity: Activity? = null) {
        val newEnabled = !_state.value.isOverlayEnabled

        if (newEnabled) {
            // Enable all metrics and show overlay
            _state.value = _state.value.copy(
                isOverlayEnabled = true,
                fpsEnabled = true,
                memoryEnabled = true,
                cpuEnabled = true,
            )
            saveOverlayEnabled(true)
            saveFpsEnabled(true)
            saveMemoryEnabled(true)
            saveCpuEnabled(true)

            // Start all monitoring engines
            if (!fpsMonitorEngine.isRunning.value) fpsMonitorEngine.start()
            if (!memoryMonitorEngine.isMonitoring.value) memoryMonitorEngine.start()
            if (!cpuMonitorEngine.isMonitoring.value) cpuMonitorEngine.start()

            val targetActivity = activity ?: activityRef?.get()
            targetActivity?.let { show(it) }
        } else {
            // Disable overlay and all metrics
            _state.value = _state.value.copy(
                isOverlayEnabled = false,
                fpsEnabled = false,
                memoryEnabled = false,
                cpuEnabled = false,
            )
            saveOverlayEnabled(false)
            saveFpsEnabled(false)
            saveMemoryEnabled(false)
            saveCpuEnabled(false)

            // Stop all monitoring engines
            fpsMonitorEngine.stop()
            memoryMonitorEngine.stop()
            cpuMonitorEngine.stop()

            hide()
        }
    }

    /**
     * Sets the overlay enabled state.
     */
    fun setOverlayEnabled(
        enabled: Boolean,
        activity: Activity? = null,
    ) {
        if (_state.value.isOverlayEnabled == enabled) return

        _state.value = _state.value.copy(isOverlayEnabled = enabled)
        saveOverlayEnabled(enabled)

        if (enabled) {
            startMonitoringEngines()
            val targetActivity = activity ?: activityRef?.get()
            targetActivity?.let { show(it) }
        } else {
            hide()
        }
    }

    /**
     * Enables a specific metric and shows it in the overlay.
     * This is called when navigating to a monitor screen (FPS, Memory, CPU).
     * If the overlay is enabled, the metric will be automatically enabled and shown.
     *
     * @param fps Enable FPS metric
     * @param memory Enable Memory metric
     * @param cpu Enable CPU metric
     */
    fun enableMetricForMonitorScreen(
        fps: Boolean = false,
        memory: Boolean = false,
        cpu: Boolean = false,
    ) {
        // Only enable metrics if the overlay master toggle is enabled
        if (!_state.value.isOverlayEnabled) return

        if (fps && !_state.value.fpsEnabled) {
            _state.value = _state.value.copy(fpsEnabled = true)
            saveFpsEnabled(true)
            if (!fpsMonitorEngine.isRunning.value) {
                fpsMonitorEngine.start()
            }
        }

        if (memory && !_state.value.memoryEnabled) {
            _state.value = _state.value.copy(memoryEnabled = true)
            saveMemoryEnabled(true)
            if (!memoryMonitorEngine.isMonitoring.value) {
                memoryMonitorEngine.start()
            }
        }

        if (cpu && !_state.value.cpuEnabled) {
            _state.value = _state.value.copy(cpuEnabled = true)
            saveCpuEnabled(true)
            if (!cpuMonitorEngine.isMonitoring.value) {
                cpuMonitorEngine.start()
            }
        }
    }

    private fun saveOverlayEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(PREF_OVERLAY_ENABLED, enabled) }
    }

    private fun saveFpsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(PREF_FPS_ENABLED, enabled) }
    }

    private fun saveMemoryEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(PREF_MEMORY_ENABLED, enabled) }
    }

    private fun saveCpuEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(PREF_CPU_ENABLED, enabled) }
    }

    private fun startMonitoringEngines() {
        // Only start engines for enabled metrics
        if (_state.value.fpsEnabled && !fpsMonitorEngine.isRunning.value) {
            fpsMonitorEngine.start()
        }
        if (_state.value.memoryEnabled && !memoryMonitorEngine.isMonitoring.value) {
            memoryMonitorEngine.start()
        }
        if (_state.value.cpuEnabled && !cpuMonitorEngine.isMonitoring.value) {
            cpuMonitorEngine.start()
        }
    }

    private fun startMetricsCollection() {
        scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        // Combine all metric flows into unified state
        scope?.launch {
            // Load saved position on IO thread
            val savedPosition = loadSavedPosition()
            _state.value = _state.value.copy(positionPercent = savedPosition)
            // Update window position after loading saved position
            updateWindowPosition()

            combine(
                fpsMonitorEngine.currentFpsInfo,
                fpsMonitorEngine.isRunning,
                memoryMonitorEngine.currentMemory,
                memoryMonitorEngine.isMonitoring,
                cpuMonitorEngine.currentCpu,
                cpuMonitorEngine.isMonitoring,
            ) { values ->
                val fpsInfo = values[0] as FpsInfo
                val fpsRunning = values[1] as Boolean
                val memoryInfo = values[2] as MemoryInfo
                val memoryRunning = values[3] as Boolean
                val cpuInfo = values[4] as CpuInfo
                val cpuRunning = values[5] as Boolean

                val currentState = _state.value

                currentState.copy(
                    fpsValue = fpsInfo.currentFps.toInt(),
                    fpsMonitorRunning = fpsRunning,
                    memoryPercent = memoryInfo.heapUsagePercent.toInt(),
                    memoryMonitorRunning = memoryRunning,
                    cpuPercent = cpuInfo.overallUsagePercent.toInt(),
                    cpuMonitorRunning = cpuRunning,
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    private fun stopMetricsCollection() {
        scope?.cancel()
        scope = null
    }

    private fun createOverlayView(activity: Activity) {
        val windowType = getOverlayWindowType()

        // Create dismiss zone view first (renders below the pill in z-order)
        createDismissZoneView(windowType)

        // Use application context so the overlay survives activity changes
        val composeView = ComposeView(context.applicationContext).apply {
            setViewTreeLifecycleOwner(this@PerformanceOverlayEngine)
            setViewTreeSavedStateRegistryOwner(this@PerformanceOverlayEngine)

            setContent {
                val currentState by state.collectAsState()

                val callbacks = OverlayCallbacks(
                    onDragStart = { setDragging(true) },
                    onDrag = { offset -> handleDrag(offset) },
                    onDragEnd = { handleDragEnd() },
                    onHideOverlay = { setOverlayEnabled(false) },
                    onRemoveFps = { toggleFps() },
                    onRemoveMemory = { toggleMemory() },
                    onRemoveCpu = { toggleCpu() },
                )

                contentProvider?.invoke(currentState, callbacks)
                    ?: DefaultOverlayContent(currentState, callbacks)
            }
        }

        val layoutParams = createOverlayLayoutParams(activity, windowType)
        overlayView = composeView

        try {
            windowManager?.addView(composeView, layoutParams)
            // Re-calculate position after layout with actual measured width
            composeView.post { updateWindowPosition() }
        } catch (_: Exception) {
            // Failed to add overlay view - likely missing permission
            overlayView = null
        }
    }

    private fun getOverlayWindowType(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
        @Suppress("DEPRECATION")
        WindowManager.LayoutParams.TYPE_PHONE
    }

    private fun createOverlayLayoutParams(
        activity: Activity,
        windowType: Int,
    ): WindowManager.LayoutParams {
        val displayMetrics = activity.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val savedPosition = _state.value.positionPercent
        val overlayWidthPx = getEstimatedOverlayWidthPx()

        val initialX = ((savedPosition.x * screenWidth).toInt() - overlayWidthPx / 2)
            .coerceIn(0, (screenWidth - overlayWidthPx).coerceAtLeast(0))
        val initialY = (savedPosition.y * screenHeight).toInt().coerceAtLeast(0)

        return WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = windowType
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
            x = initialX
            y = initialY
        }
    }

    private fun createDismissZoneView(windowType: Int) {
        val dismissView = ComposeView(context.applicationContext).apply {
            setViewTreeLifecycleOwner(this@PerformanceOverlayEngine)
            setViewTreeSavedStateRegistryOwner(this@PerformanceOverlayEngine)

            setContent {
                val currentState by state.collectAsState()
                DismissZoneContent(
                    isDragging = currentState.isDragging,
                    isInDismissZone = currentState.isDragging && currentState.isInDismissZone(),
                )
            }
        }

        val layoutParams = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = windowType
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            format = PixelFormat.TRANSLUCENT
        }

        dismissZoneView = dismissView

        try {
            windowManager?.addView(dismissView, layoutParams)
        } catch (_: Exception) {
            dismissZoneView = null
        }
    }

    private fun removeOverlayView() {
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {
                // View might already be removed
            }
        }
        overlayView = null

        dismissZoneView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {
                // View might already be removed
            }
        }
        dismissZoneView = null
    }

    /**
     * Registers activity lifecycle callbacks to observe when the host activity is destroyed.
     */
    private fun registerActivityLifecycleCallbacks(activity: Activity) {
        if (isLifecycleCallbacksRegistered) return

        // The calling activity is already started, so seed the counter at 1.
        startedActivityCount = 1

        val application = activity.application
        applicationRef = WeakReference(application)
        val oldPolicy = StrictMode.allowThreadDiskReads()
        try {
            application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
        isLifecycleCallbacksRegistered = true
    }

    /**
     * Unregisters activity lifecycle callbacks.
     */
    private fun unregisterActivityLifecycleCallbacks() {
        if (!isLifecycleCallbacksRegistered) return

        applicationRef?.get()?.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
        applicationRef = null
        isLifecycleCallbacksRegistered = false
    }

    /**
     * Clears all activity references. Call this from Activity.onDestroy() if the
     * overlay should be hidden when the activity is destroyed.
     */
    /**
     * Clears activity references but keeps the overlay visible.
     * The overlay uses application context so it survives activity destruction.
     */
    fun clearActivityReferences() {
        // Don't hide - overlay persists across activities using application context
        // Just clear the weak reference to allow the activity to be garbage collected
        activityRef = null
    }

    private fun handleDrag(deltaOffset: Offset) {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Convert delta to percentage
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
        val overlayWidthPx = getOverlayWidthPx()

        // Always center-anchor: positionPercent represents the overlay center.
        // coerceIn handles edge clamping so the pill stays fully on screen.
        val newX = ((position.x * screenWidth).toInt() - overlayWidthPx / 2)
            .coerceIn(0, (screenWidth - overlayWidthPx).coerceAtLeast(0))
        val newY = (position.y * screenHeight).toInt().coerceAtLeast(0)

        try {
            val params = view.layoutParams as WindowManager.LayoutParams
            params.x = newX
            params.y = newY
            wm.updateViewLayout(view, params)
        } catch (_: Exception) {
            // View might have been removed
        }
    }

    /**
     * Returns the overlay width in pixels, using the actual measured width when available.
     */
    private fun getOverlayWidthPx(): Int {
        val measuredWidth = overlayView?.width?.takeIf { it > 0 }
        if (measuredWidth != null) return measuredWidth
        return getEstimatedOverlayWidthPx()
    }

    /**
     * Estimates the overlay width based on the number of enabled metrics.
     */
    private fun getEstimatedOverlayWidthPx(): Int {
        val enabledCount = listOf(
            _state.value.fpsEnabled,
            _state.value.memoryEnabled,
            _state.value.cpuEnabled,
        ).count { it }

        val density = context.resources.displayMetrics.density
        val contentDp = ESTIMATED_METRIC_WIDTH_DP * enabledCount +
            METRIC_SPACING_DP * (enabledCount - 1).coerceAtLeast(0) +
            PILL_PADDING_HORIZONTAL_DP * 2
        return (contentDp * density).toInt()
    }

    /**
     * Default overlay content using PerformanceOverlayContent composable.
     */
    @Composable
    private fun DefaultOverlayContent(
        state: PerformanceOverlayState,
        callbacks: OverlayCallbacks,
    ) {
        PerformanceOverlayContent(
            state = state,
            callbacks = callbacks,
        )
    }

    /** SharedPreferences keys and overlay dimension constants. */
    companion object {
        // SharedPreferences keys
        private const val PREFS_NAME = "wormaceptor_performance_overlay_prefs"
        private const val PREF_POSITION_X = "overlay_position_x"
        private const val PREF_POSITION_Y = "overlay_position_y"
        private const val PREF_OVERLAY_ENABLED = "overlay_master_enabled"
        private const val PREF_FPS_ENABLED = "fps_enabled"
        private const val PREF_MEMORY_ENABLED = "memory_enabled"
        private const val PREF_CPU_ENABLED = "cpu_enabled"

        // Overlay width estimation constants (dp)
        private const val ESTIMATED_METRIC_WIDTH_DP = 70
        private const val METRIC_SPACING_DP = 12
        private const val PILL_PADDING_HORIZONTAL_DP = 12
    }
}

/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.azikar24.wormaceptor.core.engine.ui.PerformanceOverlayContent
import java.lang.ref.WeakReference

/**
 * Engine for displaying a floating performance overlay on top of the application.
 *
 * This engine uses a WindowManager overlay to display real-time performance metrics
 * including FPS, memory usage, and CPU usage. The overlay is draggable and can be
 * expanded to show sparkline charts.
 *
 * Features:
 * - Combines data from FpsMonitorEngine, MemoryMonitorEngine, and CpuMonitorEngine
 * - Draggable floating badge with position persistence
 * - Expandable view with sparkline charts
 * - Deep links to detailed performance screens
 * - Position stored as percentage for rotation handling
 *
 * Usage:
 * ```kotlin
 * val engine = PerformanceOverlayEngine(context)
 * engine.show(activity)
 * engine.hide()
 * ```
 */
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
    private var activityRef: WeakReference<Activity>? = null

    // Lifecycle management for ComposeView
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    // Coroutine scope for combining flows
    private var scope: CoroutineScope? = null

    // State
    private val _state = MutableStateFlow(PerformanceOverlayState.EMPTY)
    val state: StateFlow<PerformanceOverlayState> = _state.asStateFlow()

    private val _isVisible = MutableStateFlow(false)
    val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

    // SharedPreferences for position persistence
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Composable content provider - set externally
    private var contentProvider: (@Composable (PerformanceOverlayState, OverlayCallbacks) -> Unit)? = null

    /**
     * Callbacks for overlay interactions.
     */
    data class OverlayCallbacks(
        val onToggleExpanded: () -> Unit,
        val onDragStart: () -> Unit,
        val onDrag: (Offset) -> Unit,
        val onDragEnd: () -> Unit,
        val onOpenWormaCeptor: () -> Unit,
        val onOpenFpsDetail: () -> Unit,
        val onOpenMemoryDetail: () -> Unit,
        val onOpenCpuDetail: () -> Unit,
    )

    /**
     * Sets the Composable content provider for the overlay.
     * This should be called before show() to customize the overlay appearance.
     *
     * @param provider Composable function that renders the overlay content
     */
    fun setContentProvider(provider: @Composable (PerformanceOverlayState, OverlayCallbacks) -> Unit) {
        contentProvider = provider
    }

    /**
     * Shows the performance overlay for the given activity.
     *
     * @param activity The activity to attach the overlay to
     */
    fun show(activity: Activity) {
        if (_isVisible.value) return

        activityRef = WeakReference(activity)
        windowManager = activity.getSystemService(Activity.WINDOW_SERVICE) as WindowManager

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

        activityRef = null
        windowManager = null

        _isVisible.value = false
    }

    /**
     * Toggles the expanded/collapsed state of the overlay.
     */
    fun toggleExpanded() {
        _state.value = _state.value.copy(isExpanded = !_state.value.isExpanded)
    }

    /**
     * Updates the position of the overlay (as screen percentage).
     *
     * @param positionPercent New position as percentage (0.0-1.0) of screen dimensions
     */
    fun updatePosition(positionPercent: Offset) {
        val clampedPosition = Offset(
            positionPercent.x.coerceIn(0f, 1f),
            positionPercent.y.coerceIn(0f, 1f),
        )
        _state.value = _state.value.copy(positionPercent = clampedPosition)

        // Update window position
        updateWindowPosition()
    }

    /**
     * Saves the current position to SharedPreferences.
     * Call this after drag operations complete.
     */
    fun savePosition() {
        val position = _state.value.positionPercent
        prefs.edit()
            .putFloat(PREF_POSITION_X, position.x)
            .putFloat(PREF_POSITION_Y, position.y)
            .apply()
    }

    /**
     * Loads the saved position from SharedPreferences.
     */
    private fun loadSavedPosition(): Offset {
        val x = prefs.getFloat(PREF_POSITION_X, PerformanceOverlayState.DEFAULT_POSITION_PERCENT.x)
        val y = prefs.getFloat(PREF_POSITION_Y, PerformanceOverlayState.DEFAULT_POSITION_PERCENT.y)
        return Offset(x, y)
    }

    /**
     * Sets the dragging state.
     */
    fun setDragging(isDragging: Boolean) {
        _state.value = _state.value.copy(isDragging = isDragging)
    }

    private fun startMonitoringEngines() {
        if (!fpsMonitorEngine.isRunning.value) {
            fpsMonitorEngine.start()
        }
        if (!memoryMonitorEngine.isMonitoring.value) {
            memoryMonitorEngine.start()
        }
        if (!cpuMonitorEngine.isMonitoring.value) {
            cpuMonitorEngine.start()
        }
    }

    private fun startMetricsCollection() {
        scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        // Load saved position
        val savedPosition = loadSavedPosition()
        _state.value = _state.value.copy(positionPercent = savedPosition)

        // Combine all metric flows into unified state
        scope?.launch {
            combine(
                fpsMonitorEngine.currentFpsInfo,
                fpsMonitorEngine.fpsHistory,
                memoryMonitorEngine.currentMemory,
                memoryMonitorEngine.memoryHistory,
                cpuMonitorEngine.currentCpu,
                cpuMonitorEngine.cpuHistory,
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val fpsInfo = values[0] as com.azikar24.wormaceptor.domain.entities.FpsInfo
                @Suppress("UNCHECKED_CAST")
                val fpsHistory = values[1] as List<com.azikar24.wormaceptor.domain.entities.FpsInfo>
                @Suppress("UNCHECKED_CAST")
                val memoryInfo = values[2] as com.azikar24.wormaceptor.domain.entities.MemoryInfo
                @Suppress("UNCHECKED_CAST")
                val memoryHistory = values[3] as List<com.azikar24.wormaceptor.domain.entities.MemoryInfo>
                @Suppress("UNCHECKED_CAST")
                val cpuInfo = values[4] as com.azikar24.wormaceptor.domain.entities.CpuInfo
                @Suppress("UNCHECKED_CAST")
                val cpuHistory = values[5] as List<com.azikar24.wormaceptor.domain.entities.CpuInfo>

                val currentState = _state.value

                currentState.copy(
                    fpsValue = fpsInfo.currentFps.toInt(),
                    fpsHistory = fpsHistory
                        .takeLast(PerformanceOverlayState.HISTORY_SIZE)
                        .map { it.currentFps },
                    memoryPercent = memoryInfo.heapUsagePercent.toInt(),
                    memoryHistory = memoryHistory
                        .takeLast(PerformanceOverlayState.HISTORY_SIZE)
                        .map { it.heapUsagePercent },
                    cpuPercent = cpuInfo.overallUsagePercent.toInt(),
                    cpuHistory = cpuHistory
                        .takeLast(PerformanceOverlayState.HISTORY_SIZE)
                        .map { it.overallUsagePercent },
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
        val composeView = ComposeView(activity).apply {
            setViewTreeLifecycleOwner(this@PerformanceOverlayEngine)
            setViewTreeSavedStateRegistryOwner(this@PerformanceOverlayEngine)

            setContent {
                val currentState by state.collectAsState()

                val callbacks = OverlayCallbacks(
                    onToggleExpanded = { toggleExpanded() },
                    onDragStart = { setDragging(true) },
                    onDrag = { offset -> handleDrag(offset) },
                    onDragEnd = {
                        setDragging(false)
                        savePosition()
                    },
                    onOpenWormaCeptor = { openWormaCeptor() },
                    onOpenFpsDetail = { openFpsDetail() },
                    onOpenMemoryDetail = { openMemoryDetail() },
                    onOpenCpuDetail = { openCpuDetail() },
                )

                contentProvider?.invoke(currentState, callbacks)
                    ?: DefaultOverlayContent(currentState, callbacks)
            }
        }

        // Calculate initial position in pixels
        val displayMetrics = activity.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val savedPosition = loadSavedPosition()
        val initialX = (savedPosition.x * screenWidth).toInt() - (OVERLAY_WIDTH_DP * displayMetrics.density / 2).toInt()
        val initialY = (savedPosition.y * screenHeight).toInt()

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
        } catch (e: Exception) {
            // Failed to add overlay view - likely missing permission
            overlayView = null
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

    private fun handleDrag(deltaOffset: Offset) {
        val activity = activityRef?.get() ?: return
        val displayMetrics = activity.resources.displayMetrics
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
        val activity = activityRef?.get() ?: return

        val displayMetrics = activity.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val position = _state.value.positionPercent
        val newX = (position.x * screenWidth).toInt() - (OVERLAY_WIDTH_DP * displayMetrics.density / 2).toInt()
        val newY = (position.y * screenHeight).toInt()

        try {
            val params = view.layoutParams as WindowManager.LayoutParams
            params.x = newX.coerceAtLeast(0)
            params.y = newY.coerceAtLeast(0)
            wm.updateViewLayout(view, params)
        } catch (e: Exception) {
            // View might have been removed
        }
    }

    // Deep link handlers to open specific screens
    private fun openWormaCeptor() {
        val intent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("wormaceptor://tools"),
        ).apply {
            setPackage(context.packageName)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun openFpsDetail() {
        val intent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("wormaceptor://tools/fps"),
        ).apply {
            setPackage(context.packageName)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun openMemoryDetail() {
        val intent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("wormaceptor://tools/memory"),
        ).apply {
            setPackage(context.packageName)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun openCpuDetail() {
        val intent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("wormaceptor://tools/cpu"),
        ).apply {
            setPackage(context.packageName)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Default overlay content using PerformanceOverlayContent composable.
     */
    @Composable
    private fun DefaultOverlayContent(state: PerformanceOverlayState, callbacks: OverlayCallbacks) {
        PerformanceOverlayContent(
            state = state,
            callbacks = callbacks,
        )
    }

    companion object {
        private const val TAG = "PerformanceOverlayEngine"

        // SharedPreferences keys
        private const val PREFS_NAME = "wormaceptor_performance_overlay_prefs"
        private const val PREF_POSITION_X = "overlay_position_x"
        private const val PREF_POSITION_Y = "overlay_position_y"
        private const val PREF_ENABLED = "overlay_enabled"

        // Overlay dimensions
        private const val OVERLAY_WIDTH_DP = 120
        private const val OVERLAY_HEIGHT_COLLAPSED_DP = 40
        private const val OVERLAY_HEIGHT_EXPANDED_DP = 180

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

package com.azikar24.wormaceptor.core.engine

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
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
import com.azikar24.wormaceptor.core.engine.ui.PerformanceOverlayContent
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
    private var activityRef: WeakReference<Activity>? = null

    // Activity lifecycle observer - keeps overlay visible across activities
    private var applicationRef: WeakReference<Application>? = null
    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityResumed(activity: Activity) {
            // Update activity reference to the currently resumed activity
            activityRef = WeakReference(activity)
        }
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
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
        val onDragStart: () -> Unit,
        val onDrag: (Offset) -> Unit,
        val onDragEnd: () -> Unit,
        val onHideOverlay: () -> Unit,
        val onOpenWormaCeptor: () -> Unit,
        val onOpenFpsDetail: () -> Unit,
        val onOpenMemoryDetail: () -> Unit,
        val onOpenCpuDetail: () -> Unit,
        val onRemoveFps: () -> Unit,
        val onRemoveMemory: () -> Unit,
        val onRemoveCpu: () -> Unit,
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
     * Returns whether FPS metric is enabled.
     */
    fun isFpsEnabled(): Boolean = _state.value.fpsEnabled

    /**
     * Returns whether Memory metric is enabled.
     */
    fun isMemoryEnabled(): Boolean = _state.value.memoryEnabled

    /**
     * Returns whether CPU metric is enabled.
     */
    fun isCpuEnabled(): Boolean = _state.value.cpuEnabled

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
     * Sets individual metric enabled states.
     * This is useful when restoring state from preferences.
     */
    fun setMetricEnabled(fps: Boolean? = null, memory: Boolean? = null, cpu: Boolean? = null) {
        _state.value = _state.value.copy(
            fpsEnabled = fps ?: _state.value.fpsEnabled,
            memoryEnabled = memory ?: _state.value.memoryEnabled,
            cpuEnabled = cpu ?: _state.value.cpuEnabled,
        )
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
     * This is a suspend function to avoid blocking the main thread during disk I/O.
     */
    suspend fun loadSavedMetricStates() {
        val (overlayEnabled, fpsEnabled, memoryEnabled, cpuEnabled) = withContext(Dispatchers.IO) {
            PrefsState(
                overlayEnabled = prefs.getBoolean(PREF_OVERLAY_ENABLED, false),
                fpsEnabled = prefs.getBoolean(PREF_FPS_ENABLED, false),
                memoryEnabled = prefs.getBoolean(PREF_MEMORY_ENABLED, false),
                cpuEnabled = prefs.getBoolean(PREF_CPU_ENABLED, false),
            )
        }

        _state.value = _state.value.copy(
            isOverlayEnabled = overlayEnabled,
            fpsEnabled = fpsEnabled,
            memoryEnabled = memoryEnabled,
            cpuEnabled = cpuEnabled,
        )
    }

    private data class PrefsState(
        val overlayEnabled: Boolean,
        val fpsEnabled: Boolean,
        val memoryEnabled: Boolean,
        val cpuEnabled: Boolean,
    )

    /**
     * Toggles the master overlay enabled state.
     * When enabled, shows the overlay with any enabled metrics.
     * When disabled, hides the overlay but preserves metric states.
     *
     * @param activity The activity to attach the overlay to (required when enabling)
     */
    fun toggleOverlay(activity: Activity? = null) {
        val newEnabled = !_state.value.isOverlayEnabled
        _state.value = _state.value.copy(isOverlayEnabled = newEnabled)
        saveOverlayEnabled(newEnabled)

        if (newEnabled) {
            // Start any enabled monitoring engines
            startMonitoringEngines()
            val targetActivity = activity ?: activityRef?.get()
            targetActivity?.let { show(it) }
        } else {
            hide()
        }
    }

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
    fun setOverlayEnabled(enabled: Boolean, activity: Activity? = null) {
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
     * Returns whether the overlay is enabled (master toggle).
     */
    fun isOverlayEnabled(): Boolean = _state.value.isOverlayEnabled

    /**
     * Enables a specific metric and shows it in the overlay.
     * This is called when navigating to a monitor screen (FPS, Memory, CPU).
     * If the overlay is enabled, the metric will be automatically enabled and shown.
     *
     * @param fps Enable FPS metric
     * @param memory Enable Memory metric
     * @param cpu Enable CPU metric
     */
    fun enableMetricForMonitorScreen(fps: Boolean = false, memory: Boolean = false, cpu: Boolean = false) {
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
        prefs.edit().putBoolean(PREF_OVERLAY_ENABLED, enabled).apply()
    }

    private fun saveFpsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_FPS_ENABLED, enabled).apply()
    }

    private fun saveMemoryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_MEMORY_ENABLED, enabled).apply()
    }

    private fun saveCpuEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_CPU_ENABLED, enabled).apply()
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
                fpsMonitorEngine.fpsHistory,
                fpsMonitorEngine.isRunning,
                memoryMonitorEngine.currentMemory,
                memoryMonitorEngine.memoryHistory,
                memoryMonitorEngine.isMonitoring,
                cpuMonitorEngine.currentCpu,
                cpuMonitorEngine.cpuHistory,
                cpuMonitorEngine.isMonitoring,
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val fpsInfo = values[0] as com.azikar24.wormaceptor.domain.entities.FpsInfo

                @Suppress("UNCHECKED_CAST")
                val fpsHistory = values[1] as List<com.azikar24.wormaceptor.domain.entities.FpsInfo>

                val fpsRunning = values[2] as Boolean

                @Suppress("UNCHECKED_CAST")
                val memoryInfo = values[3] as com.azikar24.wormaceptor.domain.entities.MemoryInfo

                @Suppress("UNCHECKED_CAST")
                val memoryHistory = values[4] as List<com.azikar24.wormaceptor.domain.entities.MemoryInfo>

                val memoryRunning = values[5] as Boolean

                @Suppress("UNCHECKED_CAST")
                val cpuInfo = values[6] as com.azikar24.wormaceptor.domain.entities.CpuInfo

                @Suppress("UNCHECKED_CAST")
                val cpuHistory = values[7] as List<com.azikar24.wormaceptor.domain.entities.CpuInfo>

                val cpuRunning = values[8] as Boolean

                val currentState = _state.value

                currentState.copy(
                    fpsValue = fpsInfo.currentFps.toInt(),
                    fpsHistory = fpsHistory
                        .takeLast(PerformanceOverlayState.HISTORY_SIZE)
                        .map { it.currentFps },
                    fpsMonitorRunning = fpsRunning,
                    memoryPercent = memoryInfo.heapUsagePercent.toInt(),
                    memoryHistory = memoryHistory
                        .takeLast(PerformanceOverlayState.HISTORY_SIZE)
                        .map { it.heapUsagePercent },
                    memoryMonitorRunning = memoryRunning,
                    cpuPercent = cpuInfo.overallUsagePercent.toInt(),
                    cpuHistory = cpuHistory
                        .takeLast(PerformanceOverlayState.HISTORY_SIZE)
                        .map { it.overallUsagePercent },
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
                    onOpenWormaCeptor = { openWormaCeptor() },
                    onOpenFpsDetail = { openFpsDetail() },
                    onOpenMemoryDetail = { openMemoryDetail() },
                    onOpenCpuDetail = { openCpuDetail() },
                    onRemoveFps = { toggleFps() },
                    onRemoveMemory = { toggleMemory() },
                    onRemoveCpu = { toggleCpu() },
                )

                contentProvider?.invoke(currentState, callbacks)
                    ?: DefaultOverlayContent(currentState, callbacks)
            }
        }

        // Calculate initial position in pixels
        // Use position from state (may be default, will update when loadSavedPosition completes)
        val displayMetrics = activity.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val savedPosition = _state.value.positionPercent
        val overlayWidthPx = (OVERLAY_WIDTH_DP * displayMetrics.density).toInt()

        // Determine position direction based on saved position
        val direction = getPositionDirection(savedPosition.x)

        val initialX = when (direction) {
            PositionDirection.LEFT -> (savedPosition.x * screenWidth).toInt() - overlayWidthPx
            PositionDirection.RIGHT -> (savedPosition.x * screenWidth).toInt()
            PositionDirection.CENTER -> (savedPosition.x * screenWidth).toInt() - overlayWidthPx / 2
        }.coerceIn(0, screenWidth - overlayWidthPx)
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

    /**
     * Registers activity lifecycle callbacks to observe when the host activity is destroyed.
     */
    private fun registerActivityLifecycleCallbacks(activity: Activity) {
        if (isLifecycleCallbacksRegistered) return

        val application = activity.application
        applicationRef = WeakReference(application)
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
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
        val overlayWidthPx = (OVERLAY_WIDTH_DP * displayMetrics.density).toInt()

        // Calculate X based on position to prevent clipping
        val direction = getPositionDirection(position.x)
        val newX = when (direction) {
            PositionDirection.LEFT -> {
                // Near right edge: anchor content to right
                (position.x * screenWidth).toInt() - overlayWidthPx
            }
            PositionDirection.RIGHT -> {
                // Near left edge: anchor content to left
                (position.x * screenWidth).toInt()
            }
            PositionDirection.CENTER -> {
                // Center: anchor at center
                (position.x * screenWidth).toInt() - overlayWidthPx / 2
            }
        }
        val newY = (position.y * screenHeight).toInt()

        try {
            val params = view.layoutParams as WindowManager.LayoutParams
            params.x = newX.coerceIn(0, screenWidth - overlayWidthPx)
            params.y = newY.coerceAtLeast(0)
            wm.updateViewLayout(view, params)
        } catch (e: Exception) {
            // View might have been removed
        }
    }

    private fun getPositionDirection(xPercent: Float): PositionDirection = when {
        xPercent > 0.7f -> PositionDirection.LEFT
        xPercent < 0.3f -> PositionDirection.RIGHT
        else -> PositionDirection.CENTER
    }

    /**
     * Direction for positioning the overlay to avoid screen clipping.
     */
    private enum class PositionDirection {
        LEFT,
        RIGHT,
        CENTER,
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
        // SharedPreferences keys
        private const val PREFS_NAME = "wormaceptor_performance_overlay_prefs"
        private const val PREF_POSITION_X = "overlay_position_x"
        private const val PREF_POSITION_Y = "overlay_position_y"
        private const val PREF_OVERLAY_ENABLED = "overlay_master_enabled"
        private const val PREF_FPS_ENABLED = "fps_enabled"
        private const val PREF_MEMORY_ENABLED = "memory_enabled"
        private const val PREF_CPU_ENABLED = "cpu_enabled"

        // Overlay dimensions
        private const val OVERLAY_WIDTH_DP = 120

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

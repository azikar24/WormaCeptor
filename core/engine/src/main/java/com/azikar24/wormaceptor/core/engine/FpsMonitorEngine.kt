package com.azikar24.wormaceptor.core.engine

import android.view.Choreographer
import com.azikar24.wormaceptor.domain.entities.FpsInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.max
import kotlin.math.min

/**
 * Engine that monitors frame rate using Choreographer.FrameCallback.
 *
 * Uses the system Choreographer to measure frame delivery times and calculates
 * FPS metrics including current FPS, average, min, max, and dropped frames.
 *
 * A frame is considered "dropped" if it takes longer than 1.5x the display refresh period
 * (read from [refreshRateProvider] on start, falling back to 60Hz).
 * A frame is considered "jank" if it takes longer than 32ms.
 * Gaps longer than 500ms (app backgrounded, callbacks paused) are skipped, not counted.
 */
class FpsMonitorEngine @JvmOverloads constructor(
    private val historySize: Int = DEFAULT_HISTORY_SIZE,
    private val refreshRateProvider: () -> Float = { DEFAULT_REFRESH_RATE_HZ },
) {
    private var isMonitoring = false
    private var lastFrameTimeNanos = 0L
    private var refreshPeriodNanos = framePeriodNanos(DEFAULT_REFRESH_RATE_HZ)

    // Frame time tracking for FPS calculation
    private val frameTimesNanos = CopyOnWriteArrayList<Long>()
    private val fpsWindow = mutableListOf<Float>()

    // Statistics
    private var totalDroppedFrames = 0
    private var totalJankFrames = 0
    private var minFpsRecorded = Float.MAX_VALUE
    private var maxFpsRecorded = 0f
    private var fpsSum = 0f
    private var fpsCount = 0

    private val _currentFpsInfo = MutableStateFlow(FpsInfo.EMPTY)

    /** The most recent FPS measurement including min, max, average, and dropped frames. */
    val currentFpsInfo: StateFlow<FpsInfo> = _currentFpsInfo.asStateFlow()

    private val _fpsHistory = MutableStateFlow<List<FpsInfo>>(emptyList())

    /** Historical FPS samples for charting. */
    val fpsHistory: StateFlow<List<FpsInfo>> = _fpsHistory.asStateFlow()

    private val _isRunning = MutableStateFlow(false)

    /** Whether the FPS monitor is actively measuring frame delivery. */
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val choreographer: Choreographer by lazy {
        Choreographer.getInstance()
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isMonitoring) return

            if (lastFrameTimeNanos > 0) {
                val frameDurationNanos = frameTimeNanos - lastFrameTimeNanos
                // A long gap is a pause, not a frame: restart timing from this frame
                if (frameDurationNanos <= MAX_FRAME_GAP_NANOS) {
                    processFrame(frameDurationNanos)
                }
            }

            lastFrameTimeNanos = frameTimeNanos

            // Schedule next frame
            if (isMonitoring) {
                choreographer.postFrameCallback(this)
            }
        }
    }

    /**
     * Starts FPS monitoring.
     * If already monitoring, this is a no-op.
     */
    fun start() {
        if (isMonitoring) return

        isMonitoring = true
        _isRunning.value = true
        lastFrameTimeNanos = 0L
        refreshPeriodNanos = framePeriodNanos(refreshRateProvider())

        choreographer.postFrameCallback(frameCallback)
    }

    /**
     * Stops FPS monitoring.
     */
    fun stop() {
        isMonitoring = false
        _isRunning.value = false
        choreographer.removeFrameCallback(frameCallback)
    }

    /**
     * Resets all statistics and history.
     */
    fun reset() {
        frameTimesNanos.clear()
        fpsWindow.clear()
        totalDroppedFrames = 0
        totalJankFrames = 0
        minFpsRecorded = Float.MAX_VALUE
        maxFpsRecorded = 0f
        fpsSum = 0f
        fpsCount = 0
        lastFrameTimeNanos = 0L

        _currentFpsInfo.value = FpsInfo.EMPTY
        _fpsHistory.value = emptyList()
    }

    private fun processFrame(frameDurationNanos: Long) {
        // Track frame times for averaging
        frameTimesNanos.add(frameDurationNanos)
        if (frameTimesNanos.size > FPS_CALCULATION_WINDOW) {
            frameTimesNanos.removeAt(0)
        }

        // Check for dropped frame (> 1.5x refresh period)
        if (isDroppedFrame(frameDurationNanos, refreshPeriodNanos)) {
            totalDroppedFrames++
        }

        // Check for jank (> 32ms)
        if (frameDurationNanos > FpsInfo.JANK_THRESHOLD_NS) {
            totalJankFrames++
        }

        // Calculate current FPS from recent frames
        val currentFps = calculateCurrentFps()

        // Update statistics
        if (currentFps > 0) {
            minFpsRecorded = min(minFpsRecorded, currentFps)
            maxFpsRecorded = max(maxFpsRecorded, currentFps)
            fpsSum += currentFps
            fpsCount++
        }

        // Calculate average FPS
        val averageFps = if (fpsCount > 0) fpsSum / fpsCount else 0f

        // Create FpsInfo
        val fpsInfo = FpsInfo(
            currentFps = currentFps,
            averageFps = averageFps,
            minFps = if (minFpsRecorded == Float.MAX_VALUE) 0f else minFpsRecorded,
            maxFps = maxFpsRecorded,
            droppedFrames = totalDroppedFrames,
            jankFrames = totalJankFrames,
            timestamp = System.currentTimeMillis(),
        )

        _currentFpsInfo.value = fpsInfo

        // Update history (sample every N frames to avoid too frequent updates)
        fpsWindow.add(currentFps)
        if (fpsWindow.size >= HISTORY_SAMPLE_INTERVAL) {
            val avgForSample = fpsWindow.average().toFloat()
            fpsWindow.clear()

            val sampledInfo = fpsInfo.copy(currentFps = avgForSample)
            updateHistory(sampledInfo)
        }
    }

    private fun calculateCurrentFps(): Float {
        if (frameTimesNanos.isEmpty()) return 0f

        // Calculate average frame time over the window
        val avgFrameTimeNanos = frameTimesNanos.average()
        if (avgFrameTimeNanos <= 0) return 0f

        // Convert to FPS
        return (NANOS_PER_SECOND / avgFrameTimeNanos).toFloat()
    }

    private fun updateHistory(fpsInfo: FpsInfo) {
        val currentHistory = _fpsHistory.value.toMutableList()
        currentHistory.add(fpsInfo)

        // Keep only the last historySize samples
        while (currentHistory.size > historySize) {
            currentHistory.removeAt(0)
        }

        _fpsHistory.value = currentHistory.toList()
    }

    /** Sampling intervals and history size defaults. */
    companion object {
        /** Default number of FPS history samples to retain. */
        const val DEFAULT_HISTORY_SIZE = 60

        /** Warning threshold for low FPS (below this is considered poor). */
        const val FPS_WARNING_THRESHOLD = 30f
        private const val FPS_CALCULATION_WINDOW = 10
        private const val HISTORY_SAMPLE_INTERVAL = 6 // Sample every 6 frames (~100ms at 60fps)
        private const val NANOS_PER_SECOND = 1_000_000_000.0

        /** Refresh rate assumed when the display rate is unknown. */
        const val DEFAULT_REFRESH_RATE_HZ = 60f

        /** A frame counts as dropped when it exceeds this multiple of the refresh period. */
        private const val DROPPED_FRAME_TOLERANCE = 1.5

        /** Frame gaps above this (500ms) are treated as pauses and skipped. */
        private const val MAX_FRAME_GAP_NANOS = 500_000_000L

        internal fun framePeriodNanos(refreshRateHz: Float): Long {
            val rate = if (refreshRateHz > 0f) refreshRateHz else DEFAULT_REFRESH_RATE_HZ
            return (NANOS_PER_SECOND / rate).toLong()
        }

        internal fun isDroppedFrame(
            frameDurationNanos: Long,
            framePeriodNanos: Long,
        ): Boolean = frameDurationNanos > framePeriodNanos * DROPPED_FRAME_TOLERANCE
    }
}

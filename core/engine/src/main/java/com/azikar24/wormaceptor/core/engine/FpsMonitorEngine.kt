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
 * A frame is considered "dropped" if it takes longer than 16.67ms (60fps threshold).
 * A frame is considered "jank" if it takes longer than 32ms.
 */
class FpsMonitorEngine(
    private val historySize: Int = DEFAULT_HISTORY_SIZE,
) {
    private var isMonitoring = false
    private var lastFrameTimeNanos = 0L

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
    val currentFpsInfo: StateFlow<FpsInfo> = _currentFpsInfo.asStateFlow()

    private val _fpsHistory = MutableStateFlow<List<FpsInfo>>(emptyList())
    val fpsHistory: StateFlow<List<FpsInfo>> = _fpsHistory.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val choreographer: Choreographer by lazy {
        Choreographer.getInstance()
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isMonitoring) return

            if (lastFrameTimeNanos > 0) {
                val frameDurationNanos = frameTimeNanos - lastFrameTimeNanos
                processFrame(frameDurationNanos)
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

        // Check for dropped frame (> 16.67ms)
        if (frameDurationNanos > FpsInfo.TARGET_FRAME_TIME_NS) {
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

    companion object {
        const val DEFAULT_HISTORY_SIZE = 60
        private const val FPS_CALCULATION_WINDOW = 10
        private const val HISTORY_SAMPLE_INTERVAL = 6 // Sample every 6 frames (~100ms at 60fps)
        private const val NANOS_PER_SECOND = 1_000_000_000.0
    }
}

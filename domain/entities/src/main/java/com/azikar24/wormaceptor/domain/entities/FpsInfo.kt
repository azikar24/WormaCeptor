package com.azikar24.wormaceptor.domain.entities

/**
 * Represents FPS monitoring data at a specific point in time.
 *
 * @property currentFps The current frames per second
 * @property averageFps The average FPS over the monitoring period
 * @property minFps The minimum FPS recorded
 * @property maxFps The maximum FPS recorded
 * @property droppedFrames Number of frames that took longer than 16.67ms (60fps threshold)
 * @property jankFrames Number of frames that took longer than 32ms (severe jank threshold)
 * @property timestamp When this measurement was taken
 */
data class FpsInfo(
    val currentFps: Float,
    val averageFps: Float,
    val minFps: Float,
    val maxFps: Float,
    val droppedFrames: Int,
    val jankFrames: Int,
    val timestamp: Long,
) {
    /** Constants and default instances for [FpsInfo]. */
    companion object {
        /**
         * Default empty FpsInfo instance.
         */
        val EMPTY = FpsInfo(
            currentFps = 0f,
            averageFps = 0f,
            minFps = 0f,
            maxFps = 0f,
            droppedFrames = 0,
            jankFrames = 0,
            timestamp = 0L,
        )

        /**
         * Target frame time for 60 FPS (in nanoseconds).
         */
        const val TARGET_FRAME_TIME_NS = 16_666_666L // ~16.67ms

        /**
         * Jank threshold - frames taking longer than this are considered severe jank (in nanoseconds).
         */
        const val JANK_THRESHOLD_NS = 32_000_000L // 32ms
    }
}

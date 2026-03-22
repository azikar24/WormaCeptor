package com.azikar24.wormaceptor.core.engine

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset

/**
 * Represents the complete state of the performance overlay.
 *
 * This state is used to render the floating performance badge.
 * Position values are stored as percentages (0.0-1.0) of screen dimensions to handle
 * orientation changes gracefully.
 *
 * @property isOverlayEnabled Master toggle for showing/hiding the overlay
 * @property positionPercent Position as percentage of screen (0.0-1.0) for rotation handling
 * @property isDragging Whether the user is currently dragging the overlay
 * @property fpsEnabled Whether FPS metric is enabled and should be displayed
 * @property memoryEnabled Whether memory metric is enabled and should be displayed
 * @property cpuEnabled Whether CPU metric is enabled and should be displayed
 * @property fpsValue Current frames per second
 * @property fpsMonitorRunning Whether the FPS monitor is actively running
 * @property memoryPercent Current memory usage percentage
 * @property memoryMonitorRunning Whether the memory monitor is actively running
 * @property cpuPercent Current CPU usage percentage
 * @property cpuMonitorRunning Whether the CPU monitor is actively running
 */
@Stable
data class PerformanceOverlayState(
    val isOverlayEnabled: Boolean = false,
    val positionPercent: Offset = DEFAULT_POSITION_PERCENT,
    val isDragging: Boolean = false,
    val fpsEnabled: Boolean = false,
    val memoryEnabled: Boolean = false,
    val cpuEnabled: Boolean = false,
    val fpsValue: Int = 0,
    val fpsMonitorRunning: Boolean = false,
    val memoryPercent: Int = 0,
    val memoryMonitorRunning: Boolean = false,
    val cpuPercent: Int = 0,
    val cpuMonitorRunning: Boolean = false,
) {
    /**
     * Returns true if at least one metric is enabled.
     */
    fun hasAnyMetricEnabled(): Boolean = fpsEnabled || memoryEnabled || cpuEnabled

    /**
     * Returns true if the overlay is currently in the dismiss zone (bottom of screen).
     */
    fun isInDismissZone(): Boolean = positionPercent.y >= DISMISS_ZONE_THRESHOLD

    /** Positioning defaults and empty state factory. */
    companion object {
        /**
         * Default position: top-right corner with safe margin.
         * X = 0.80 means 80% from left (right side with room for content)
         * Y = 0.05 means 5% from top (near top edge)
         */
        val DEFAULT_POSITION_PERCENT = Offset(0.80f, 0.05f)

        /**
         * Y position threshold for dismiss zone (bottom 15% of screen).
         */
        const val DISMISS_ZONE_THRESHOLD = 0.85f

        /**
         * Empty state with no data.
         */
        val EMPTY = PerformanceOverlayState()
    }
}

/**
 * Color thresholds for performance metrics visualization.
 * These define when metrics should show green (good), yellow (warning), or red (critical).
 */
object PerformanceThresholds {
    /** FPS at or above this value is considered healthy. */
    const val FPS_GOOD = 55

    /** FPS at or above this value (but below [FPS_GOOD]) triggers a warning. */
    const val FPS_WARNING = 30

    /** Memory usage percentage below this value is considered healthy. */
    const val MEMORY_GOOD = 60

    /** Memory usage percentage at or above this value is critical. */
    const val MEMORY_WARNING = 80

    /** CPU usage percentage below this value is considered healthy. */
    const val CPU_GOOD = 50

    /** CPU usage percentage at or above this value is critical. */
    const val CPU_WARNING = 80
}

/**
 * Enum representing the health status of a metric.
 */
enum class MetricStatus {
    /** Metric value is within healthy range. */
    GOOD,

    /** Metric value is approaching a problematic threshold. */
    WARNING,

    /** Metric value has exceeded the acceptable threshold. */
    CRITICAL,

    /** Metric is not being actively monitored (paused). */
    INACTIVE,
    ;

    /** Threshold-based factories for deriving status from raw metric values. */
    companion object {
        /**
         * Determines FPS status based on value.
         * Higher FPS is better.
         *
         * @param fps The current FPS value
         * @param isMonitoring Whether the FPS monitor is actively running
         */
        fun fromFps(
            fps: Int,
            isMonitoring: Boolean = true,
        ): MetricStatus = when {
            !isMonitoring -> INACTIVE
            fps >= PerformanceThresholds.FPS_GOOD -> GOOD
            fps >= PerformanceThresholds.FPS_WARNING -> WARNING
            else -> CRITICAL
        }

        /**
         * Determines memory status based on percentage.
         * Lower memory usage is better.
         *
         * @param percent The current memory usage percentage
         * @param isMonitoring Whether the memory monitor is actively running
         */
        fun fromMemoryPercent(
            percent: Int,
            isMonitoring: Boolean = true,
        ): MetricStatus = when {
            !isMonitoring -> INACTIVE
            percent < PerformanceThresholds.MEMORY_GOOD -> GOOD
            percent <= PerformanceThresholds.MEMORY_WARNING -> WARNING
            else -> CRITICAL
        }

        /**
         * Determines CPU status based on percentage.
         * Lower CPU usage is better.
         *
         * @param percent The current CPU usage percentage
         * @param isMonitoring Whether the CPU monitor is actively running
         */
        fun fromCpuPercent(
            percent: Int,
            isMonitoring: Boolean = true,
        ): MetricStatus = when {
            !isMonitoring -> INACTIVE
            percent < PerformanceThresholds.CPU_GOOD -> GOOD
            percent <= PerformanceThresholds.CPU_WARNING -> WARNING
            else -> CRITICAL
        }
    }
}

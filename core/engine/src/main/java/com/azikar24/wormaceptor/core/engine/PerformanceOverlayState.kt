package com.azikar24.wormaceptor.core.engine

import androidx.compose.runtime.Immutable
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
 * @property fpsHistory History of FPS values for sparkline (last 30 data points)
 * @property fpsMonitorRunning Whether the FPS monitor is actively running
 * @property memoryPercent Current memory usage percentage
 * @property memoryHistory History of memory percentage values for sparkline
 * @property memoryMonitorRunning Whether the memory monitor is actively running
 * @property cpuPercent Current CPU usage percentage
 * @property cpuHistory History of CPU percentage values for sparkline
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
    val fpsHistory: List<Float> = emptyList(),
    val fpsMonitorRunning: Boolean = false,
    val memoryPercent: Int = 0,
    val memoryHistory: List<Float> = emptyList(),
    val memoryMonitorRunning: Boolean = false,
    val cpuPercent: Int = 0,
    val cpuHistory: List<Float> = emptyList(),
    val cpuMonitorRunning: Boolean = false,
) {
    companion object {
        /**
         * Default position: top-right corner with some margin.
         * X = 0.95 means 95% from left (near right edge)
         * Y = 0.05 means 5% from top (near top edge)
         */
        val DEFAULT_POSITION_PERCENT = Offset(0.95f, 0.05f)

        /**
         * Maximum number of history points to keep for sparklines.
         * At 1 sample per second, this represents 30 seconds of data.
         */
        const val HISTORY_SIZE = 30

        /**
         * Y position threshold for dismiss zone (bottom 15% of screen).
         */
        const val DISMISS_ZONE_THRESHOLD = 0.85f

        /**
         * Empty state with no data.
         */
        val EMPTY = PerformanceOverlayState()
    }

    /**
     * Returns true if at least one metric is enabled.
     */
    fun hasAnyMetricEnabled(): Boolean = fpsEnabled || memoryEnabled || cpuEnabled

    /**
     * Returns the count of enabled metrics.
     */
    fun enabledMetricCount(): Int = listOf(fpsEnabled, memoryEnabled, cpuEnabled).count { it }

    /**
     * Returns true if the overlay is currently in the dismiss zone (bottom of screen).
     */
    fun isInDismissZone(): Boolean = positionPercent.y >= DISMISS_ZONE_THRESHOLD
}

/**
 * Color thresholds for performance metrics visualization.
 * These define when metrics should show green (good), yellow (warning), or red (critical).
 */
object PerformanceThresholds {
    // FPS thresholds
    const val FPS_GOOD = 55
    const val FPS_WARNING = 30

    // Memory usage thresholds (percentage)
    const val MEMORY_GOOD = 60
    const val MEMORY_WARNING = 80

    // CPU usage thresholds (percentage)
    const val CPU_GOOD = 50
    const val CPU_WARNING = 80
}

/**
 * Enum representing the health status of a metric.
 */
enum class MetricStatus {
    GOOD,
    WARNING,
    CRITICAL,

    /** Metric is not being actively monitored (paused). */
    INACTIVE,
    ;

    companion object {
        /**
         * Determines FPS status based on value.
         * Higher FPS is better.
         *
         * @param fps The current FPS value
         * @param isMonitoring Whether the FPS monitor is actively running
         */
        fun fromFps(fps: Int, isMonitoring: Boolean = true): MetricStatus = when {
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
        fun fromMemoryPercent(percent: Int, isMonitoring: Boolean = true): MetricStatus = when {
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
        fun fromCpuPercent(percent: Int, isMonitoring: Boolean = true): MetricStatus = when {
            !isMonitoring -> INACTIVE
            percent < PerformanceThresholds.CPU_GOOD -> GOOD
            percent <= PerformanceThresholds.CPU_WARNING -> WARNING
            else -> CRITICAL
        }
    }
}

/**
 * Persisted settings for the performance overlay.
 * These values survive app restarts via SharedPreferences.
 *
 * @property enabled Whether the overlay is currently enabled
 * @property positionXPercent X position as percentage (0.0-1.0)
 * @property positionYPercent Y position as percentage (0.0-1.0)
 * @property showFps Whether to show FPS metric
 * @property showMemory Whether to show memory metric
 * @property showCpu Whether to show CPU metric
 */
@Immutable
data class PerformanceOverlaySettings(
    val enabled: Boolean = false,
    val positionXPercent: Float = PerformanceOverlayState.DEFAULT_POSITION_PERCENT.x,
    val positionYPercent: Float = PerformanceOverlayState.DEFAULT_POSITION_PERCENT.y,
    val showFps: Boolean = true,
    val showMemory: Boolean = true,
    val showCpu: Boolean = true,
) {
    companion object {
        val DEFAULT = PerformanceOverlaySettings()
    }
}

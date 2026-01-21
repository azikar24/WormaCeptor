/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents CPU usage information at a specific point in time.
 *
 * @property timestamp Timestamp in milliseconds when this snapshot was taken
 * @property overallUsagePercent Overall CPU usage percentage (0-100)
 * @property perCoreUsage List of usage percentages for each CPU core (0-100 each)
 * @property coreCount Number of CPU cores
 * @property cpuFrequencyMHz Current CPU frequency in MHz (may be 0 if unavailable)
 * @property cpuTemperature CPU temperature in Celsius (null if unavailable)
 * @property uptime System uptime in milliseconds
 */
data class CpuInfo(
    val timestamp: Long,
    val overallUsagePercent: Float,
    val perCoreUsage: List<Float>,
    val coreCount: Int,
    val cpuFrequencyMHz: Long,
    val cpuTemperature: Float?,
    val uptime: Long = 0L,
) {
    companion object {
        /**
         * Creates an empty CpuInfo instance with zero/default values.
         */
        fun empty(): CpuInfo = CpuInfo(
            timestamp = 0L,
            overallUsagePercent = 0f,
            perCoreUsage = emptyList(),
            coreCount = 0,
            cpuFrequencyMHz = 0L,
            cpuTemperature = null,
            uptime = 0L,
        )
    }
}

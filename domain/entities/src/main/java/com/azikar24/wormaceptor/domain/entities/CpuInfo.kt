package com.azikar24.wormaceptor.domain.entities

/**
 * Indicates whether CPU measurements come from system-wide stats or the app process only.
 *
 * On Android 8+ (API 26), SELinux may restrict access to /proc/stat, so the engine
 * automatically falls back to reading the app's own /proc/self/stat.
 */
enum class CpuMeasurementSource {
    /** System-wide CPU usage from /proc/stat. */
    SYSTEM,

    /** App process CPU usage from /proc/self/stat. */
    PROCESS,
}

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
 * @property measurementSource Whether this is system-wide or app process measurement
 */
data class CpuInfo(
    val timestamp: Long,
    val overallUsagePercent: Float,
    val perCoreUsage: List<Float>,
    val coreCount: Int,
    val cpuFrequencyMHz: Long,
    val cpuTemperature: Float?,
    val uptime: Long = 0L,
    val measurementSource: CpuMeasurementSource = CpuMeasurementSource.SYSTEM,
) {
    /** Factory methods for [CpuInfo]. */
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

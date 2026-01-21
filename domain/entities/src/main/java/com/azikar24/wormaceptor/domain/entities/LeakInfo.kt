/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents information about a detected memory leak.
 *
 * @property timestamp Timestamp in milliseconds when the leak was detected
 * @property objectClass Fully qualified class name of the leaked object
 * @property leakDescription Human-readable description of the leak
 * @property retainedSize Estimated retained size in bytes (if calculable)
 * @property referencePath List of reference chain from GC root to leaked object
 * @property severity Severity level of the leak
 */
data class LeakInfo(
    val timestamp: Long,
    val objectClass: String,
    val leakDescription: String,
    val retainedSize: Long,
    val referencePath: List<String>,
    val severity: LeakSeverity,
) {
    /**
     * Severity levels for memory leaks.
     */
    enum class LeakSeverity {
        /** Low severity - minor leak, minimal impact */
        LOW,
        /** Medium severity - moderate leak, should be investigated */
        MEDIUM,
        /** High severity - significant leak, should be fixed soon */
        HIGH,
        /** Critical severity - severe leak, requires immediate attention */
        CRITICAL,
    }

    companion object {
        /**
         * Creates an empty LeakInfo instance with default values.
         */
        fun empty(): LeakInfo = LeakInfo(
            timestamp = 0L,
            objectClass = "",
            leakDescription = "",
            retainedSize = 0L,
            referencePath = emptyList(),
            severity = LeakSeverity.LOW,
        )
    }
}

/**
 * Summary statistics for detected memory leaks.
 *
 * @property totalLeaks Total number of leaks detected
 * @property criticalCount Number of critical severity leaks
 * @property highCount Number of high severity leaks
 * @property mediumCount Number of medium severity leaks
 * @property lowCount Number of low severity leaks
 * @property totalRetainedBytes Total retained memory in bytes across all leaks
 */
data class LeakSummary(
    val totalLeaks: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val totalRetainedBytes: Long,
) {
    companion object {
        /**
         * Creates an empty LeakSummary instance with zero values.
         */
        fun empty(): LeakSummary = LeakSummary(
            totalLeaks = 0,
            criticalCount = 0,
            highCount = 0,
            mediumCount = 0,
            lowCount = 0,
            totalRetainedBytes = 0L,
        )
    }
}

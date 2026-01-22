/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a main thread violation detected by StrictMode.
 *
 * Thread violations occur when blocking operations (disk I/O, network calls, etc.)
 * are performed on the main/UI thread, which can cause UI jank or ANRs.
 *
 * @property id Unique identifier for this violation
 * @property timestamp Timestamp in milliseconds when this violation occurred
 * @property violationType The type of violation detected
 * @property description Human-readable description of the violation
 * @property stackTrace Stack trace at the point of violation
 * @property durationMs Duration of the violation in milliseconds (if available)
 * @property threadName Name of the thread where violation occurred
 */
data class ThreadViolation(
    val id: Long,
    val timestamp: Long,
    val violationType: ViolationType,
    val description: String,
    val stackTrace: List<String>,
    val durationMs: Long?,
    val threadName: String,
) {
    /**
     * Types of thread violations that can be detected.
     */
    enum class ViolationType {
        /** File read operation on main thread */
        DISK_READ,

        /** File write operation on main thread */
        DISK_WRITE,

        /** Network operation on main thread */
        NETWORK,

        /** Slow method call on main thread */
        SLOW_CALL,

        /** Custom slow code block on main thread */
        CUSTOM_SLOW_CODE,
    }

    companion object {
        /**
         * Creates an empty ThreadViolation instance.
         */
        fun empty(): ThreadViolation = ThreadViolation(
            id = 0L,
            timestamp = 0L,
            violationType = ViolationType.SLOW_CALL,
            description = "",
            stackTrace = emptyList(),
            durationMs = null,
            threadName = "",
        )
    }
}

/**
 * Summary statistics for thread violations.
 *
 * @property totalViolations Total number of violations recorded
 * @property diskReadCount Number of disk read violations
 * @property diskWriteCount Number of disk write violations
 * @property networkCount Number of network violations
 * @property slowCallCount Number of slow call violations
 * @property customSlowCodeCount Number of custom slow code violations
 */
data class ViolationStats(
    val totalViolations: Int,
    val diskReadCount: Int,
    val diskWriteCount: Int,
    val networkCount: Int,
    val slowCallCount: Int,
    val customSlowCodeCount: Int,
) {
    companion object {
        /**
         * Creates an empty ViolationStats instance.
         */
        fun empty(): ViolationStats = ViolationStats(
            totalViolations = 0,
            diskReadCount = 0,
            diskWriteCount = 0,
            networkCount = 0,
            slowCallCount = 0,
            customSlowCodeCount = 0,
        )
    }
}

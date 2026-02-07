package com.azikar24.wormaceptor.domain.entities

/**
 * Log level enum corresponding to Android's Log class levels.
 */
enum class LogLevel(val priority: Int, val tag: String) {
    VERBOSE(2, "V"),
    DEBUG(3, "D"),
    INFO(4, "I"),
    WARN(5, "W"),
    ERROR(6, "E"),
    ASSERT(7, "A"),
    ;

    companion object {
        fun fromPriority(priority: Int): LogLevel {
            return entries.find { it.priority == priority } ?: VERBOSE
        }

        fun fromTag(tag: String): LogLevel {
            return entries.find { it.tag.equals(tag, ignoreCase = true) } ?: VERBOSE
        }
    }
}

/**
 * Represents a single log entry captured from logcat.
 *
 * @property id Unique identifier for the log entry
 * @property timestamp Timestamp in milliseconds when the log was captured
 * @property level Log level (VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT)
 * @property tag The tag associated with the log entry
 * @property pid Process ID that generated the log
 * @property tid Thread ID that generated the log
 * @property message The log message content
 */
data class LogEntry(
    val id: Long,
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val pid: Int,
    val tid: Int = 0,
    val message: String,
)

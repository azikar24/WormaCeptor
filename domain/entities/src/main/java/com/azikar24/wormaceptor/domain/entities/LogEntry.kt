package com.azikar24.wormaceptor.domain.entities

/**
 * Log level enum corresponding to Android's Log class levels.
 */
enum class LogLevel(
    /** Android Log priority constant (2=VERBOSE through 7=ASSERT). */
    val priority: Int,
    /** Single-character abbreviation used in logcat output. */
    val tag: String,
) {
    /** Lowest priority; fine-grained diagnostic messages. */
    VERBOSE(2, "V"),

    /** Debugging messages useful during development. */
    DEBUG(3, "D"),

    /** Informational messages highlighting progress or state. */
    INFO(4, "I"),

    /** Potentially harmful situations that deserve attention. */
    WARN(5, "W"),

    /** Error events that might still allow the app to continue. */
    ERROR(6, "E"),

    /** Severe failure; the app is in an unrecoverable state. */
    ASSERT(7, "A"),
    ;

    /** Lookup helpers for converting raw log data to [LogLevel]. */
    companion object {
        /** Resolves a [LogLevel] from an Android log priority constant, defaulting to [VERBOSE]. */
        fun fromPriority(priority: Int): LogLevel {
            return entries.find { it.priority == priority } ?: VERBOSE
        }

        /** Resolves a [LogLevel] from a single-character tag (e.g., "D"), defaulting to [VERBOSE]. */
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

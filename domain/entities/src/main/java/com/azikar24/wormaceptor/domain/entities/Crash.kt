package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a captured application crash event.
 *
 * @property id Unique identifier for the crash record.
 * @property timestamp Epoch millis when the crash occurred.
 * @property exceptionType Fully qualified class name of the thrown exception.
 * @property message Optional message attached to the exception.
 * @property stackTrace Full stack trace string of the crash.
 */
data class Crash(
    val id: Long = 0,
    val timestamp: Long,
    val exceptionType: String,
    val message: String?,
    val stackTrace: String,
)

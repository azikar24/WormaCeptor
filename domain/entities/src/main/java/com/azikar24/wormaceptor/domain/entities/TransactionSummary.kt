package com.azikar24.wormaceptor.domain.entities

import java.util.UUID

/**
 * Lightweight summary of a network transaction used for list displays.
 *
 * Contains only the fields needed to render a transaction row without
 * loading the full [NetworkTransaction] with its request/response bodies.
 *
 * @property id Unique identifier matching the parent [NetworkTransaction].
 * @property method HTTP method (e.g. GET, POST).
 * @property host Host portion of the request URL.
 * @property path Path portion of the request URL.
 * @property code HTTP status code, or null if not yet received.
 * @property tookMs Round-trip time in milliseconds, or null if still in-flight.
 * @property hasRequestBody Whether a request body is present.
 * @property hasResponseBody Whether a response body is present.
 * @property status Current lifecycle status of the transaction.
 * @property timestamp Epoch millis when the transaction was initiated.
 */
data class TransactionSummary(
    val id: UUID,
    val method: String,
    val host: String,
    val path: String,
    val code: Int?,
    val tookMs: Long?,
    val hasRequestBody: Boolean,
    val hasResponseBody: Boolean,
    val status: TransactionStatus,
    val timestamp: EpochMillis,
)

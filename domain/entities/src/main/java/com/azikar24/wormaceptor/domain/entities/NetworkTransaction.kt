package com.azikar24.wormaceptor.domain.entities

import java.util.UUID

/** Lifecycle status of a network transaction. */
enum class TransactionStatus {
    /** Transaction is in-flight; response has not yet been received. */
    ACTIVE,

    /** Transaction finished successfully with a response. */
    COMPLETED,

    /** Transaction terminated with an error before a response was received. */
    FAILED,
}

/**
 * Full representation of a captured HTTP network transaction.
 *
 * @property id Unique identifier for this transaction.
 * @property timestamp Epoch millis when the transaction was initiated.
 * @property durationMs Total round-trip time in milliseconds, or null while in-flight.
 * @property status Current lifecycle status of the transaction.
 * @property request The outgoing HTTP request.
 * @property response The received HTTP response, or null if not yet available.
 * @property extensions Arbitrary key-value metadata attached by interceptors.
 */
data class NetworkTransaction(
    val id: UUID = UUID.randomUUID(),
    val timestamp: EpochMillis = System.currentTimeMillis(),
    val durationMs: Long? = null,
    val status: TransactionStatus = TransactionStatus.ACTIVE,
    val request: Request,
    val response: Response? = null,
    val extensions: Map<String, String> = emptyMap(),
)

package com.azikar24.wormaceptor.domain.entities

import java.util.UUID

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

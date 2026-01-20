package com.azikar24.wormaceptor.domain.entities

import java.util.UUID

enum class TransactionStatus { ACTIVE, COMPLETED, FAILED }

data class NetworkTransaction(
    val id: UUID = UUID.randomUUID(),
    val timestamp: EpochMillis = System.currentTimeMillis(),
    val durationMs: Long? = null,
    val status: TransactionStatus = TransactionStatus.ACTIVE,
    val request: Request,
    val response: Response? = null,
    val extensions: Map<String, String> = emptyMap(),
)

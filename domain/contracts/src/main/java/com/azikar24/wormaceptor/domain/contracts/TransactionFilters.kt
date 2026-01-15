package com.azikar24.wormaceptor.domain.contracts

/**
 * Filters for querying transactions with pagination support.
 */
data class TransactionFilters(
    val statusRange: IntRange? = null,
    val method: String? = null,
    val minDuration: Long? = null,
    val maxDuration: Long? = null,
    val contentType: String? = null
)

package com.azikar24.wormaceptor.domain.contracts

/**
 * Filters for querying transactions with pagination support.
 */
data class TransactionFilters(
    /** Inclusive HTTP status code range (e.g., 200..299), null for no filter. */
    val statusRange: IntRange? = null,
    /** HTTP method to match (e.g., "GET"), null for all methods. */
    val method: String? = null,
    /** Minimum request duration in milliseconds, null for no lower bound. */
    val minDuration: Long? = null,
    /** Maximum request duration in milliseconds, null for no upper bound. */
    val maxDuration: Long? = null,
    /** Response content-type substring to match (e.g., "json"), null for all types. */
    val contentType: String? = null,
)

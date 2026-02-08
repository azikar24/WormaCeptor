package com.azikar24.wormaceptor.feature.viewer.ui.components

import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Quick filters that can be applied to the transaction list.
 * Each filter has a label and a predicate function.
 */
enum class QuickFilter(val label: String) {
    ERRORS("Errors"),
    SLOW("Slow (>1s)"),
    LARGE(">100KB"),
    TODAY("Today"),
    JSON("JSON"),
    IMAGES("Images"),
    ;

    /**
     * Returns true if the transaction matches this filter criteria.
     */
    fun matches(transaction: TransactionSummary): Boolean = when (this) {
        ERRORS -> transaction.code != null && transaction.code in 400..599
        SLOW -> transaction.tookMs?.let { it > 1000 } ?: false
        LARGE -> false // Would need response size in TransactionSummary
        TODAY -> isToday(transaction.timestamp)
        JSON -> transaction.path.contains(".json", ignoreCase = true) ||
            transaction.path.contains("json", ignoreCase = true)
        IMAGES -> transaction.path.let { path ->
            path.endsWith(".png", ignoreCase = true) ||
                path.endsWith(".jpg", ignoreCase = true) ||
                path.endsWith(".jpeg", ignoreCase = true) ||
                path.endsWith(".gif", ignoreCase = true) ||
                path.endsWith(".webp", ignoreCase = true) ||
                path.endsWith(".svg", ignoreCase = true)
        }
    }

    private fun isToday(timestampMillis: Long): Boolean {
        val today = LocalDate.now()
        val transactionDate = Instant.ofEpochMilli(timestampMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return today == transactionDate
    }
}

/**
 * Applies all active quick filters to a list of transactions.
 * Uses AND logic: a transaction must match ALL active filters.
 */
fun List<TransactionSummary>.applyQuickFilters(activeFilters: Set<QuickFilter>): List<TransactionSummary> {
    if (activeFilters.isEmpty()) return this

    return filter { transaction ->
        activeFilters.all { filter -> filter.matches(transaction) }
    }
}

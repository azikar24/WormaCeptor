package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.components.QuickFilter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID

data class TransactionListViewState(
    val searchQuery: String = "",
    val filterMethods: Set<String> = emptySet(),
    val filterStatusRanges: Set<IntRange> = emptySet(),
    val totalCount: Int = 0,
    val isRefreshingTransactions: Boolean = false,
    val quickFilters: Set<QuickFilter> = emptySet(),
    val selectedIds: Set<UUID> = emptySet(),
    val isInitialLoading: Boolean = true,
    val transactions: ImmutableList<TransactionSummary> = persistentListOf(),
    val allTransactions: ImmutableList<TransactionSummary> = persistentListOf(),
    val showFilterSheet: Boolean = false,
    val draftFilterQuery: String = "",
    val draftFilterMethods: Set<String> = emptySet(),
    val draftFilterStatusRanges: Set<IntRange> = emptySet(),
    val showClearTransactionsDialog: Boolean = false,
    val showDeleteSelectedDialog: Boolean = false,
) {
    val hasActiveFilters: Boolean
        get() = filterMethods.isNotEmpty() ||
            filterStatusRanges.isNotEmpty() ||
            searchQuery.isNotBlank()
}

package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.feature.viewer.ui.components.QuickFilter
import java.util.UUID

/**
 * Consolidated UI state for the viewer home screen.
 *
 * Reactive data streams such as [ViewerViewModel.transactions], [ViewerViewModel.crashes],
 * and [ViewerViewModel.pagedTransactions] remain as separate [kotlinx.coroutines.flow.StateFlow]
 * properties on the ViewModel because they are engine-driven and require flow operators
 * (combine, debounce, flatMapLatest) that do not fit inside a simple data class.
 */
data class ViewerViewState(
    /** Current search text entered by the user. */
    val searchQuery: String = "",
    /** Set of HTTP method strings selected as filters (e.g. "GET", "POST"). */
    val filterMethods: Set<String> = emptySet(),
    /** Set of status-code ranges selected as filters (e.g. 200..299). */
    val filterStatusRanges: Set<IntRange> = emptySet(),
    /** Index of the currently selected tab (Transactions / Crashes / Tools). */
    val selectedTabIndex: Int = 0,
    /** Total unfiltered transaction count used for display purposes. */
    val totalCount: Int = 0,
    /** Whether the transactions list is currently showing pull-to-refresh feedback. */
    val isRefreshingTransactions: Boolean = false,
    /** Whether the crashes list is currently showing pull-to-refresh feedback. */
    val isRefreshingCrashes: Boolean = false,
    /** Set of currently active quick filters applied to the transaction list. */
    val quickFilters: Set<QuickFilter> = emptySet(),
    /** Set of transaction IDs that are currently selected for bulk actions. */
    val selectedIds: Set<UUID> = emptySet(),
    /** Whether the initial data load is still in progress. */
    val isInitialLoading: Boolean = true,
    /** Set of tool category names that are currently collapsed. */
    val collapsedToolCategories: Set<String> = emptySet(),
)

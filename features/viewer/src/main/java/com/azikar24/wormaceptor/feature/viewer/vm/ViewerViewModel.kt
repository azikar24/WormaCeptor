package com.azikar24.wormaceptor.feature.viewer.vm

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.domain.contracts.TransactionFilters
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.components.QuickFilter
import com.azikar24.wormaceptor.feature.viewer.ui.components.applyQuickFilters
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * MVI ViewModel for the viewer home screen.
 *
 * Manages simple UI state via [ViewerViewState] and exposes engine-driven reactive
 * data streams ([allTransactions], [transactions], [pagedTransactions], [crashes])
 * as separate [StateFlow] / [Flow] properties.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ViewerViewModel(
    private val queryEngine: QueryEngine,
    context: Context,
) : BaseViewModel<ViewerViewState, ViewerViewEffect, ViewerViewEvent>(ViewerViewState()) {

    private val collapsePrefs = context.getSharedPreferences(COLLAPSE_PREFS_NAME, Context.MODE_PRIVATE)

    init {
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                collapsePrefs.getStringSet(COLLAPSE_KEY, emptySet()) ?: emptySet()
            }
            if (saved.isNotEmpty()) {
                updateState { copy(collapsedToolCategories = saved) }
            }
        }
    }

    /** Unfiltered stream of every recorded transaction summary. */
    val allTransactions: StateFlow<ImmutableList<TransactionSummary>> = queryEngine.observeTransactions()
        .map { it.toImmutableList() }
        .onEach { updateState { copy(isInitialLoading = false) } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), persistentListOf())

    /** Paginated transaction stream that reacts to search query and filter state changes. */
    val pagedTransactions: Flow<PagingData<TransactionSummary>> =
        combine(
            uiState.map { it.searchQuery }.debounce(SEARCH_DEBOUNCE_PAGED),
            uiState.map { it.filterMethods },
            uiState.map { it.filterStatusRanges },
        ) { query, methods, statusRanges ->
            Triple(query, methods, statusRanges)
        }.flatMapLatest { (query, methods, statusRanges) ->
            val filters = TransactionFilters(
                statusRange = statusRanges.singleOrNull(),
                method = methods.singleOrNull(),
            )
            viewModelScope.launch {
                val count = queryEngine.getTransactionCount(query.takeIf { it.isNotBlank() })
                updateState { copy(totalCount = count) }
            }
            queryEngine.observeTransactionsPaged(
                searchQuery = query.takeIf { it.isNotBlank() },
                filters = filters,
                pageSize = PAGE_SIZE,
            )
        }.cachedIn(viewModelScope)

    /** Filtered and search-aware transaction list with quick-filter support. */
    val transactions: StateFlow<ImmutableList<TransactionSummary>> = combine(
        uiState.map { it.searchQuery }.debounce(SEARCH_DEBOUNCE_LIST),
        uiState.map { it.filterMethods },
        uiState.map { it.filterStatusRanges },
        uiState.map { it.quickFilters },
        allTransactions,
    ) { query, methods, statusRanges, quickFilters, list ->
        list.filter { transaction ->
            val matchesSearch = if (query.isBlank()) {
                true
            } else {
                transaction.host.contains(query, ignoreCase = true) ||
                    transaction.path.contains(query, ignoreCase = true) ||
                    transaction.method.contains(query, ignoreCase = true) ||
                    transaction.status.name.contains(query, ignoreCase = true)
            }

            val matchesMethod = methods.isEmpty() ||
                methods.any { transaction.method.equals(it, ignoreCase = true) }

            val matchesStatus = statusRanges.isEmpty() ||
                (transaction.code?.let { code -> statusRanges.any { code in it } } ?: false)

            matchesSearch && matchesMethod && matchesStatus
        }.applyQuickFilters(quickFilters).toImmutableList()
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), persistentListOf())

    /** Reactive stream of all recorded crash entries, ordered by most recent first. */
    val crashes = queryEngine.observeCrashes()
        .map { it.toImmutableList() }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), persistentListOf())

    /** Whether the user is currently in multi-select mode (at least one transaction selected). */
    val isSelectionMode: StateFlow<Boolean> = uiState
        .map { it.selectedIds.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), false)

    override fun handleEvent(event: ViewerViewEvent) {
        when (event) {
            is ViewerViewEvent.SearchQueryChanged -> updateState { copy(searchQuery = event.query) }
            is ViewerViewEvent.MethodFiltersChanged -> updateState { copy(filterMethods = event.methods) }
            is ViewerViewEvent.StatusFiltersChanged -> updateState { copy(filterStatusRanges = event.ranges) }
            is ViewerViewEvent.ClearFilters -> updateState {
                copy(filterMethods = emptySet(), filterStatusRanges = emptySet())
            }
            is ViewerViewEvent.TabSelected -> updateState { copy(selectedTabIndex = event.index) }

            is ViewerViewEvent.QuickFilterToggled -> handleQuickFilterToggle(event.filter)
            is ViewerViewEvent.QuickFiltersCleared -> updateState { copy(quickFilters = emptySet()) }

            is ViewerViewEvent.SelectionToggled -> handleSelectionToggle(event.id)
            is ViewerViewEvent.SelectAllClicked -> handleSelectAll()
            is ViewerViewEvent.SelectionCleared -> updateState { copy(selectedIds = emptySet()) }
            is ViewerViewEvent.DeleteSelectedClicked -> handleDeleteSelected()
            is ViewerViewEvent.DeleteTransaction -> handleDeleteTransaction(event.id)

            is ViewerViewEvent.ClearAllTransactions -> handleClearAllTransactions()
            is ViewerViewEvent.ClearAllCrashes -> handleClearAllCrashes()

            is ViewerViewEvent.RefreshTransactions -> handleRefreshTransactions()
            is ViewerViewEvent.RefreshCrashes -> handleRefreshCrashes()

            is ViewerViewEvent.ShowMessage -> emitEffect(ViewerViewEffect.ShowSnackbar(event.message))
            is ViewerViewEvent.ToolCategoryCollapseToggled -> handleToolCategoryCollapseToggle(event.category)

            is ViewerViewEvent.FilterSheetVisibilityChanged ->
                updateState { copy(showFilterSheet = event.visible) }
            is ViewerViewEvent.OverflowMenuVisibilityChanged ->
                updateState { copy(showOverflowMenu = event.visible) }
            is ViewerViewEvent.ToolsSearchActiveChanged -> updateState {
                copy(toolsSearchActive = event.active, toolsSearchQuery = if (event.active) toolsSearchQuery else "")
            }
            is ViewerViewEvent.ToolsSearchQueryChanged ->
                updateState { copy(toolsSearchQuery = event.query) }
            is ViewerViewEvent.ClearTransactionsDialogVisibilityChanged ->
                updateState { copy(showClearTransactionsDialog = event.visible) }
            is ViewerViewEvent.ClearCrashesDialogVisibilityChanged ->
                updateState { copy(showClearCrashesDialog = event.visible) }
            is ViewerViewEvent.DeleteSelectedDialogVisibilityChanged ->
                updateState { copy(showDeleteSelectedDialog = event.visible) }
        }
    }

    /** Returns the [TransactionSummary] items that are currently selected by the user. */
    fun getSelectedTransactions(): List<TransactionSummary> {
        val currentSelectedIds = uiState.value.selectedIds
        return transactions.value.filter { it.id in currentSelectedIds }
    }

    private fun handleQuickFilterToggle(filter: QuickFilter) {
        updateState {
            val updated = if (filter in quickFilters) quickFilters - filter else quickFilters + filter
            copy(quickFilters = updated)
        }
    }

    private fun handleSelectionToggle(id: UUID) {
        updateState {
            val updated = if (id in selectedIds) selectedIds - id else selectedIds + id
            copy(selectedIds = updated)
        }
    }

    private fun handleSelectAll() {
        viewModelScope.launch {
            val allIds = transactions.value.map { it.id }.toSet()
            updateState { copy(selectedIds = allIds) }
        }
    }

    private fun handleDeleteSelected() {
        viewModelScope.launch {
            val idsToDelete = uiState.value.selectedIds.toList()
            updateState { copy(selectedIds = emptySet(), showDeleteSelectedDialog = false) }
            queryEngine.deleteTransactions(idsToDelete)
        }
    }

    private fun handleDeleteTransaction(id: UUID) {
        viewModelScope.launch {
            queryEngine.deleteTransactions(listOf(id))
        }
    }

    private fun handleClearAllTransactions() {
        viewModelScope.launch {
            updateState { copy(showClearTransactionsDialog = false) }
            queryEngine.clear()
        }
    }

    private fun handleClearAllCrashes() {
        viewModelScope.launch {
            updateState { copy(showClearCrashesDialog = false) }
            queryEngine.clearCrashes()
        }
    }

    private fun handleRefreshTransactions() {
        if (uiState.value.isRefreshingTransactions) return

        viewModelScope.launch {
            updateState { copy(isRefreshingTransactions = true) }
            delay(REFRESH_DELAY)
            updateState { copy(isRefreshingTransactions = false) }
        }
    }

    private fun handleToolCategoryCollapseToggle(category: String) {
        updateState {
            val updated = if (category in collapsedToolCategories) {
                collapsedToolCategories - category
            } else {
                collapsedToolCategories + category
            }
            copy(collapsedToolCategories = updated)
        }
        viewModelScope.launch(Dispatchers.IO) {
            collapsePrefs.edit {
                putStringSet(COLLAPSE_KEY, uiState.value.collapsedToolCategories)
            }
        }
    }

    private fun handleRefreshCrashes() {
        if (uiState.value.isRefreshingCrashes) return

        viewModelScope.launch {
            updateState { copy(isRefreshingCrashes = true) }
            delay(REFRESH_DELAY)
            updateState { copy(isRefreshingCrashes = false) }
        }
    }

    /** Timeout and debounce constants for reactive streams. */
    companion object {
        private const val SUBSCRIPTION_TIMEOUT = 5000L
        private const val SEARCH_DEBOUNCE_PAGED = 100L
        private const val SEARCH_DEBOUNCE_LIST = 150L
        private const val PAGE_SIZE = 30
        private const val REFRESH_DELAY = 500L
        private const val COLLAPSE_PREFS_NAME = "wormaceptor_tools_collapse"
        private const val COLLAPSE_KEY = "collapsed"
    }
}

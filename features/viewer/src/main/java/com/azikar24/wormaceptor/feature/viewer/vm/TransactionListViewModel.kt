package com.azikar24.wormaceptor.feature.viewer.vm

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.common.presentation.NoOpNavigator
import com.azikar24.wormaceptor.common.presentation.SearchDebounce
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.domain.contracts.TransactionFilters
import com.azikar24.wormaceptor.domain.entities.ExportFormat
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.components.QuickFilter
import com.azikar24.wormaceptor.feature.viewer.ui.components.applyQuickFilters
import com.azikar24.wormaceptor.feature.viewer.ui.util.CurlGenerator
import com.azikar24.wormaceptor.feature.viewer.ui.util.buildFullUrl
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
import java.util.UUID

/**
 * MVI ViewModel for the transaction list tab.
 *
 * Manages transaction-related UI state via [TransactionListViewState] and exposes
 * engine-driven reactive data streams ([allTransactions], [transactions], [pagedTransactions])
 * as separate [StateFlow] / [Flow] properties.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TransactionListViewModel(
    private val queryEngine: QueryEngine,
) : BaseViewModel<TransactionListViewState, TransactionListViewEffect, TransactionListViewEvent, NoOpNavigator>(
    TransactionListViewState(),
    NoOpNavigator,
) {

    /** Unfiltered stream of every recorded transaction summary. Syncs into [TransactionListViewState]. */
    private val allTransactions: StateFlow<ImmutableList<TransactionSummary>> = queryEngine.observeTransactions()
        .map { it.toImmutableList() }
        .onEach { updateState { copy(isInitialLoading = false, allTransactions = it) } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, persistentListOf())

    /** Paginated transaction stream that reacts to search query and filter state changes. */
    val pagedTransactions: Flow<PagingData<TransactionSummary>> =
        combine(
            uiState.map { it.searchQuery }.debounce(SearchDebounce.DEFAULT),
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

    /** Filtered and search-aware transaction list with quick-filter support. Syncs into [TransactionListViewState]. */
    private val transactions: StateFlow<ImmutableList<TransactionSummary>> = combine(
        uiState.map { it.searchQuery }.debounce(SearchDebounce.DEFAULT),
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

            val matchesStatus = statusRanges.isEmpty() || transaction.code?.let { code ->
                statusRanges.any { code in it }
            } ?: false

            matchesSearch && matchesMethod && matchesStatus
        }.applyQuickFilters(quickFilters).toImmutableList()
    }.onEach { updateState { copy(transactions = it) } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, persistentListOf())

    /** Whether the user is currently in multi-select mode (at least one transaction selected). */
    val isSelectionMode: StateFlow<Boolean> = uiState
        .map { it.selectedIds.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), false)

    override fun handleEvent(event: TransactionListViewEvent) {
        when (event) {
            is TransactionListViewEvent.SearchQueryChanged -> updateState { copy(searchQuery = event.query) }
            is TransactionListViewEvent.MethodFiltersChanged -> updateState { copy(filterMethods = event.methods) }
            is TransactionListViewEvent.StatusFiltersChanged -> updateState { copy(filterStatusRanges = event.ranges) }
            is TransactionListViewEvent.ClearFilters -> updateState {
                copy(
                    searchQuery = "",
                    filterMethods = emptySet(),
                    filterStatusRanges = emptySet(),
                )
            }

            is TransactionListViewEvent.QuickFilterToggled -> handleQuickFilterToggle(event.filter)
            is TransactionListViewEvent.QuickFiltersCleared -> updateState { copy(quickFilters = emptySet()) }

            is TransactionListViewEvent.SelectionToggled -> handleSelectionToggle(event.id)
            is TransactionListViewEvent.SelectAllClicked -> handleSelectAll()
            is TransactionListViewEvent.SelectionCleared -> updateState { copy(selectedIds = emptySet()) }
            is TransactionListViewEvent.DeleteSelectedClicked -> handleDeleteSelected()
            is TransactionListViewEvent.DeleteTransaction -> handleDeleteTransaction(event.id)

            is TransactionListViewEvent.ClearAllTransactions -> handleClearAllTransactions()
            is TransactionListViewEvent.RefreshTransactions -> handleRefreshTransactions()

            is TransactionListViewEvent.ShowMessage -> emitEffect(TransactionListViewEffect.ShowSnackBar(event.message))

            is TransactionListViewEvent.FilterSheetVisibilityChanged ->
                updateState { copy(showFilterSheet = event.visible) }

            is TransactionListViewEvent.ClearTransactionsDialogVisibilityChanged ->
                updateState { copy(showClearTransactionsDialog = event.visible) }

            is TransactionListViewEvent.DeleteSelectedDialogVisibilityChanged ->
                updateState { copy(showDeleteSelectedDialog = event.visible) }

            // Export
            is TransactionListViewEvent.ExportAllTransactions -> handleExportAll(ExportFormat.JSON)
            is TransactionListViewEvent.ExportAllTransactionsAsHar -> handleExportAll(ExportFormat.HAR)
            is TransactionListViewEvent.ExportSelectedTransactions -> handleExportSelected(ExportFormat.JSON)
            is TransactionListViewEvent.ExportSelectedTransactionsAsHar -> handleExportSelected(ExportFormat.HAR)

            // Share
            is TransactionListViewEvent.ShareSelectedTransactions -> handleShareSelected()
            is TransactionListViewEvent.ShareTransactionAsHar -> handleShareAsHar(event.id)
            is TransactionListViewEvent.ShareTransaction -> handleShareTransaction(event.summary)

            // Clipboard
            is TransactionListViewEvent.CopyTransactionUrl -> handleCopyUrl(event.summary)
            is TransactionListViewEvent.CopyTransactionAsCurl -> handleCopyAsCurl(event.id)

            // Filter sheet draft
            is TransactionListViewEvent.Filter -> handleFilterEvent(event)
        }
    }

    private fun handleFilterEvent(event: TransactionListViewEvent.Filter) {
        when (event) {
            is TransactionListViewEvent.Filter.SheetOpened -> updateState {
                copy(
                    showFilterSheet = true,
                    draftFilterQuery = searchQuery,
                    draftFilterMethods = filterMethods,
                    draftFilterStatusRanges = filterStatusRanges,
                )
            }
            is TransactionListViewEvent.Filter.DraftQueryChanged -> updateState {
                copy(draftFilterQuery = event.query)
            }
            is TransactionListViewEvent.Filter.DraftMethodToggled -> updateState {
                val updated = if (event.method in draftFilterMethods) {
                    draftFilterMethods - event.method
                } else {
                    draftFilterMethods + event.method
                }
                copy(draftFilterMethods = updated)
            }
            is TransactionListViewEvent.Filter.DraftStatusRangeToggled -> updateState {
                val updated = if (event.range in draftFilterStatusRanges) {
                    draftFilterStatusRanges.filter { it != event.range }.toSet()
                } else {
                    draftFilterStatusRanges + setOf(event.range)
                }
                copy(draftFilterStatusRanges = updated)
            }
            is TransactionListViewEvent.Filter.DraftCleared -> updateState {
                copy(
                    draftFilterQuery = "",
                    draftFilterMethods = emptySet(),
                    draftFilterStatusRanges = emptySet(),
                )
            }
            is TransactionListViewEvent.Filter.Applied -> updateState {
                copy(
                    searchQuery = draftFilterQuery,
                    filterMethods = draftFilterMethods,
                    filterStatusRanges = draftFilterStatusRanges,
                    showFilterSheet = false,
                )
            }
        }
    }

    /** Returns the [TransactionSummary] items that are currently selected by the user. */
    fun getSelectedTransactions(): List<TransactionSummary> {
        val state = uiState.value
        return state.transactions.filter { it.id in state.selectedIds }
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

    private fun handleRefreshTransactions() {
        if (uiState.value.isRefreshingTransactions) return

        viewModelScope.launch {
            updateState { copy(isRefreshingTransactions = true) }
            delay(REFRESH_DELAY)
            updateState { copy(isRefreshingTransactions = false) }
        }
    }

    // ── Export / Share / Clipboard handlers ───────────────────────────

    private fun handleExportAll(format: ExportFormat) {
        viewModelScope.launch {
            val allTx = queryEngine.getAllTransactionsForExport()
            emitEffect(TransactionListViewEffect.ExportTransactions(allTx, format))
        }
    }

    private fun handleExportSelected(format: ExportFormat) {
        viewModelScope.launch {
            val selected = getSelectedTransactions()
            val fullTransactions = selected.mapNotNull { summary ->
                queryEngine.getDetails(summary.id)
            }
            emitEffect(TransactionListViewEffect.ExportTransactions(fullTransactions, format))
        }
    }

    private fun handleShareSelected() {
        val selected = getSelectedTransactions()
        val text = selected.joinToString("\n\n") { transaction ->
            val url = buildFullUrl(transaction.host, transaction.path)
            buildString {
                appendLine("${transaction.method} $url")
                appendLine("Status: ${transaction.code ?: "Pending"}")
                transaction.tookMs?.let { appendLine("Duration: ${it}ms") }
            }
        }
        emitEffect(
            TransactionListViewEffect.ShareText(
                text = text,
                title = "${selected.size} Transactions",
            ),
        )
    }

    private fun handleShareAsHar(id: UUID) {
        viewModelScope.launch {
            val full = queryEngine.getDetails(id) ?: return@launch
            emitEffect(TransactionListViewEffect.ExportTransactions(listOf(full), ExportFormat.HAR))
        }
    }

    private fun handleShareTransaction(summary: TransactionSummary) {
        val url = buildFullUrl(summary.host, summary.path)
        val text = buildString {
            appendLine("${summary.method} $url")
            appendLine("Status: ${summary.code ?: "Pending"}")
            summary.tookMs?.let { appendLine("Duration: ${it}ms") }
        }
        emitEffect(TransactionListViewEffect.ShareText(text = text, title = "Transaction"))
    }

    private fun handleCopyUrl(summary: TransactionSummary) {
        val url = buildFullUrl(summary.host, summary.path)
        emitEffect(TransactionListViewEffect.CopyToClipboard(label = "URL", content = url))
    }

    private fun handleCopyAsCurl(id: UUID) {
        viewModelScope.launch {
            val fullTransaction = queryEngine.getDetails(id)
            if (fullTransaction == null) {
                emitEffect(TransactionListViewEffect.ShowSnackBar("Failed to load transaction details"))
                return@launch
            }
            val body = fullTransaction.request.bodyRef?.let { blobId ->
                queryEngine.getBody(blobId)
            }
            val curl = CurlGenerator.generate(
                method = fullTransaction.request.method,
                url = fullTransaction.request.url,
                headers = fullTransaction.request.headers,
                body = body,
            )
            emitEffect(TransactionListViewEffect.CopyToClipboard(label = "cURL", content = curl))
        }
    }

    companion object {
        private const val SUBSCRIPTION_TIMEOUT = 5000L
        private const val PAGE_SIZE = 30
        private const val REFRESH_DELAY = 500L
    }
}

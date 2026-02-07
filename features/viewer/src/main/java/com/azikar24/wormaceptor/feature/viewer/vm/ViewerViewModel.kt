package com.azikar24.wormaceptor.feature.viewer.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ViewerViewModel(
    private val queryEngine: QueryEngine,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Snackbar message flow for Activity-level messages
    private val _snackbarMessage = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    /**
     * Show a snackbar message from the Activity level.
     */
    fun showMessage(message: String) {
        _snackbarMessage.tryEmit(message)
    }

    private val _filterMethods = MutableStateFlow<Set<String>>(emptySet())
    val filterMethods: StateFlow<Set<String>> = _filterMethods

    private val _filterStatusRanges = MutableStateFlow<Set<IntRange>>(emptySet())
    val filterStatusRanges: StateFlow<Set<IntRange>> = _filterStatusRanges

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    // Total count for UI display
    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount

    // Refresh state for pull-to-refresh
    private val _isRefreshingTransactions = MutableStateFlow(false)
    val isRefreshingTransactions: StateFlow<Boolean> = _isRefreshingTransactions

    private val _isRefreshingCrashes = MutableStateFlow(false)
    val isRefreshingCrashes: StateFlow<Boolean> = _isRefreshingCrashes

    // Quick filter state
    private val _quickFilters = MutableStateFlow<Set<QuickFilter>>(emptySet())
    val quickFilters: StateFlow<Set<QuickFilter>> = _quickFilters

    // Multi-select state
    private val _selectedIds = MutableStateFlow<Set<UUID>>(emptySet())
    val selectedIds: StateFlow<Set<UUID>> = _selectedIds

    val isSelectionMode: StateFlow<Boolean> = _selectedIds
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val allTransactions: StateFlow<ImmutableList<TransactionSummary>> = queryEngine.observeTransactions()
        .map { it.toImmutableList() }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    // Paged transactions with database-level pagination for better performance with large datasets
    val pagedTransactions: Flow<PagingData<TransactionSummary>> =
        combine(
            _searchQuery.debounce(100),
            _filterMethods,
            _filterStatusRanges,
        ) { query, methods, statusRanges ->
            Triple(query, methods, statusRanges)
        }.flatMapLatest { (query, methods, statusRanges) ->
            val filters = TransactionFilters(
                statusRange = statusRanges.singleOrNull(),
                method = methods.singleOrNull(),
            )
            // Update total count when filters change
            viewModelScope.launch {
                _totalCount.value = queryEngine.getTransactionCount(query.takeIf { it.isNotBlank() })
            }
            queryEngine.observeTransactionsPaged(
                searchQuery = query.takeIf { it.isNotBlank() },
                filters = filters,
                pageSize = 30,
            )
        }.cachedIn(viewModelScope)

    val transactions: StateFlow<ImmutableList<TransactionSummary>> = combine(
        _searchQuery.debounce(150),
        _filterMethods,
        _filterStatusRanges,
        _quickFilters,
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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    val crashes = queryEngine.observeCrashes()
        .map { it.toImmutableList() }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setMethodFilters(methods: Set<String>) {
        _filterMethods.value = methods
    }

    fun setStatusFilters(ranges: Set<IntRange>) {
        _filterStatusRanges.value = ranges
    }

    fun clearFilters() {
        _filterMethods.value = emptySet()
        _filterStatusRanges.value = emptySet()
    }

    suspend fun clearAllTransactions() {
        queryEngine.clear()
    }

    suspend fun clearAllCrashes() {
        queryEngine.clearCrashes()
    }

    fun updateSelectedTab(index: Int) {
        _selectedTabIndex.value = index
    }

    // Quick filter functions
    fun toggleQuickFilter(filter: QuickFilter) {
        _quickFilters.value = if (filter in _quickFilters.value) {
            _quickFilters.value - filter
        } else {
            _quickFilters.value + filter
        }
    }

    fun clearQuickFilters() {
        _quickFilters.value = emptySet()
    }

    val hasActiveQuickFilters: StateFlow<Boolean> = _quickFilters
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Selection functions
    fun toggleSelection(id: UUID) {
        _selectedIds.value = if (id in _selectedIds.value) {
            _selectedIds.value - id
        } else {
            _selectedIds.value + id
        }
    }

    fun selectAll() {
        viewModelScope.launch {
            _selectedIds.value = transactions.value.map { it.id }.toSet()
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    suspend fun deleteSelected() {
        val idsToDelete = _selectedIds.value.toList()
        _selectedIds.value = emptySet()
        queryEngine.deleteTransactions(idsToDelete)
    }

    suspend fun deleteTransaction(id: UUID) {
        queryEngine.deleteTransactions(listOf(id))
    }

    fun getSelectedTransactions(): List<TransactionSummary> {
        return transactions.value.filter { it.id in _selectedIds.value }
    }

    /**
     * Refresh transactions - triggers a simulated refresh.
     * The data is already reactive via Flow, so this mainly provides visual feedback.
     */
    fun refreshTransactions() {
        if (_isRefreshingTransactions.value) return // Ignore if already refreshing

        viewModelScope.launch {
            _isRefreshingTransactions.value = true
            // Small delay for visual feedback - data is already live via Flow
            delay(500)
            _isRefreshingTransactions.value = false
        }
    }

    /**
     * Refresh crashes - triggers a simulated refresh.
     * The data is already reactive via Flow, so this mainly provides visual feedback.
     */
    fun refreshCrashes() {
        if (_isRefreshingCrashes.value) return // Ignore if already refreshing

        viewModelScope.launch {
            _isRefreshingCrashes.value = true
            // Small delay for visual feedback - data is already live via Flow
            delay(500)
            _isRefreshingCrashes.value = false
        }
    }

    /**
     * Get the list of all transaction IDs for pager navigation
     */
    fun getTransactionIds(): List<UUID> {
        return allTransactions.value.map { it.id }
    }
}

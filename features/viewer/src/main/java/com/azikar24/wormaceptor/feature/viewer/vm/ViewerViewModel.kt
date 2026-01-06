package com.azikar24.wormaceptor.feature.viewer.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ViewerViewModel(
    private val queryEngine: QueryEngine
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _filterMethod = MutableStateFlow<String?>(null)
    val filterMethod: StateFlow<String?> = _filterMethod
    
    private val _filterStatusRange = MutableStateFlow<IntRange?>(null)
    val filterStatusRange: StateFlow<IntRange?> = _filterStatusRange

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    val transactions: StateFlow<List<TransactionSummary>> = combine(
        _searchQuery,
        _filterMethod,
        _filterStatusRange,
        queryEngine.observeTransactions()
    ) { query, method, statusRange, list ->
        list.filter { transaction ->
            val matchesSearch = if (query.isBlank()) true else {
                transaction.path.contains(query, ignoreCase = true) || 
                transaction.method.contains(query, ignoreCase = true) ||
                transaction.status.name.contains(query, ignoreCase = true)
            }
            
            val matchesMethod = method == null || transaction.method.equals(method, ignoreCase = true)
            
            val matchesStatus = statusRange == null || (transaction.code?.let { it in statusRange } ?: false)
            
            matchesSearch && matchesMethod && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val crashes = queryEngine.observeCrashes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    
    fun setMethodFilter(method: String?) {
        _filterMethod.value = method
    }
    
    fun setStatusFilter(range: IntRange?) {
        _filterStatusRange.value = range
    }
    
    fun clearFilters() {
        _filterMethod.value = null
        _filterStatusRange.value = null
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
}

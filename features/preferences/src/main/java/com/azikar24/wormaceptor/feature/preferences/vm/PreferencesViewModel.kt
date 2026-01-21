/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.preferences.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.domain.contracts.PreferencesRepository
import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the SharedPreferences Inspector feature.
 * Handles search, filtering, and CRUD operations on preferences.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PreferencesViewModel(
    private val repository: PreferencesRepository,
) : ViewModel() {

    // File list search query
    private val _fileSearchQuery = MutableStateFlow("")
    val fileSearchQuery: StateFlow<String> = _fileSearchQuery

    // Currently selected file
    private val _selectedFileName = MutableStateFlow<String?>(null)
    val selectedFileName: StateFlow<String?> = _selectedFileName

    // Item search query (for detail screen)
    private val _itemSearchQuery = MutableStateFlow("")
    val itemSearchQuery: StateFlow<String> = _itemSearchQuery

    // Type filter (null = all types)
    private val _typeFilter = MutableStateFlow<String?>(null)
    val typeFilter: StateFlow<String?> = _typeFilter

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // All preference files
    val preferenceFiles: StateFlow<ImmutableList<PreferenceFile>> = combine(
        repository.observePreferenceFiles(),
        _fileSearchQuery.debounce(150),
    ) { files, query ->
        files.filter { file ->
            query.isBlank() || file.name.contains(query, ignoreCase = true)
        }.sortedBy { it.name.lowercase() }.toImmutableList()
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    // Items for the selected file
    val preferenceItems: StateFlow<ImmutableList<PreferenceItem>> = _selectedFileName
        .flatMapLatest { fileName ->
            if (fileName == null) {
                flowOf(emptyList())
            } else {
                combine(
                    repository.observePreferenceItems(fileName),
                    _itemSearchQuery.debounce(150),
                    _typeFilter,
                ) { items, query, typeFilter ->
                    items.filter { item ->
                        val matchesQuery = query.isBlank() ||
                            item.key.contains(query, ignoreCase = true) ||
                            item.value.displayValue.contains(query, ignoreCase = true)

                        val matchesType = typeFilter == null ||
                            item.value.typeName == typeFilter

                        matchesQuery && matchesType
                    }
                }
            }
        }
        .map { it.toImmutableList() }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    // Available type filters based on current items
    val availableTypes: StateFlow<ImmutableList<String>> = _selectedFileName
        .flatMapLatest { fileName ->
            if (fileName == null) {
                flowOf(emptyList())
            } else {
                repository.observePreferenceItems(fileName).map { items ->
                    items.map { it.value.typeName }.distinct().sorted()
                }
            }
        }
        .map { it.toImmutableList() }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    // Total item count for selected file (unfiltered)
    val totalItemCount: StateFlow<Int> = _selectedFileName
        .flatMapLatest { fileName ->
            if (fileName == null) {
                flowOf(0)
            } else {
                repository.observePreferenceItems(fileName).map { it.size }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onFileSearchQueryChanged(query: String) {
        _fileSearchQuery.value = query
    }

    fun selectFile(fileName: String) {
        _selectedFileName.value = fileName
        _itemSearchQuery.value = ""
        _typeFilter.value = null
    }

    fun clearFileSelection() {
        _selectedFileName.value = null
        _itemSearchQuery.value = ""
        _typeFilter.value = null
    }

    fun onItemSearchQueryChanged(query: String) {
        _itemSearchQuery.value = query
    }

    fun setTypeFilter(typeName: String?) {
        _typeFilter.value = typeName
    }

    fun clearFilters() {
        _itemSearchQuery.value = ""
        _typeFilter.value = null
    }

    fun setPreference(key: String, value: PreferenceValue) {
        val fileName = _selectedFileName.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.setPreference(fileName, key, value)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePreference(key: String) {
        val fileName = _selectedFileName.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deletePreference(fileName, key)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearCurrentFile() {
        val fileName = _selectedFileName.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.clearFile(fileName)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Creates a new preference item in the current file.
     */
    fun createPreference(key: String, value: PreferenceValue) {
        setPreference(key, value)
    }
}

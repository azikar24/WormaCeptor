package com.azikar24.wormaceptor.feature.preferences.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.domain.contracts.PreferencesRepository
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val DebounceMillis = 150L

/**
 * ViewModel for the SharedPreferences Inspector feature.
 * Handles search, filtering, and CRUD operations on preferences.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PreferencesViewModel(
    private val repository: PreferencesRepository,
) : BaseViewModel<PreferencesViewState, PreferencesViewEffect, PreferencesViewEvent>(
    PreferencesViewState(),
) {

    private val _fileSearchQuery = MutableStateFlow("")
    private val _selectedFileName = MutableStateFlow<String?>(null)
    private val _itemSearchQuery = MutableStateFlow("")
    private val _typeFilter = MutableStateFlow<String?>(null)

    init {
        observePreferenceFiles()
        observePreferenceItems()
        observeAvailableTypes()
        observeTotalItemCount()
    }

    override fun handleEvent(event: PreferencesViewEvent) {
        when (event) {
            is PreferencesViewEvent.FileSearchQueryChanged -> handleFileSearchQueryChanged(event.query)
            is PreferencesViewEvent.SelectFile -> handleSelectFile(event.fileName)
            is PreferencesViewEvent.ClearFileSelection -> handleClearFileSelection()
            is PreferencesViewEvent.ItemSearchQueryChanged -> handleItemSearchQueryChanged(event.query)
            is PreferencesViewEvent.SetTypeFilter -> handleSetTypeFilter(event.typeName)
            is PreferencesViewEvent.ClearFilters -> handleClearFilters()
            is PreferencesViewEvent.SetPreference -> handleSetPreference(event.key, event.value)
            is PreferencesViewEvent.DeletePreference -> handleDeletePreference(event.key)
            is PreferencesViewEvent.ClearCurrentFile -> handleClearCurrentFile()
            is PreferencesViewEvent.CreatePreference -> handleSetPreference(event.key, event.value)
        }
    }

    private fun handleFileSearchQueryChanged(query: String) {
        _fileSearchQuery.value = query
        updateState { copy(fileSearchQuery = query) }
    }

    private fun handleSelectFile(fileName: String) {
        _selectedFileName.value = fileName
        _itemSearchQuery.value = ""
        _typeFilter.value = null
        updateState {
            copy(
                selectedFileName = fileName,
                itemSearchQuery = "",
                typeFilter = null,
            )
        }
    }

    private fun handleClearFileSelection() {
        _selectedFileName.value = null
        _itemSearchQuery.value = ""
        _typeFilter.value = null
        updateState {
            copy(
                selectedFileName = null,
                itemSearchQuery = "",
                typeFilter = null,
            )
        }
    }

    private fun handleItemSearchQueryChanged(query: String) {
        _itemSearchQuery.value = query
        updateState { copy(itemSearchQuery = query) }
    }

    private fun handleSetTypeFilter(typeName: String?) {
        _typeFilter.value = typeName
        updateState { copy(typeFilter = typeName) }
    }

    private fun handleClearFilters() {
        _itemSearchQuery.value = ""
        _typeFilter.value = null
        updateState { copy(itemSearchQuery = "", typeFilter = null) }
    }

    private fun handleSetPreference(
        key: String,
        value: PreferenceValue,
    ) {
        val fileName = uiState.value.selectedFileName ?: return
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            try {
                repository.setPreference(fileName, key, value)
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun handleDeletePreference(key: String) {
        val fileName = uiState.value.selectedFileName ?: return
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            try {
                repository.deletePreference(fileName, key)
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun handleClearCurrentFile() {
        val fileName = uiState.value.selectedFileName ?: return
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            try {
                repository.clearFile(fileName)
            } finally {
                updateState { copy(isLoading = false) }
            }
        }
    }

    private fun observePreferenceFiles() {
        viewModelScope.launch {
            combine(
                repository.observePreferenceFiles(),
                _fileSearchQuery.debounce(DebounceMillis),
            ) { files, query ->
                files.filter { file ->
                    query.isBlank() || file.name.contains(query, ignoreCase = true)
                }.sortedBy { it.name.lowercase() }.toImmutableList()
            }.flowOn(Dispatchers.Default)
                .collect { files ->
                    updateState { copy(preferenceFiles = files) }
                }
        }
    }

    private fun observePreferenceItems() {
        viewModelScope.launch {
            _selectedFileName
                .flatMapLatest { fileName ->
                    if (fileName == null) {
                        flowOf(emptyList())
                    } else {
                        combine(
                            repository.observePreferenceItems(fileName),
                            _itemSearchQuery.debounce(DebounceMillis),
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
                .collect { items ->
                    updateState { copy(preferenceItems = items) }
                }
        }
    }

    private fun observeAvailableTypes() {
        viewModelScope.launch {
            _selectedFileName
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
                .collect { types ->
                    updateState { copy(availableTypes = types) }
                }
        }
    }

    private fun observeTotalItemCount() {
        viewModelScope.launch {
            _selectedFileName
                .flatMapLatest { fileName ->
                    if (fileName == null) {
                        flowOf(0)
                    } else {
                        repository.observePreferenceItems(fileName).map { it.size }
                    }
                }
                .collect { count ->
                    updateState { copy(totalItemCount = count) }
                }
        }
    }
}

package com.azikar24.wormaceptor.feature.preferences.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.common.presentation.SearchDebounce
import com.azikar24.wormaceptor.domain.contracts.PreferencesRepository
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import com.azikar24.wormaceptor.feature.preferences.navigator.PreferencesNavigator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
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

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PreferencesViewModel(
    private val repository: PreferencesRepository,
    navigator: PreferencesNavigator,
) : BaseViewModel<PreferencesViewState, PreferencesViewEffect, PreferencesViewEvent, PreferencesNavigator>(
    PreferencesViewState(),
    navigator,
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
            is PreferencesViewEvent.List -> handleListEvent(event)
            is PreferencesViewEvent.Detail -> handleDetailEvent(event)
            is PreferencesViewEvent.Editor -> handleEditorEvent(event)
        }
    }

    private fun handleListEvent(event: PreferencesViewEvent.List) {
        when (event) {
            is PreferencesViewEvent.List.SearchToggled -> handleToggleFileSearch()
            is PreferencesViewEvent.List.SearchQueryChanged -> handleFileSearchQueryChanged(event.query)
            is PreferencesViewEvent.List.Selected -> {
                handleSelectFile(event.fileName)
                navigator.navigateToDetail()
            }
            is PreferencesViewEvent.List.SelectionCleared -> handleClearFileSelection()
            is PreferencesViewEvent.List.BackPressed -> {
                handleClearFileSelection()
                navigator.navigateBack()
            }
        }
    }

    private fun handleDetailEvent(event: PreferencesViewEvent.Detail) {
        when (event) {
            is PreferencesViewEvent.Detail.SearchQueryChanged -> handleItemSearchQueryChanged(event.query)
            is PreferencesViewEvent.Detail.TypeFilterChanged -> handleSetTypeFilter(event.typeName)
            is PreferencesViewEvent.Detail.FiltersCleared -> handleClearFilters()
            is PreferencesViewEvent.Detail.PreferenceSet -> handleSetPreference(event.key, event.value)
            is PreferencesViewEvent.Detail.PreferenceDeleted -> handleDeletePreference(event.key)
            is PreferencesViewEvent.Detail.FileCleared -> handleClearCurrentFile()
            is PreferencesViewEvent.Detail.PreferenceCreated -> handleSetPreference(event.key, event.value)
            is PreferencesViewEvent.Detail.EditSheetOpened -> updateState {
                copy(
                    editingItem = event.item,
                    showEditSheet = true,
                    editor = PreferenceEditorState.fromItem(event.item),
                )
            }
            is PreferencesViewEvent.Detail.EditSheetDismissed -> updateState {
                copy(
                    editingItem = null,
                    showEditSheet = false,
                    editor = PreferenceEditorState(),
                )
            }
            is PreferencesViewEvent.Detail.DeleteConfirmShown -> updateState {
                copy(showDeleteConfirmKey = event.key)
            }
            is PreferencesViewEvent.Detail.DeleteConfirmDismissed -> updateState {
                copy(showDeleteConfirmKey = null)
            }
            is PreferencesViewEvent.Detail.ClearConfirmShown -> updateState {
                copy(showClearConfirmDialog = true)
            }
            is PreferencesViewEvent.Detail.ClearConfirmDismissed -> updateState {
                copy(showClearConfirmDialog = false)
            }
        }
    }

    private fun handleEditorEvent(event: PreferencesViewEvent.Editor) {
        when (event) {
            is PreferencesViewEvent.Editor.KeyChanged -> updateEditor { copy(key = event.value) }
            is PreferencesViewEvent.Editor.TypeSelected -> updateEditor {
                copy(selectedType = event.type, typeDropdownExpanded = false)
            }
            is PreferencesViewEvent.Editor.TypeDropdownExpandedChanged -> updateEditor {
                copy(typeDropdownExpanded = event.expanded)
            }
            is PreferencesViewEvent.Editor.StringValueChanged -> updateEditor { copy(stringValue = event.value) }
            is PreferencesViewEvent.Editor.IntValueChanged -> updateEditor { copy(intValue = event.value) }
            is PreferencesViewEvent.Editor.LongValueChanged -> updateEditor { copy(longValue = event.value) }
            is PreferencesViewEvent.Editor.FloatValueChanged -> updateEditor { copy(floatValue = event.value) }
            is PreferencesViewEvent.Editor.BooleanValueChanged -> updateEditor { copy(booleanValue = event.value) }
            is PreferencesViewEvent.Editor.NewStringSetItemChanged -> updateEditor {
                copy(newStringSetItem = event.value)
            }
            is PreferencesViewEvent.Editor.AddStringSetItem -> updateEditor {
                if (newStringSetItem.isBlank()) {
                    this
                } else {
                    copy(
                        stringSetValues = (stringSetValues + newStringSetItem).toPersistentList(),
                        newStringSetItem = "",
                    )
                }
            }
            is PreferencesViewEvent.Editor.RemoveStringSetItem -> updateEditor {
                copy(
                    stringSetValues = stringSetValues
                        .toMutableList()
                        .apply { removeAt(event.index) }
                        .toPersistentList(),
                )
            }
            is PreferencesViewEvent.Editor.SaveRequested -> handleEditorSave()
            is PreferencesViewEvent.Editor.DeleteRequested -> handleEditorDelete()
        }
    }

    private fun updateEditor(reducer: PreferenceEditorState.() -> PreferenceEditorState) {
        updateState { copy(editor = editor.reducer()) }
    }

    private fun handleEditorSave() {
        val snapshot = uiState.value
        val editor = snapshot.editor
        if (!editor.canSave) return
        handleSetPreference(editor.key, editor.toPreferenceValue())
        sendEvent(PreferencesViewEvent.Detail.EditSheetDismissed)
    }

    private fun handleEditorDelete() {
        val editor = uiState.value.editor
        if (!editor.isEditing) return
        handleDeletePreference(editor.key)
        sendEvent(PreferencesViewEvent.Detail.EditSheetDismissed)
    }

    private fun handleToggleFileSearch() {
        val nowActive = !uiState.value.isFileSearchActive
        updateState { copy(isFileSearchActive = nowActive) }
        if (!nowActive) {
            _fileSearchQuery.value = ""
            updateState { copy(fileSearchQuery = "") }
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
                isItemsLoading = true,
                preferenceItems = persistentListOf(),
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
                isItemsLoading = false,
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
                _fileSearchQuery.debounce(SearchDebounce.DEFAULT),
            ) { files, query ->
                files.filter { file ->
                    query.isBlank() || file.name.contains(query, ignoreCase = true)
                }.sortedBy { it.name.lowercase() }.toImmutableList()
            }.flowOn(Dispatchers.Default)
                .collect { files ->
                    updateState { copy(preferenceFiles = files, isFilesLoading = false) }
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
                            _itemSearchQuery.debounce(SearchDebounce.DEFAULT),
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
                    updateState { copy(preferenceItems = items, isItemsLoading = false) }
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

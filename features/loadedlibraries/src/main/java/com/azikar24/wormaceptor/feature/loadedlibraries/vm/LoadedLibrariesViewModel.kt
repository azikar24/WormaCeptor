package com.azikar24.wormaceptor.feature.loadedlibraries.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.LoadedLibrariesEngine
import com.azikar24.wormaceptor.domain.entities.LibrarySummary
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary.LibraryType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Loaded Libraries Inspector screen.
 */
class LoadedLibrariesViewModel(
    private val engine: LoadedLibrariesEngine,
) : ViewModel() {

    // Filter by type
    private val _selectedType = MutableStateFlow<LibraryType?>(null)
    val selectedType: StateFlow<LibraryType?> = _selectedType.asStateFlow()

    // Filter by system/app
    private val _showSystemLibs = MutableStateFlow(true)
    val showSystemLibs: StateFlow<Boolean> = _showSystemLibs.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected library for detail view
    private val _selectedLibrary = MutableStateFlow<LoadedLibrary?>(null)
    val selectedLibrary: StateFlow<LoadedLibrary?> = _selectedLibrary.asStateFlow()

    // Loading state
    val isLoading: StateFlow<Boolean> = engine.isLoading

    // Error state
    val error: StateFlow<String?> = engine.error

    // Summary
    val summary: StateFlow<LibrarySummary> = engine.summary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibrarySummary.empty())

    // Filtered libraries
    val filteredLibraries: StateFlow<ImmutableList<LoadedLibrary>> =
        combine(engine.libraries, _selectedType, _showSystemLibs, _searchQuery) { libs, type, showSystem, query ->
            var filtered = libs
            if (type != null) filtered = filtered.filter { it.type == type }
            if (!showSystem) filtered = filtered.filter { !it.isSystemLibrary }
            if (query.isNotBlank()) {
                val lowerQuery = query.lowercase()
                filtered = filtered.filter {
                    it.name.lowercase().contains(
                        lowerQuery,
                    ) || it.path.lowercase().contains(lowerQuery)
                }
            }
            filtered.toImmutableList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    fun setSelectedType(type: LibraryType?) {
        _selectedType.value = type
    }
    fun setShowSystemLibs(show: Boolean) {
        _showSystemLibs.value = show
    }
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    fun selectLibrary(library: LoadedLibrary) {
        _selectedLibrary.value = library
    }
    fun dismissDetail() {
        _selectedLibrary.value = null
    }
    fun refresh() {
        engine.refresh()
    }
    fun exportAsText(): String = engine.exportAsText()
}

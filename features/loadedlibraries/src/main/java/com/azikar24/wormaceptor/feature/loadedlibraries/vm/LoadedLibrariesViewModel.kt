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

    private val _selectedType = MutableStateFlow<LibraryType?>(null)

    /** Currently selected library type filter, or null for all types. */
    val selectedType: StateFlow<LibraryType?> = _selectedType.asStateFlow()

    private val _showSystemLibs = MutableStateFlow(true)

    /** Whether system libraries are included in the displayed list. */
    val showSystemLibs: StateFlow<Boolean> = _showSystemLibs.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    /** Current text used to filter libraries by name or path. */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedLibrary = MutableStateFlow<LoadedLibrary?>(null)

    /** Library currently selected for detail view, or null if none. */
    val selectedLibrary: StateFlow<LoadedLibrary?> = _selectedLibrary.asStateFlow()

    /** Whether the engine is currently scanning for loaded libraries. */
    val isLoading: StateFlow<Boolean> = engine.isLoading

    /** Error message from the last scan, or null if none. */
    val error: StateFlow<String?> = engine.error

    /** Aggregate summary of all loaded libraries. */
    val summary: StateFlow<LibrarySummary> = engine.summary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibrarySummary.empty())

    /** Libraries filtered by type, system/app origin, and search query. */
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

    /** Filters the library list to show only the specified type, or all if null. */
    fun setSelectedType(type: LibraryType?) {
        _selectedType.value = type
    }

    /** Controls whether system libraries are included in the list. */
    fun setShowSystemLibs(show: Boolean) {
        _showSystemLibs.value = show
    }

    /** Updates the search query used to filter libraries by name or path. */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** Selects a library to show its details in the bottom sheet. */
    fun selectLibrary(library: LoadedLibrary) {
        _selectedLibrary.value = library
    }

    /** Closes the library detail bottom sheet. */
    fun dismissDetail() {
        _selectedLibrary.value = null
    }

    /** Re-scans the device for loaded libraries. */
    fun refresh() {
        engine.refresh()
    }

    /** Exports the current library list as a shareable plain-text summary. */
    fun exportAsText(): String = engine.exportAsText()
}

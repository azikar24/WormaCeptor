package com.azikar24.wormaceptor.feature.loadedlibraries.vm

import com.azikar24.wormaceptor.domain.entities.LibrarySummary
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LoadedLibrariesViewState(
    val isLibrariesLoading: Boolean = true,
    val filteredLibraries: ImmutableList<LoadedLibrary> = persistentListOf(),
    val summary: LibrarySummary = LibrarySummary.empty(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedType: LoadedLibrary.LibraryType? = null,
    val showSystemLibs: Boolean = true,
    val searchQuery: String = "",
    val searchActive: Boolean = false,
    val selectedLibrary: LoadedLibrary? = null,
)

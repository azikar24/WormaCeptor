package com.azikar24.wormaceptor.feature.loadedlibraries.vm

import com.azikar24.wormaceptor.domain.entities.LibrarySummary
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Consolidated UI state for the Loaded Libraries Inspector screen. */
data class LoadedLibrariesViewState(
    val filteredLibraries: ImmutableList<LoadedLibrary> = persistentListOf(),
    val summary: LibrarySummary = LibrarySummary.empty(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedType: LoadedLibrary.LibraryType? = null,
    val showSystemLibs: Boolean = true,
    val searchQuery: String = "",
    val selectedLibrary: LoadedLibrary? = null,
)

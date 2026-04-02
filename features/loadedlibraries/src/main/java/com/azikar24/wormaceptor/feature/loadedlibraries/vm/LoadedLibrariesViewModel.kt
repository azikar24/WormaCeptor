package com.azikar24.wormaceptor.feature.loadedlibraries.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.LoadedLibrariesEngine
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

/**
 * ViewModel for the Loaded Libraries Inspector screen.
 *
 * Extends [BaseViewModel] following MVI: all user actions arrive via [LoadedLibrariesViewEvent],
 * the single [uiState] drives recomposition, and one-time effects use [LoadedLibrariesViewEffect].
 */
class LoadedLibrariesViewModel(
    private val engine: LoadedLibrariesEngine,
) : BaseViewModel<LoadedLibrariesViewState, LoadedLibrariesViewEffect, LoadedLibrariesViewEvent>(
    initialState = LoadedLibrariesViewState(),
) {

    init {
        observeEngineState()
    }

    override fun handleEvent(event: LoadedLibrariesViewEvent) {
        when (event) {
            is LoadedLibrariesViewEvent.SetSelectedType -> {
                updateState { copy(selectedType = event.type) }
                refilter()
            }
            is LoadedLibrariesViewEvent.SetShowSystemLibs -> {
                updateState { copy(showSystemLibs = event.show) }
                refilter()
            }
            is LoadedLibrariesViewEvent.SetSearchQuery -> {
                updateState { copy(searchQuery = event.query) }
                refilter()
            }
            is LoadedLibrariesViewEvent.SelectLibrary -> {
                updateState { copy(selectedLibrary = event.library) }
            }
            is LoadedLibrariesViewEvent.DismissDetail -> {
                updateState { copy(selectedLibrary = null) }
            }
            is LoadedLibrariesViewEvent.Refresh -> {
                engine.refresh()
            }
        }
    }

    /** Exports the current library list as a shareable plain-text summary. */
    fun exportAsText(): String = engine.exportAsText()

    private fun observeEngineState() {
        combine(
            engine.libraries,
            engine.isLoading,
            engine.error,
            engine.summary,
        ) { libs, loading, err, summary ->
            updateState {
                copy(
                    filteredLibraries = filterLibraries(libs, selectedType, showSystemLibs, searchQuery),
                    isLoading = loading,
                    error = err,
                    summary = summary,
                )
            }
        }.launchIn(viewModelScope)
    }

    /** Re-applies filters against the engine's current library list. */
    private fun refilter() {
        val currentLibs = engine.libraries.value
        updateState {
            copy(filteredLibraries = filterLibraries(currentLibs, selectedType, showSystemLibs, searchQuery))
        }
    }

    private fun filterLibraries(
        libs: List<LoadedLibrary>,
        type: LoadedLibrary.LibraryType?,
        showSystem: Boolean,
        query: String,
    ) = libs.let { all ->
        var filtered = all
        if (type != null) filtered = filtered.filter { it.type == type }
        if (!showSystem) filtered = filtered.filter { !it.isSystemLibrary }
        if (query.isNotBlank()) {
            val lowerQuery = query.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(lowerQuery) ||
                    it.path.lowercase().contains(lowerQuery)
            }
        }
        filtered.toImmutableList()
    }
}

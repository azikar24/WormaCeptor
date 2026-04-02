package com.azikar24.wormaceptor.feature.dependenciesinspector.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.DependenciesInspectorEngine
import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.DependencyInfo
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

/**
 * ViewModel for the Dependencies Inspector screen.
 *
 * Extends [BaseViewModel] following MVI: all user actions arrive via [DependenciesInspectorViewEvent],
 * the single [uiState] drives recomposition, and one-time effects use [DependenciesInspectorViewEffect].
 */
class DependenciesInspectorViewModel(
    private val engine: DependenciesInspectorEngine,
) : BaseViewModel<DependenciesInspectorViewState, DependenciesInspectorViewEffect, DependenciesInspectorViewEvent>(
    initialState = DependenciesInspectorViewState(),
) {

    init {
        observeEngineState()
    }

    override fun handleEvent(event: DependenciesInspectorViewEvent) {
        when (event) {
            is DependenciesInspectorViewEvent.SetSelectedCategory -> {
                updateState { copy(selectedCategory = event.category) }
                refilter()
            }
            is DependenciesInspectorViewEvent.SetSearchQuery -> {
                updateState { copy(searchQuery = event.query) }
                refilter()
            }
            is DependenciesInspectorViewEvent.SetShowVersionedOnly -> {
                updateState { copy(showVersionedOnly = event.show) }
                refilter()
            }
            is DependenciesInspectorViewEvent.SelectDependency -> {
                updateState { copy(selectedDependency = event.dependency) }
            }
            is DependenciesInspectorViewEvent.DismissDetail -> {
                updateState { copy(selectedDependency = null) }
            }
            is DependenciesInspectorViewEvent.Refresh -> {
                engine.refresh()
            }
        }
    }

    /** Exports the current dependency list as a shareable plain-text summary. */
    fun exportAsText(): String = engine.exportAsText()

    private fun observeEngineState() {
        combine(
            engine.dependencies,
            engine.isLoading,
            engine.error,
            engine.summary,
        ) { deps, loading, err, summary ->
            updateState {
                copy(
                    filteredDependencies = filterDependencies(deps, selectedCategory, searchQuery, showVersionedOnly),
                    isLoading = loading,
                    error = err,
                    summary = summary,
                )
            }
        }.launchIn(viewModelScope)
    }

    /** Re-applies filters against the engine's current dependency list. */
    private fun refilter() {
        val currentDeps = engine.dependencies.value
        updateState {
            copy(
                filteredDependencies = filterDependencies(
                    currentDeps,
                    selectedCategory,
                    searchQuery,
                    showVersionedOnly,
                ),
            )
        }
    }

    private fun filterDependencies(
        deps: List<DependencyInfo>,
        category: DependencyCategory?,
        query: String,
        versionedOnly: Boolean,
    ) = deps.let { all ->
        var filtered = all

        if (category != null) {
            filtered = filtered.filter { it.category == category }
        }

        if (versionedOnly) {
            filtered = filtered.filter { it.version != null }
        }

        if (query.isNotBlank()) {
            val lowerQuery = query.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(lowerQuery) ||
                    it.packageName.lowercase().contains(lowerQuery) ||
                    it.groupId?.lowercase()?.contains(lowerQuery) == true ||
                    it.artifactId?.lowercase()?.contains(lowerQuery) == true
            }
        }

        filtered.toImmutableList()
    }
}

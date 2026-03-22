package com.azikar24.wormaceptor.feature.dependenciesinspector.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.DependenciesInspectorEngine
import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.DependencyInfo
import com.azikar24.wormaceptor.domain.entities.DependencySummary
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
 * ViewModel for the Dependencies Inspector screen.
 */
class DependenciesInspectorViewModel(
    private val engine: DependenciesInspectorEngine,
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<DependencyCategory?>(null)

    /** Currently selected dependency category filter, or null for all categories. */
    val selectedCategory: StateFlow<DependencyCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    /** Current text used to filter dependencies by name or package. */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showVersionedOnly = MutableStateFlow(false)

    /** When true, only dependencies with a detected version number are displayed. */
    val showVersionedOnly: StateFlow<Boolean> = _showVersionedOnly.asStateFlow()

    private val _selectedDependency = MutableStateFlow<DependencyInfo?>(null)

    /** Dependency currently selected for detail view, or null if none. */
    val selectedDependency: StateFlow<DependencyInfo?> = _selectedDependency.asStateFlow()

    /** Whether the engine is currently scanning for dependencies. */
    val isLoading: StateFlow<Boolean> = engine.isLoading

    /** Error message from the last scan, or null if none. */
    val error: StateFlow<String?> = engine.error

    /** Aggregate summary of all detected dependencies. */
    val summary: StateFlow<DependencySummary> = engine.summary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DependencySummary.empty())

    /** Dependencies filtered by category, search query, and version presence. */
    val filteredDependencies: StateFlow<ImmutableList<DependencyInfo>> =
        combine(
            engine.dependencies,
            _selectedCategory,
            _searchQuery,
            _showVersionedOnly,
        ) { deps, category, query, versionedOnly ->
            var filtered = deps

            // Filter by category
            if (category != null) {
                filtered = filtered.filter { it.category == category }
            }

            // Filter by version presence
            if (versionedOnly) {
                filtered = filtered.filter { it.version != null }
            }

            // Filter by search query
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
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    /** Filters dependencies to show only the specified category, or all if null. */
    fun setSelectedCategory(category: DependencyCategory?) {
        _selectedCategory.value = category
    }

    /** Updates the search query used to filter dependencies by name or package. */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** When true, only dependencies with a detected version number are shown. */
    fun setShowVersionedOnly(show: Boolean) {
        _showVersionedOnly.value = show
    }

    /** Selects a dependency to display its details in the bottom sheet. */
    fun selectDependency(dependency: DependencyInfo) {
        _selectedDependency.value = dependency
    }

    /** Closes the dependency detail bottom sheet. */
    fun dismissDetail() {
        _selectedDependency.value = null
    }

    /** Re-scans the classpath for application dependencies. */
    fun refresh() {
        engine.refresh()
    }

    /** Exports the current dependency list as a shareable plain-text summary. */
    fun exportAsText(): String = engine.exportAsText()
}

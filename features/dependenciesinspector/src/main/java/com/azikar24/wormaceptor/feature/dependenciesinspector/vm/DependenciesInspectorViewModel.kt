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

    // Filter by category
    private val _selectedCategory = MutableStateFlow<DependencyCategory?>(null)
    val selectedCategory: StateFlow<DependencyCategory?> = _selectedCategory.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Show only dependencies with detected versions
    private val _showVersionedOnly = MutableStateFlow(false)
    val showVersionedOnly: StateFlow<Boolean> = _showVersionedOnly.asStateFlow()

    // Selected dependency for detail view
    private val _selectedDependency = MutableStateFlow<DependencyInfo?>(null)
    val selectedDependency: StateFlow<DependencyInfo?> = _selectedDependency.asStateFlow()

    // Loading state
    val isLoading: StateFlow<Boolean> = engine.isLoading

    // Error state
    val error: StateFlow<String?> = engine.error

    // Summary
    val summary: StateFlow<DependencySummary> = engine.summary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DependencySummary.empty())

    // Filtered dependencies (WormaCeptor's internal deps are already excluded by the engine)
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

    fun setSelectedCategory(category: DependencyCategory?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setShowVersionedOnly(show: Boolean) {
        _showVersionedOnly.value = show
    }

    fun selectDependency(dependency: DependencyInfo) {
        _selectedDependency.value = dependency
    }

    fun dismissDetail() {
        _selectedDependency.value = null
    }

    fun refresh() {
        engine.refresh()
    }

    fun exportAsText(): String = engine.exportAsText()
}

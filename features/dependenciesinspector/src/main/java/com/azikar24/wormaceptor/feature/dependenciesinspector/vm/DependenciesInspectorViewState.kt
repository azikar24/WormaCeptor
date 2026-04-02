package com.azikar24.wormaceptor.feature.dependenciesinspector.vm

import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.DependencyInfo
import com.azikar24.wormaceptor.domain.entities.DependencySummary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Consolidated UI state for the Dependencies Inspector screen. */
data class DependenciesInspectorViewState(
    val filteredDependencies: ImmutableList<DependencyInfo> = persistentListOf(),
    val summary: DependencySummary = DependencySummary.empty(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: DependencyCategory? = null,
    val searchQuery: String = "",
    val showVersionedOnly: Boolean = false,
    val selectedDependency: DependencyInfo? = null,
)

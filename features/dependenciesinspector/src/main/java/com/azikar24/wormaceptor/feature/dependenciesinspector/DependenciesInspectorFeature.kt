package com.azikar24.wormaceptor.feature.dependenciesinspector

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.DependenciesInspectorEngine
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.DependenciesInspectorScreen
import com.azikar24.wormaceptor.feature.dependenciesinspector.vm.DependenciesInspectorViewModel

/**
 * Entry point for the Dependencies Inspector feature.
 *
 * This feature detects Java/Kotlin libraries present in the application
 * and displays their versions when available.
 */
object DependenciesInspectorFeature {

    /**
     * Creates a DependenciesInspectorViewModel factory.
     */
    fun createViewModelFactory(engine: DependenciesInspectorEngine): DependenciesInspectorViewModelFactory {
        return DependenciesInspectorViewModelFactory(engine)
    }
}

/**
 * Factory for creating DependenciesInspectorViewModel instances.
 */
class DependenciesInspectorViewModelFactory(
    private val engine: DependenciesInspectorEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DependenciesInspectorViewModel::class.java)) {
            return DependenciesInspectorViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Dependencies Inspector feature.
 */
@Composable
fun DependenciesInspector(
    engine: DependenciesInspectorEngine,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val factory = remember { DependenciesInspectorFeature.createViewModelFactory(engine) }
    val viewModel: DependenciesInspectorViewModel = viewModel(factory = factory)

    val dependencies by viewModel.filteredDependencies.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDependency by viewModel.selectedDependency.collectAsState()
    val showVersionedOnly by viewModel.showVersionedOnly.collectAsState()

    DependenciesInspectorScreen(
        dependencies = dependencies,
        summary = summary,
        isLoading = isLoading,
        error = error,
        selectedCategory = selectedCategory,
        searchQuery = searchQuery,
        selectedDependency = selectedDependency,
        showVersionedOnly = showVersionedOnly,
        onCategorySelected = viewModel::setSelectedCategory,
        onSearchQueryChanged = viewModel::setSearchQuery,
        onDependencySelected = viewModel::selectDependency,
        onDismissDetail = viewModel::dismissDetail,
        onShowVersionedOnlyChanged = viewModel::setShowVersionedOnly,
        onRefresh = viewModel::refresh,
        onBack = onNavigateBack,
        modifier = modifier,
    )
}

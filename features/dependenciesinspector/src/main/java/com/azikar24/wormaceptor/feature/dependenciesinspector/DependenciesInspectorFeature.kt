package com.azikar24.wormaceptor.feature.dependenciesinspector

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.DependenciesInspectorEngine
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.DependenciesInspectorScreen
import com.azikar24.wormaceptor.feature.dependenciesinspector.vm.DependenciesInspectorViewEvent
import com.azikar24.wormaceptor.feature.dependenciesinspector.vm.DependenciesInspectorViewModel
import org.koin.compose.koinInject

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
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: DependenciesInspectorEngine = koinInject()
    val factory = remember { DependenciesInspectorFeature.createViewModelFactory(engine) }
    val viewModel: DependenciesInspectorViewModel = viewModel(factory = factory)

    BaseScreen(viewModel = viewModel) { state, onEvent ->
        DependenciesInspectorScreen(
            dependencies = state.filteredDependencies,
            summary = state.summary,
            isLoading = state.isLoading,
            error = state.error,
            selectedCategory = state.selectedCategory,
            searchQuery = state.searchQuery,
            selectedDependency = state.selectedDependency,
            showVersionedOnly = state.showVersionedOnly,
            onCategorySelected = { onEvent(DependenciesInspectorViewEvent.SetSelectedCategory(it)) },
            onSearchQueryChanged = { onEvent(DependenciesInspectorViewEvent.SetSearchQuery(it)) },
            onDependencySelected = { onEvent(DependenciesInspectorViewEvent.SelectDependency(it)) },
            onDismissDetail = { onEvent(DependenciesInspectorViewEvent.DismissDetail) },
            onShowVersionedOnlyChanged = { onEvent(DependenciesInspectorViewEvent.SetShowVersionedOnly(it)) },
            onRefresh = { onEvent(DependenciesInspectorViewEvent.Refresh) },
            onBack = onNavigateBack,
            modifier = modifier,
        )
    }
}

package com.azikar24.wormaceptor.feature.loadedlibraries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.LoadedLibrariesEngine
import com.azikar24.wormaceptor.feature.loadedlibraries.ui.LoadedLibrariesScreen
import com.azikar24.wormaceptor.feature.loadedlibraries.vm.LoadedLibrariesViewEvent
import com.azikar24.wormaceptor.feature.loadedlibraries.vm.LoadedLibrariesViewModel
import org.koin.compose.koinInject

/**
 * Entry point for the Loaded Libraries Inspector feature.
 */
object LoadedLibrariesFeature {

    /**
     * Creates a LoadedLibrariesViewModel factory.
     */
    fun createViewModelFactory(engine: LoadedLibrariesEngine): LoadedLibrariesViewModelFactory {
        return LoadedLibrariesViewModelFactory(engine)
    }
}

/**
 * Factory for creating LoadedLibrariesViewModel instances.
 */
class LoadedLibrariesViewModelFactory(
    private val engine: LoadedLibrariesEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoadedLibrariesViewModel::class.java)) {
            return LoadedLibrariesViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Loaded Libraries Inspector feature.
 */
@Composable
fun LoadedLibrariesInspector(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: LoadedLibrariesEngine = koinInject()
    val factory = remember { LoadedLibrariesFeature.createViewModelFactory(engine) }
    val viewModel: LoadedLibrariesViewModel = viewModel(factory = factory)

    BaseScreen(viewModel = viewModel) { state, onEvent ->
        LoadedLibrariesScreen(
            libraries = state.filteredLibraries,
            summary = state.summary,
            isLoading = state.isLoading,
            error = state.error,
            selectedType = state.selectedType,
            showSystemLibs = state.showSystemLibs,
            searchQuery = state.searchQuery,
            selectedLibrary = state.selectedLibrary,
            onTypeSelected = { onEvent(LoadedLibrariesViewEvent.SetSelectedType(it)) },
            onShowSystemLibsChanged = { onEvent(LoadedLibrariesViewEvent.SetShowSystemLibs(it)) },
            onSearchQueryChanged = { onEvent(LoadedLibrariesViewEvent.SetSearchQuery(it)) },
            onLibrarySelected = { onEvent(LoadedLibrariesViewEvent.SelectLibrary(it)) },
            onDismissDetail = { onEvent(LoadedLibrariesViewEvent.DismissDetail) },
            onRefresh = { onEvent(LoadedLibrariesViewEvent.Refresh) },
            onBack = onNavigateBack,
            modifier = modifier,
        )
    }
}

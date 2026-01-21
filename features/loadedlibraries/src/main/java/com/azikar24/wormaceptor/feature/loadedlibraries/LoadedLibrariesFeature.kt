/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.loadedlibraries

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.LoadedLibrariesEngine
import com.azikar24.wormaceptor.feature.loadedlibraries.ui.LoadedLibrariesScreen
import com.azikar24.wormaceptor.feature.loadedlibraries.vm.LoadedLibrariesViewModel

/**
 * Entry point for the Loaded Libraries Inspector feature.
 */
object LoadedLibrariesFeature {

    /**
     * Creates a LoadedLibrariesEngine instance.
     */
    fun createEngine(context: Context): LoadedLibrariesEngine {
        return LoadedLibrariesEngine(context.applicationContext)
    }

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
    context: Context,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine = remember { LoadedLibrariesFeature.createEngine(context) }
    val factory = remember { LoadedLibrariesFeature.createViewModelFactory(engine) }
    val viewModel: LoadedLibrariesViewModel = viewModel(factory = factory)

    val libraries by viewModel.filteredLibraries.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val showSystemLibs by viewModel.showSystemLibs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedLibrary by viewModel.selectedLibrary.collectAsState()

    LoadedLibrariesScreen(
        libraries = libraries,
        summary = summary,
        isLoading = isLoading,
        error = error,
        selectedType = selectedType,
        showSystemLibs = showSystemLibs,
        searchQuery = searchQuery,
        selectedLibrary = selectedLibrary,
        onTypeSelected = viewModel::setSelectedType,
        onShowSystemLibsChanged = viewModel::setShowSystemLibs,
        onSearchQueryChanged = viewModel::setSearchQuery,
        onLibrarySelected = viewModel::selectLibrary,
        onDismissDetail = viewModel::dismissDetail,
        onRefresh = viewModel::refresh,
        onBack = onNavigateBack,
        modifier = modifier,
    )
}

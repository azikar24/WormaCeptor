package com.azikar24.wormaceptor.feature.filebrowser

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.domain.contracts.FileSystemRepository
import com.azikar24.wormaceptor.feature.filebrowser.data.FileSystemDataSource
import com.azikar24.wormaceptor.feature.filebrowser.data.FileSystemRepositoryImpl
import com.azikar24.wormaceptor.feature.filebrowser.ui.FileBrowserScreen
import com.azikar24.wormaceptor.feature.filebrowser.ui.FileInfoSheet
import com.azikar24.wormaceptor.feature.filebrowser.ui.FileViewerScreen
import com.azikar24.wormaceptor.feature.filebrowser.vm.FileBrowserViewEffect
import com.azikar24.wormaceptor.feature.filebrowser.vm.FileBrowserViewEvent
import com.azikar24.wormaceptor.feature.filebrowser.vm.FileBrowserViewModel
import kotlinx.coroutines.launch

/**
 * Entry point for the File Browser feature.
 * Provides a factory method and composable navigation.
 */
object FileBrowserFeature {

    /**
     * Creates a FileSystemRepository instance for the given context.
     * Use this in your dependency injection setup.
     */
    fun createRepository(context: Context): FileSystemRepository {
        val dataSource = FileSystemDataSource(context.applicationContext)
        return FileSystemRepositoryImpl(dataSource)
    }

    /**
     * Creates a FileBrowserViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(repository: FileSystemRepository): FileBrowserViewModelFactory {
        return FileBrowserViewModelFactory(repository)
    }
}

/**
 * Factory for creating FileBrowserViewModel instances.
 */
class FileBrowserViewModelFactory(
    private val repository: FileSystemRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FileBrowserViewModel::class.java)) {
            return FileBrowserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the File Browser feature.
 * Handles navigation between browser, viewer, and info screens.
 */
@Composable
fun FileBrowser(context: Context, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val repository = remember { FileBrowserFeature.createRepository(context) }
    val factory = remember { FileBrowserFeature.createViewModelFactory(repository) }
    val viewModel: FileBrowserViewModel = viewModel(factory = factory)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val state by viewModel.uiState.collectAsState()

    // Handle one-time effects
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                FileBrowserViewEffect.AtRoot -> onNavigateBack?.invoke()
                FileBrowserViewEffect.NavigatedBack -> { /* Navigation handled via state */ }
            }
        }
    }

    // Main browser screen or file viewer
    val selectedFile = state.selectedFile
    val fileContent = state.fileContent
    if (selectedFile != null && fileContent != null) {
        FileViewerScreen(
            filePath = selectedFile,
            content = fileContent,
            onBack = { viewModel.sendEvent(FileBrowserViewEvent.CloseFileViewer) },
            modifier = modifier,
        )
    } else {
        FileBrowserScreen(
            currentPath = state.currentPath,
            navigationStack = state.navigationStack,
            filteredFiles = state.filteredFiles,
            searchQuery = state.searchQuery,
            isLoading = state.isLoading,
            error = state.error,
            onSearchQueryChanged = { viewModel.sendEvent(FileBrowserViewEvent.SearchQueryChanged(it)) },
            onNavigateToBreadcrumb = { viewModel.sendEvent(FileBrowserViewEvent.NavigateToBreadcrumb(it)) },
            onFileClick = { file -> viewModel.sendEvent(FileBrowserViewEvent.FileClicked(file)) },
            onFileLongClick = { file -> viewModel.sendEvent(FileBrowserViewEvent.FileLongClicked(file)) },
            onSortModeChanged = { viewModel.sendEvent(FileBrowserViewEvent.SetSortMode(it)) },
            onNavigateBack = {
                viewModel.sendEvent(FileBrowserViewEvent.NavigateBack)
                state.navigationStack.isNotEmpty()
            },
            onExitBrowser = { onNavigateBack?.invoke() },
            onClearError = { viewModel.sendEvent(FileBrowserViewEvent.ClearError) },
            modifier = modifier,
        )
    }

    // File info bottom sheet
    state.fileInfo?.let { info ->
        FileInfoSheet(
            fileInfo = info,
            onDismiss = { viewModel.sendEvent(FileBrowserViewEvent.HideFileInfo) },
            onDelete = { viewModel.sendEvent(FileBrowserViewEvent.DeleteFile(it)) },
            onShowMessage = { message ->
                scope.launch { snackbarHostState.showSnackbar(message) }
            },
        )
    }
}

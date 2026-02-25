package com.azikar24.wormaceptor.feature.filebrowser

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
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
fun FileBrowser(
    context: Context,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val repository = remember { FileBrowserFeature.createRepository(context) }
    val factory = remember { FileBrowserFeature.createViewModelFactory(repository) }
    val viewModel: FileBrowserViewModel = viewModel(factory = factory)
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                FileBrowserViewEffect.AtRoot -> onNavigateBack?.invoke()
                FileBrowserViewEffect.NavigatedBack -> { /* Navigation handled via state */ }
            }
        },
    ) { state, onEvent ->
        // Main browser screen or file viewer
        val selectedFile = state.selectedFile
        val fileContent = state.fileContent
        if (selectedFile != null && fileContent != null) {
            FileViewerScreen(
                filePath = selectedFile,
                content = fileContent,
                onBack = { onEvent(FileBrowserViewEvent.CloseFileViewer) },
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
                onSearchQueryChanged = { onEvent(FileBrowserViewEvent.SearchQueryChanged(it)) },
                onNavigateToBreadcrumb = { onEvent(FileBrowserViewEvent.NavigateToBreadcrumb(it)) },
                onFileClick = { file -> onEvent(FileBrowserViewEvent.FileClicked(file)) },
                onFileLongClick = { file -> onEvent(FileBrowserViewEvent.FileLongClicked(file)) },
                onSortModeChanged = { onEvent(FileBrowserViewEvent.SetSortMode(it)) },
                onNavigateBack = {
                    onEvent(FileBrowserViewEvent.NavigateBack)
                    state.navigationStack.isNotEmpty()
                },
                onExitBrowser = { onNavigateBack?.invoke() },
                onClearError = { onEvent(FileBrowserViewEvent.ClearError) },
                modifier = modifier,
                snackBarHostState = snackBarHostState,
            )
        }

        // File info bottom sheet
        state.fileInfo?.let { info ->
            FileInfoSheet(
                fileInfo = info,
                onDismiss = { onEvent(FileBrowserViewEvent.HideFileInfo) },
                onDelete = { onEvent(FileBrowserViewEvent.DeleteFile(it)) },
                onShowMessage = { message ->
                    scope.launch { snackBarHostState.showSnackbar(message) }
                },
            )
        }
    }
}

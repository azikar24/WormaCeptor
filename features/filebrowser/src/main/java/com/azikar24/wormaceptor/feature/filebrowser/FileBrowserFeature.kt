/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.filebrowser

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.azikar24.wormaceptor.feature.filebrowser.vm.FileBrowserViewModel

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

    val currentPath by viewModel.currentPath.collectAsState()
    val navigationStack by viewModel.navigationStack.collectAsState()
    val filteredFiles by viewModel.filteredFiles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val fileContent by viewModel.fileContent.collectAsState()
    val fileInfo by viewModel.fileInfo.collectAsState()

    // Main browser screen or file viewer
    if (selectedFile != null && fileContent != null) {
        FileViewerScreen(
            filePath = selectedFile!!,
            content = fileContent!!,
            onBack = viewModel::closeFileViewer,
            modifier = modifier,
        )
    } else {
        FileBrowserScreen(
            currentPath = currentPath,
            navigationStack = navigationStack,
            filteredFiles = filteredFiles,
            searchQuery = searchQuery,
            isLoading = isLoading,
            error = error,
            onSearchQueryChanged = viewModel::onSearchQueryChanged,
            onNavigateToBreadcrumb = viewModel::navigateToBreadcrumb,
            onFileClick = { file ->
                if (file.isDirectory) {
                    viewModel.navigateToDirectory(file.path)
                } else {
                    viewModel.openFile(file.path)
                }
            },
            onFileLongClick = { file ->
                viewModel.showFileInfo(file.path)
            },
            onSortModeChanged = viewModel::setSortMode,
            onNavigateBack = viewModel::navigateBack,
            onExitBrowser = { onNavigateBack?.invoke() },
            onClearError = viewModel::clearError,
            modifier = modifier,
        )
    }

    // File info bottom sheet
    fileInfo?.let { info ->
        FileInfoSheet(
            fileInfo = info,
            onDismiss = viewModel::hideFileInfo,
            onDelete = viewModel::deleteFile,
        )
    }
}

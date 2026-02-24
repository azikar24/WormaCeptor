package com.azikar24.wormaceptor.feature.filebrowser.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.domain.contracts.FileSystemRepository
import com.azikar24.wormaceptor.domain.entities.FileEntry
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

/**
 * ViewModel for the File Browser feature.
 * Manages navigation stack, file listing, and file operations.
 */
class FileBrowserViewModel(
    private val repository: FileSystemRepository,
) : BaseViewModel<FileBrowserViewState, FileBrowserViewEffect, FileBrowserViewEvent>(FileBrowserViewState()) {

    init {
        handleLoadRootDirectories()
    }

    override fun handleEvent(event: FileBrowserViewEvent) {
        when (event) {
            FileBrowserViewEvent.LoadRootDirectories -> handleLoadRootDirectories()
            is FileBrowserViewEvent.NavigateToDirectory -> handleNavigateToDirectory(event.path)
            FileBrowserViewEvent.NavigateBack -> handleNavigateBack()
            is FileBrowserViewEvent.NavigateToBreadcrumb -> handleNavigateToBreadcrumb(event.index)
            is FileBrowserViewEvent.SearchQueryChanged -> handleSearchQueryChanged(event.query)
            is FileBrowserViewEvent.SetSortMode -> handleSetSortMode(event.mode)
            is FileBrowserViewEvent.FileClicked -> handleFileClicked(event.file)
            is FileBrowserViewEvent.FileLongClicked -> handleFileLongClicked(event.file)
            is FileBrowserViewEvent.DeleteFile -> handleDeleteFile(event.path)
            FileBrowserViewEvent.CloseFileViewer -> updateState { copy(selectedFile = null, fileContent = null) }
            FileBrowserViewEvent.HideFileInfo -> updateState { copy(fileInfo = null) }
            FileBrowserViewEvent.ClearError -> updateState { copy(error = null) }
        }
    }

    private fun handleLoadRootDirectories() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            try {
                val roots = repository.getRootDirectories()
                updateState {
                    copy(
                        isLoading = false,
                        filteredFiles = roots.toImmutableList(),
                        currentPath = null,
                        navigationStack = persistentListOf(),
                    )
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                updateState {
                    copy(isLoading = false, error = "Failed to load root directories: ${e.message}")
                }
            }
        }
    }

    private fun handleNavigateToDirectory(path: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null, searchQuery = "") }
            try {
                val fileList = repository.listFiles(path)
                val sorted = applySorting(fileList, uiState.value.sortMode)
                val newStack = uiState.value.navigationStack.toMutableList()
                newStack.add(path)
                updateState {
                    copy(
                        isLoading = false,
                        filteredFiles = sorted.toImmutableList(),
                        currentPath = path,
                        navigationStack = newStack.toImmutableList(),
                    )
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                updateState {
                    copy(isLoading = false, error = "Failed to list directory: ${e.message}")
                }
            }
        }
    }

    private fun handleNavigateBack() {
        val stack = uiState.value.navigationStack

        if (stack.isEmpty()) {
            emitEffect(FileBrowserViewEffect.AtRoot)
            return
        }

        if (stack.size == 1) {
            handleLoadRootDirectories()
            emitEffect(FileBrowserViewEffect.NavigatedBack)
            return
        }

        val newStack = stack.dropLast(1)
        updateState { copy(navigationStack = newStack.toImmutableList()) }

        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null, searchQuery = "") }
            try {
                val parentPath = newStack.last()
                val fileList = repository.listFiles(parentPath)
                val sorted = applySorting(fileList, uiState.value.sortMode)
                updateState {
                    copy(
                        isLoading = false,
                        filteredFiles = sorted.toImmutableList(),
                        currentPath = parentPath,
                    )
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                updateState {
                    copy(isLoading = false, error = "Failed to list directory: ${e.message}")
                }
            }
        }
        emitEffect(FileBrowserViewEffect.NavigatedBack)
    }

    private fun handleNavigateToBreadcrumb(index: Int) {
        val stack = uiState.value.navigationStack

        if (index < 0) {
            handleLoadRootDirectories()
            return
        }

        if (index >= stack.size) {
            return
        }

        val path = stack[index]
        val newStack = stack.take(index + 1)
        updateState { copy(navigationStack = newStack.toImmutableList()) }

        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null, searchQuery = "") }
            try {
                val fileList = repository.listFiles(path)
                val sorted = applySorting(fileList, uiState.value.sortMode)
                updateState {
                    copy(
                        isLoading = false,
                        filteredFiles = sorted.toImmutableList(),
                        currentPath = path,
                    )
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                updateState {
                    copy(isLoading = false, error = "Failed to list directory: ${e.message}")
                }
            }
        }
    }

    private fun handleSearchQueryChanged(query: String) {
        updateState { copy(searchQuery = query) }
        filterCurrentFiles(query)
    }

    private fun handleSetSortMode(mode: SortMode) {
        updateState { copy(sortMode = mode) }
        refreshCurrentDirectory()
    }

    private fun handleFileClicked(file: FileEntry) {
        if (file.isDirectory) {
            handleNavigateToDirectory(file.path)
        } else {
            handleOpenFile(file.path)
        }
    }

    private fun handleFileLongClicked(file: FileEntry) {
        handleShowFileInfo(file.path)
    }

    private fun handleOpenFile(path: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            try {
                val content = repository.readFile(path)
                updateState {
                    copy(isLoading = false, fileContent = content, selectedFile = path)
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                updateState {
                    copy(isLoading = false, error = "Failed to read file: ${e.message}")
                }
            }
        }
    }

    private fun handleShowFileInfo(path: String) {
        viewModelScope.launch {
            try {
                val info = repository.getFileInfo(path)
                updateState { copy(fileInfo = info) }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                updateState { copy(error = "Failed to get file info: ${e.message}") }
            }
        }
    }

    private fun handleDeleteFile(path: String) {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            try {
                val success = repository.deleteFile(path)
                if (success) {
                    refreshCurrentDirectory()
                } else {
                    updateState { copy(isLoading = false, error = "Failed to delete file") }
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                updateState {
                    copy(isLoading = false, error = "Failed to delete file: ${e.message}")
                }
            }
        }
    }

    private fun refreshCurrentDirectory() {
        val state = uiState.value
        val currentPath = state.currentPath
        if (currentPath != null) {
            viewModelScope.launch {
                updateState { copy(isLoading = true, error = null) }
                try {
                    val fileList = repository.listFiles(currentPath)
                    val sorted = applySorting(fileList, uiState.value.sortMode)
                    val filtered = filterFiles(sorted, uiState.value.searchQuery)
                    updateState {
                        copy(isLoading = false, filteredFiles = filtered.toImmutableList())
                    }
                } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                    updateState {
                        copy(isLoading = false, error = "Failed to list directory: ${e.message}")
                    }
                }
            }
        } else {
            handleLoadRootDirectories()
        }
    }

    private fun filterCurrentFiles(query: String) {
        // Re-fetch and filter from the current directory
        val currentPath = uiState.value.currentPath
        if (currentPath != null) {
            viewModelScope.launch {
                try {
                    val fileList = repository.listFiles(currentPath)
                    val sorted = applySorting(fileList, uiState.value.sortMode)
                    val filtered = filterFiles(sorted, query)
                    updateState { copy(filteredFiles = filtered.toImmutableList()) }
                } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                    updateState { copy(error = "Failed to filter files: ${e.message}") }
                }
            }
        } else {
            viewModelScope.launch {
                try {
                    val roots = repository.getRootDirectories()
                    val filtered = filterFiles(roots, query)
                    updateState { copy(filteredFiles = filtered.toImmutableList()) }
                } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                    updateState { copy(error = "Failed to filter files: ${e.message}") }
                }
            }
        }
    }

    private fun filterFiles(files: List<FileEntry>, query: String): List<FileEntry> {
        return if (query.isBlank()) {
            files
        } else {
            files.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    private fun applySorting(files: List<FileEntry>, sortMode: SortMode): List<FileEntry> {
        return when (sortMode) {
            SortMode.NAME -> files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            SortMode.SIZE -> files.sortedWith(compareBy({ !it.isDirectory }, { -it.sizeBytes }))
            SortMode.DATE -> files.sortedWith(compareBy({ !it.isDirectory }, { -it.lastModified }))
        }
    }
}

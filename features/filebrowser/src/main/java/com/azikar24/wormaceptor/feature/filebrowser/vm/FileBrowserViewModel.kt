/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.filebrowser.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.domain.contracts.FileSystemRepository
import com.azikar24.wormaceptor.domain.entities.FileContent
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.domain.entities.FileInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the File Browser feature.
 * Manages navigation stack, file listing, and file operations.
 */
class FileBrowserViewModel(
    private val repository: FileSystemRepository,
) : ViewModel() {

    // Navigation stack (list of paths)
    private val _navigationStack = MutableStateFlow<ImmutableList<String>>(persistentListOf())
    val navigationStack: StateFlow<ImmutableList<String>> = _navigationStack.asStateFlow()

    // Current directory path
    private val _currentPath = MutableStateFlow<String?>(null)
    val currentPath: StateFlow<String?> = _currentPath.asStateFlow()

    // Files in current directory
    private val _files = MutableStateFlow<ImmutableList<FileEntry>>(persistentListOf())

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered files based on search
    private val _filteredFiles = MutableStateFlow<ImmutableList<FileEntry>>(persistentListOf())
    val filteredFiles: StateFlow<ImmutableList<FileEntry>> = _filteredFiles.asStateFlow()

    // Sort mode
    private val _sortMode = MutableStateFlow(SortMode.NAME)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Selected file for viewing
    private val _selectedFile = MutableStateFlow<String?>(null)
    val selectedFile: StateFlow<String?> = _selectedFile.asStateFlow()

    // File content
    private val _fileContent = MutableStateFlow<FileContent?>(null)
    val fileContent: StateFlow<FileContent?> = _fileContent.asStateFlow()

    // File info for bottom sheet
    private val _fileInfo = MutableStateFlow<FileInfo?>(null)
    val fileInfo: StateFlow<FileInfo?> = _fileInfo.asStateFlow()

    init {
        loadRootDirectories()
    }

    /**
     * Loads the root directories as the initial view.
     */
    fun loadRootDirectories() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val roots = repository.getRootDirectories()
                _files.value = roots.toImmutableList()
                _filteredFiles.value = roots.toImmutableList()
                _currentPath.value = null
                _navigationStack.value = persistentListOf()
            } catch (e: Exception) {
                _error.value = "Failed to load root directories: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Navigates into a directory.
     */
    fun navigateToDirectory(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _searchQuery.value = ""
            try {
                val fileList = repository.listFiles(path)
                _files.value = applySorting(fileList).toImmutableList()
                _filteredFiles.value = _files.value
                _currentPath.value = path

                // Add to navigation stack
                val newStack = _navigationStack.value.toMutableList()
                newStack.add(path)
                _navigationStack.value = newStack.toImmutableList()
            } catch (e: Exception) {
                _error.value = "Failed to list directory: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Navigates back to parent directory or root.
     */
    fun navigateBack(): Boolean {
        val stack = _navigationStack.value

        if (stack.isEmpty()) {
            // Already at root
            return false
        }

        if (stack.size == 1) {
            // Go back to root
            loadRootDirectories()
            return true
        }

        // Go to parent in stack
        val newStack = stack.dropLast(1)
        _navigationStack.value = newStack.toImmutableList()

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _searchQuery.value = ""
            try {
                val parentPath = newStack.last()
                val fileList = repository.listFiles(parentPath)
                _files.value = applySorting(fileList).toImmutableList()
                _filteredFiles.value = _files.value
                _currentPath.value = parentPath
            } catch (e: Exception) {
                _error.value = "Failed to list directory: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
        return true
    }

    /**
     * Navigates to a specific breadcrumb level.
     */
    fun navigateToBreadcrumb(index: Int) {
        val stack = _navigationStack.value

        if (index < 0) {
            loadRootDirectories()
            return
        }

        if (index >= stack.size) {
            return
        }

        val path = stack[index]
        val newStack = stack.take(index + 1)
        _navigationStack.value = newStack.toImmutableList()

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _searchQuery.value = ""
            try {
                val fileList = repository.listFiles(path)
                _files.value = applySorting(fileList).toImmutableList()
                _filteredFiles.value = _files.value
                _currentPath.value = path
            } catch (e: Exception) {
                _error.value = "Failed to list directory: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates search query and filters files.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        filterFiles(query)
    }

    /**
     * Changes the sort mode.
     */
    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
        _files.value = applySorting(_files.value).toImmutableList()
        filterFiles(_searchQuery.value)
    }

    /**
     * Opens a file for viewing.
     */
    fun openFile(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val content = repository.readFile(path)
                _fileContent.value = content
                _selectedFile.value = path
            } catch (e: Exception) {
                _error.value = "Failed to read file: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Closes the file viewer.
     */
    fun closeFileViewer() {
        _selectedFile.value = null
        _fileContent.value = null
    }

    /**
     * Shows file info bottom sheet.
     */
    fun showFileInfo(path: String) {
        viewModelScope.launch {
            try {
                val info = repository.getFileInfo(path)
                _fileInfo.value = info
            } catch (e: Exception) {
                _error.value = "Failed to get file info: ${e.message}"
            }
        }
    }

    /**
     * Hides file info bottom sheet.
     */
    fun hideFileInfo() {
        _fileInfo.value = null
    }

    /**
     * Deletes a file.
     */
    fun deleteFile(path: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val success = repository.deleteFile(path)
                if (success) {
                    // Refresh current directory
                    _currentPath.value?.let { currentPath ->
                        val fileList = repository.listFiles(currentPath)
                        _files.value = applySorting(fileList).toImmutableList()
                        filterFiles(_searchQuery.value)
                    } ?: loadRootDirectories()
                } else {
                    _error.value = "Failed to delete file"
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete file: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clears error message.
     */
    fun clearError() {
        _error.value = null
    }

    private fun filterFiles(query: String) {
        _filteredFiles.value = if (query.isBlank()) {
            _files.value
        } else {
            _files.value
                .filter { it.name.contains(query, ignoreCase = true) }
                .toImmutableList()
        }
    }

    private fun applySorting(files: List<FileEntry>): List<FileEntry> {
        return when (_sortMode.value) {
            SortMode.NAME -> files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            SortMode.SIZE -> files.sortedWith(compareBy({ !it.isDirectory }, { -it.sizeBytes }))
            SortMode.DATE -> files.sortedWith(compareBy({ !it.isDirectory }, { -it.lastModified }))
        }
    }

    enum class SortMode {
        NAME, SIZE, DATE
    }
}

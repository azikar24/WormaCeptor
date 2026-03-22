package com.azikar24.wormaceptor.feature.filebrowser.vm

import com.azikar24.wormaceptor.domain.entities.FileContent
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.domain.entities.FileInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Consolidated UI state for the file browser screen.
 *
 * @property navigationStack Stack of directory paths representing the browsing history.
 * @property currentPath Absolute path of the directory currently displayed, or null at root.
 * @property filteredFiles Files in the current directory after applying search and sort filters.
 * @property searchQuery Current text used to filter files by name.
 * @property sortMode Active sort order for the file listing.
 * @property isLoading Whether a directory listing or file read is in progress.
 * @property error Error message to display, or null if none.
 * @property selectedFile Path of the file currently open in the viewer, or null if none.
 * @property fileContent Parsed content of the selected file, or null if not loaded.
 * @property fileInfo Metadata for the file shown in the info bottom sheet, or null if hidden.
 */
data class FileBrowserViewState(
    val navigationStack: ImmutableList<String> = persistentListOf(),
    val currentPath: String? = null,
    val filteredFiles: ImmutableList<FileEntry> = persistentListOf(),
    val searchQuery: String = "",
    val sortMode: SortMode = SortMode.NAME,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFile: String? = null,
    val fileContent: FileContent? = null,
    val fileInfo: FileInfo? = null,
)

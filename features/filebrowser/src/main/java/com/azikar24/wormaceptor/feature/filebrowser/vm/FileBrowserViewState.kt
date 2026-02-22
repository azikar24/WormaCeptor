package com.azikar24.wormaceptor.feature.filebrowser.vm

import com.azikar24.wormaceptor.domain.entities.FileContent
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.domain.entities.FileInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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

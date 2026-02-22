package com.azikar24.wormaceptor.feature.filebrowser.vm

import com.azikar24.wormaceptor.domain.entities.FileEntry

sealed class FileBrowserViewEvent {
    data object LoadRootDirectories : FileBrowserViewEvent()
    data class NavigateToDirectory(val path: String) : FileBrowserViewEvent()
    data object NavigateBack : FileBrowserViewEvent()
    data class NavigateToBreadcrumb(val index: Int) : FileBrowserViewEvent()
    data class SearchQueryChanged(val query: String) : FileBrowserViewEvent()
    data class SetSortMode(val mode: SortMode) : FileBrowserViewEvent()
    data class FileClicked(val file: FileEntry) : FileBrowserViewEvent()
    data class FileLongClicked(val file: FileEntry) : FileBrowserViewEvent()
    data class DeleteFile(val path: String) : FileBrowserViewEvent()
    data object CloseFileViewer : FileBrowserViewEvent()
    data object HideFileInfo : FileBrowserViewEvent()
    data object ClearError : FileBrowserViewEvent()
}

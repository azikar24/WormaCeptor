package com.azikar24.wormaceptor.feature.filebrowser.vm

import com.azikar24.wormaceptor.domain.entities.FileEntry

/** User-initiated events dispatched to the file browser ViewModel. */
sealed class FileBrowserViewEvent {
    /** Requests loading the top-level root directories. */
    data object LoadRootDirectories : FileBrowserViewEvent()

    /**
     * Navigates into the directory at the given path.
     *
     * @property path Absolute path of the directory to open.
     */
    data class NavigateToDirectory(val path: String) : FileBrowserViewEvent()

    /** Navigates up one level in the directory stack. */
    data object NavigateBack : FileBrowserViewEvent()

    /**
     * Navigates to a specific position in the breadcrumb trail.
     *
     * @property index Zero-based index in the breadcrumb path segments.
     */
    data class NavigateToBreadcrumb(val index: Int) : FileBrowserViewEvent()

    /**
     * Updates the search query used to filter files in the current directory.
     *
     * @property query Text to match against file names.
     */
    data class SearchQueryChanged(val query: String) : FileBrowserViewEvent()

    /**
     * Changes the sort order of the file listing.
     *
     * @property mode New sort order to apply.
     */
    data class SetSortMode(val mode: SortMode) : FileBrowserViewEvent()

    /**
     * Handles a tap on a file or directory entry.
     *
     * @property file Entry that was tapped.
     */
    data class FileClicked(val file: FileEntry) : FileBrowserViewEvent()

    /**
     * Handles a long-press on a file to show its details.
     *
     * @property file Entry that was long-pressed.
     */
    data class FileLongClicked(val file: FileEntry) : FileBrowserViewEvent()

    /**
     * Deletes the file at the given path.
     *
     * @property path Absolute path of the file to delete.
     */
    data class DeleteFile(val path: String) : FileBrowserViewEvent()

    /** Closes the file content viewer overlay. */
    data object CloseFileViewer : FileBrowserViewEvent()

    /** Dismisses the file info bottom sheet. */
    data object HideFileInfo : FileBrowserViewEvent()

    /** Dismisses the current error message. */
    data object ClearError : FileBrowserViewEvent()
}

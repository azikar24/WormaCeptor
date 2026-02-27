package com.azikar24.wormaceptor.feature.filebrowser.vm

/** One-time side-effects emitted by the file browser ViewModel and consumed by the UI. */
sealed class FileBrowserViewEffect {
    /** The user successfully navigated back one directory. */
    data object NavigatedBack : FileBrowserViewEffect()

    /** The user attempted to navigate back but is already at the root. */
    data object AtRoot : FileBrowserViewEffect()
}

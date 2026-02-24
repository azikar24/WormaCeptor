package com.azikar24.wormaceptor.feature.filebrowser.vm

sealed class FileBrowserViewEffect {
    data object NavigatedBack : FileBrowserViewEffect()
    data object AtRoot : FileBrowserViewEffect()
}

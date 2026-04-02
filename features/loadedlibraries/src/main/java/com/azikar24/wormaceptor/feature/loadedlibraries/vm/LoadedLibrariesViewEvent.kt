package com.azikar24.wormaceptor.feature.loadedlibraries.vm

import com.azikar24.wormaceptor.domain.entities.LoadedLibrary

/** User actions dispatched from the Loaded Libraries Inspector UI. */
sealed class LoadedLibrariesViewEvent {
    data class SetSelectedType(val type: LoadedLibrary.LibraryType?) : LoadedLibrariesViewEvent()
    data class SetShowSystemLibs(val show: Boolean) : LoadedLibrariesViewEvent()
    data class SetSearchQuery(val query: String) : LoadedLibrariesViewEvent()
    data class SelectLibrary(val library: LoadedLibrary) : LoadedLibrariesViewEvent()
    data object DismissDetail : LoadedLibrariesViewEvent()
    data object Refresh : LoadedLibrariesViewEvent()
}

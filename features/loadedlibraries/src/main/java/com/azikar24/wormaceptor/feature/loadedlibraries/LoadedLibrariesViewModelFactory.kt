package com.azikar24.wormaceptor.feature.loadedlibraries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.LoadedLibrariesEngine
import com.azikar24.wormaceptor.feature.loadedlibraries.vm.LoadedLibrariesViewModel

class LoadedLibrariesViewModelFactory(
    private val engine: LoadedLibrariesEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoadedLibrariesViewModel::class.java)) {
            return LoadedLibrariesViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

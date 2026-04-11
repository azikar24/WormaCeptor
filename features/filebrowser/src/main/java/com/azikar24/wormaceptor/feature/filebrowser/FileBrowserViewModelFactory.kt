package com.azikar24.wormaceptor.feature.filebrowser

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.domain.contracts.FileSystemRepository
import com.azikar24.wormaceptor.feature.filebrowser.vm.FileBrowserViewModel

class FileBrowserViewModelFactory(
    private val repository: FileSystemRepository,
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FileBrowserViewModel::class.java)) {
            return FileBrowserViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

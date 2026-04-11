package com.azikar24.wormaceptor.feature.database

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.domain.contracts.DatabaseRepository
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewModel

class DatabaseViewModelFactory(
    private val repository: DatabaseRepository,
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DatabaseViewModel::class.java)) {
            return DatabaseViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

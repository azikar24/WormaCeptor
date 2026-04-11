package com.azikar24.wormaceptor.feature.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.domain.contracts.PreferencesRepository
import com.azikar24.wormaceptor.feature.preferences.vm.PreferencesViewModel

class PreferencesViewModelFactory(
    private val repository: PreferencesRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreferencesViewModel::class.java)) {
            return PreferencesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

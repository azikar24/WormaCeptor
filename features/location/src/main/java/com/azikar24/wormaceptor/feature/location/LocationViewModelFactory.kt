package com.azikar24.wormaceptor.feature.location

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.LocationSimulatorEngine
import com.azikar24.wormaceptor.domain.contracts.LocationSimulatorRepository
import com.azikar24.wormaceptor.feature.location.vm.LocationViewModel

class LocationViewModelFactory(
    private val repository: LocationSimulatorRepository,
    private val engine: LocationSimulatorEngine,
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(repository, engine, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

package com.azikar24.wormaceptor.feature.pushsimulator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.PushSimulatorEngine
import com.azikar24.wormaceptor.domain.contracts.PushSimulatorRepository
import com.azikar24.wormaceptor.feature.pushsimulator.vm.PushSimulatorViewModel

class PushSimulatorViewModelFactory(
    private val repository: PushSimulatorRepository,
    private val engine: PushSimulatorEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PushSimulatorViewModel::class.java)) {
            return PushSimulatorViewModel(repository, engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

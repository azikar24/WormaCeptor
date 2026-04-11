package com.azikar24.wormaceptor.feature.fps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.FpsMonitorEngine
import com.azikar24.wormaceptor.feature.fps.vm.FpsViewModel

class FpsViewModelFactory(
    private val engine: FpsMonitorEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FpsViewModel::class.java)) {
            return FpsViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

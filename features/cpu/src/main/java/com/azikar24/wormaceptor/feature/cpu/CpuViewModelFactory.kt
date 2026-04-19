package com.azikar24.wormaceptor.feature.cpu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import com.azikar24.wormaceptor.feature.cpu.vm.CpuViewModel

/** Factory for creating [CpuViewModel] instances with the required [CpuMonitorEngine]. */
class CpuViewModelFactory(
    private val engine: CpuMonitorEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CpuViewModel::class.java)) {
            return CpuViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

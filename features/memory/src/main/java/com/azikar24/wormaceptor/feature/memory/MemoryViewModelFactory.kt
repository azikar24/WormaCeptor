package com.azikar24.wormaceptor.feature.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.feature.memory.vm.MemoryViewModel

class MemoryViewModelFactory(
    private val engine: MemoryMonitorEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoryViewModel::class.java)) {
            return MemoryViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

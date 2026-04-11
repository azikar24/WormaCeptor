package com.azikar24.wormaceptor.feature.leakdetection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.LeakDetectionEngine
import com.azikar24.wormaceptor.feature.leakdetection.vm.LeakDetectionViewModel

class LeakDetectionViewModelFactory(
    private val engine: LeakDetectionEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeakDetectionViewModel::class.java)) {
            return LeakDetectionViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

package com.azikar24.wormaceptor.feature.threadviolation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.ThreadViolationEngine
import com.azikar24.wormaceptor.feature.threadviolation.vm.ThreadViolationViewModel

class ThreadViolationViewModelFactory(
    private val engine: ThreadViolationEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThreadViolationViewModel::class.java)) {
            return ThreadViolationViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

package com.azikar24.wormaceptor.feature.dependenciesinspector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.DependenciesInspectorEngine
import com.azikar24.wormaceptor.feature.dependenciesinspector.vm.DependenciesInspectorViewModel

/** Factory for creating [DependenciesInspectorViewModel] with the required [DependenciesInspectorEngine]. */
class DependenciesInspectorViewModelFactory(
    private val engine: DependenciesInspectorEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DependenciesInspectorViewModel::class.java)) {
            return DependenciesInspectorViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

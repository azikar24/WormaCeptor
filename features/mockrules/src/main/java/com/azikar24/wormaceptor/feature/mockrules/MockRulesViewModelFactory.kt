package com.azikar24.wormaceptor.feature.mockrules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.MockEngine
import com.azikar24.wormaceptor.domain.contracts.MockRuleRepository
import com.azikar24.wormaceptor.feature.mockrules.vm.MockRulesViewModel

internal class MockRulesViewModelFactory(
    private val repository: MockRuleRepository,
    private val engine: MockEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MockRulesViewModel::class.java)) {
            return MockRulesViewModel(repository, engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

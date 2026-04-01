package com.azikar24.wormaceptor.feature.mockrules.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.MockEngine
import com.azikar24.wormaceptor.domain.contracts.MockRuleRepository
import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for the Mock Rules list and management screens.
 *
 * Coordinates between [MockRuleRepository] for persistence and [MockEngine] for
 * runtime rule evaluation.
 */
internal class MockRulesViewModel(
    private val repository: MockRuleRepository,
    private val engine: MockEngine,
) : BaseViewModel<MockRulesViewState, MockRulesEffect, MockRulesEvent>(
    initialState = MockRulesViewState(),
) {

    init {
        repository.getAll()
            .distinctUntilChanged()
            .onEach { rules -> engine.setRules(rules) }
            .launchIn(viewModelScope)

        combine(
            repository.getAll(),
            engine.mockingEnabled,
        ) { rules, mockingEnabled ->
            updateState {
                copy(
                    rules = rules.toImmutableList(),
                    mockingEnabled = mockingEnabled,
                    isLoading = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun handleEvent(event: MockRulesEvent) {
        when (event) {
            is MockRulesEvent.ToggleMocking -> engine.setMockingEnabled(!engine.mockingEnabled.value)
            is MockRulesEvent.ToggleRule -> viewModelScope.launch {
                val rule = repository.getById(event.ruleId) ?: return@launch
                repository.update(rule.copy(enabled = !rule.enabled))
            }
            is MockRulesEvent.DeleteRule -> viewModelScope.launch {
                repository.delete(event.ruleId)
            }
            is MockRulesEvent.DeleteAllRules -> viewModelScope.launch {
                repository.deleteAll()
                engine.resetCounters()
            }
            is MockRulesEvent.SaveRule -> viewModelScope.launch {
                val existing = repository.getById(event.rule.id)
                if (existing != null) {
                    repository.update(event.rule)
                } else {
                    repository.insert(event.rule)
                }
            }
        }
    }

    /** Returns a rule by ID for editing. */
    suspend fun getRuleById(ruleId: String): MockRule? {
        return repository.getById(ruleId)
    }
}

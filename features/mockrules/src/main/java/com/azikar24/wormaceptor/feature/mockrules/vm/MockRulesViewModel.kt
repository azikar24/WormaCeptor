package com.azikar24.wormaceptor.feature.mockrules.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.MockEngine
import com.azikar24.wormaceptor.domain.contracts.MockRuleRepository
import com.azikar24.wormaceptor.domain.entities.mock.MockDelay
import com.azikar24.wormaceptor.domain.entities.mock.MockResponse
import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import com.azikar24.wormaceptor.domain.entities.mock.RequestMatcher
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class MockRulesViewModel(
    private val repository: MockRuleRepository,
    private val engine: MockEngine,
) : BaseViewModel<MockRulesViewState, MockRulesEffect, MockRulesEvent>(
    initialState = MockRulesViewState(),
) {

    private var existingRule: MockRule? = null

    init {
        repository.getAll()
            .distinctUntilChanged()
            .onEach { rules -> engine.setRules(rules) }
            .combine(engine.mockingEnabled) { rules, mockingEnabled ->
                updateState {
                    copy(
                        rules = rules.toImmutableList(),
                        mockingEnabled = mockingEnabled,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleEvent(event: MockRulesEvent) {
        when (event) {
            is MockRulesEvent.List -> handleListEvent(event)
            is MockRulesEvent.Editor -> handleEditorEvent(event)
        }
    }

    private fun handleListEvent(event: MockRulesEvent.List) {
        when (event) {
            is MockRulesEvent.List.ToggleMocking ->
                engine.setMockingEnabled(!engine.mockingEnabled.value)

            is MockRulesEvent.List.ToggleRule -> viewModelScope.launch {
                val rule = repository.getById(event.ruleId) ?: return@launch
                repository.update(rule.copy(enabled = !rule.enabled))
            }

            is MockRulesEvent.List.DeleteRule -> viewModelScope.launch {
                repository.delete(event.ruleId)
            }

            is MockRulesEvent.List.DeleteAllRules -> viewModelScope.launch {
                repository.deleteAll()
                engine.resetCounters()
            }
        }
    }

    private fun handleEditorEvent(event: MockRulesEvent.Editor) {
        when (event) {
            is MockRulesEvent.Editor.LoadRule -> loadRule(event.ruleId)

            is MockRulesEvent.Editor.SaveRule -> viewModelScope.launch {
                val rule = buildRule()
                val existing = repository.getById(rule.id)
                if (existing != null) {
                    repository.update(rule)
                } else {
                    repository.insert(rule)
                }
                existingRule = null
                updateState { copy(editor = EditorState()) }
                emitEffect(MockRulesEffect.NavigateBack)
            }

            is MockRulesEvent.Editor.NameChanged ->
                updateEditor { copy(name = event.value) }
            is MockRulesEvent.Editor.UrlPatternChanged ->
                updateEditor { copy(urlPattern = event.value) }
            is MockRulesEvent.Editor.MatchTypeChanged ->
                updateEditor { copy(matchType = event.value) }
            is MockRulesEvent.Editor.MethodChanged ->
                updateEditor { copy(method = event.value) }
            is MockRulesEvent.Editor.MethodDropdownExpandedChanged ->
                updateEditor { copy(methodDropdownExpanded = event.expanded) }
            is MockRulesEvent.Editor.StatusCodeChanged ->
                updateEditor { copy(statusCode = event.value.toIntOrNull() ?: statusCode) }
            is MockRulesEvent.Editor.StatusMessageChanged ->
                updateEditor { copy(statusMessage = event.value) }
            is MockRulesEvent.Editor.ContentTypeChanged ->
                updateEditor { copy(contentType = event.value) }
            is MockRulesEvent.Editor.ResponseBodyChanged ->
                updateEditor { copy(responseBody = event.value) }
            is MockRulesEvent.Editor.DelayTypeChanged ->
                updateEditor { copy(delayType = event.value) }
            is MockRulesEvent.Editor.DelayMsChanged ->
                updateEditor { copy(delayMs = event.value) }
            is MockRulesEvent.Editor.DelayMinMsChanged ->
                updateEditor { copy(delayMinMs = event.value) }
            is MockRulesEvent.Editor.DelayMaxMsChanged ->
                updateEditor { copy(delayMaxMs = event.value) }
        }
    }

    private fun loadRule(ruleId: String?) {
        if (ruleId == null || ruleId == "new") {
            existingRule = null
            updateState { copy(editor = EditorState(isLoaded = true)) }
            return
        }
        viewModelScope.launch {
            val rule = repository.getById(ruleId)
            existingRule = rule
            if (rule != null) {
                updateState {
                    copy(
                        editor = EditorState(
                            name = rule.name,
                            urlPattern = rule.matcher.urlPattern,
                            matchType = rule.matcher.matchType,
                            method = rule.matcher.method.orEmpty(),
                            statusCode = rule.response.statusCode,
                            statusMessage = rule.response.statusMessage,
                            contentType = rule.response.contentType,
                            responseBody = rule.response.body.orEmpty(),
                            delayType = when (rule.delay) {
                                is MockDelay.Fixed -> DelayType.FIXED
                                is MockDelay.Range -> DelayType.RANGE
                                else -> DelayType.NONE
                            },
                            delayMs = when (val d = rule.delay) {
                                is MockDelay.Fixed -> d.ms.toString()
                                else -> "0"
                            },
                            delayMinMs = when (val d = rule.delay) {
                                is MockDelay.Range -> d.minMs.toString()
                                else -> "0"
                            },
                            delayMaxMs = when (val d = rule.delay) {
                                is MockDelay.Range -> d.maxMs.toString()
                                else -> "1000"
                            },
                            isEditing = true,
                            isLoaded = true,
                        ),
                    )
                }
            } else {
                updateState { copy(editor = EditorState(isLoaded = true)) }
            }
        }
    }

    private fun updateEditor(reducer: EditorState.() -> EditorState) {
        updateState { copy(editor = editor.reducer()) }
    }

    private fun buildRule(): MockRule {
        val s = uiState.value.editor
        val delay = when (s.delayType) {
            DelayType.FIXED -> MockDelay.Fixed(ms = s.delayMs.toLongOrNull() ?: 0L)
            DelayType.RANGE -> MockDelay.Range(
                minMs = s.delayMinMs.toLongOrNull() ?: 0L,
                maxMs = s.delayMaxMs.toLongOrNull() ?: 1000L,
            )
            DelayType.NONE -> MockDelay.None
        }
        val matcher = RequestMatcher(
            urlPattern = s.urlPattern.trim(),
            matchType = s.matchType,
            method = s.method.takeIf { it.isNotBlank() },
        )
        val response = MockResponse(
            statusCode = s.statusCode,
            statusMessage = s.statusMessage,
            contentType = s.contentType,
            body = s.responseBody.takeIf { it.isNotBlank() },
        )
        return existingRule?.copy(
            name = s.name.trim(),
            matcher = matcher,
            response = response,
            delay = delay,
        ) ?: MockRule(
            name = s.name.trim(),
            matcher = matcher,
            response = response,
            delay = delay,
        )
    }
}

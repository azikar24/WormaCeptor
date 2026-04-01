package com.azikar24.wormaceptor.feature.mockrules.vm

import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.domain.entities.mock.MockDelay
import com.azikar24.wormaceptor.domain.entities.mock.MockResponse
import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import com.azikar24.wormaceptor.domain.entities.mock.RequestMatcher

internal class MockRuleEditorViewModel : BaseViewModel<MockRuleEditorState, MockRuleEditorEffect, MockRuleEditorEvent>(
    initialState = MockRuleEditorState(),
) {

    private var existingRule: MockRule? = null

    fun initialize(rule: MockRule?) {
        existingRule = rule
        if (rule != null) {
            updateState {
                copy(
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
                )
            }
        }
    }

    override fun handleEvent(event: MockRuleEditorEvent) {
        when (event) {
            is MockRuleEditorEvent.NameChanged -> updateState { copy(name = event.value) }
            is MockRuleEditorEvent.UrlPatternChanged -> updateState { copy(urlPattern = event.value) }
            is MockRuleEditorEvent.MatchTypeChanged -> updateState { copy(matchType = event.value) }
            is MockRuleEditorEvent.MethodChanged -> updateState { copy(method = event.value) }
            is MockRuleEditorEvent.MethodDropdownExpandedChanged -> updateState {
                copy(
                    methodDropdownExpanded = event.expanded,
                )
            }
            is MockRuleEditorEvent.StatusCodeChanged -> updateState {
                copy(
                    statusCode = event.value.toIntOrNull() ?: statusCode,
                )
            }
            is MockRuleEditorEvent.StatusMessageChanged -> updateState { copy(statusMessage = event.value) }
            is MockRuleEditorEvent.ContentTypeChanged -> updateState { copy(contentType = event.value) }
            is MockRuleEditorEvent.ResponseBodyChanged -> updateState { copy(responseBody = event.value) }
            is MockRuleEditorEvent.DelayTypeChanged -> updateState { copy(delayType = event.value) }
            is MockRuleEditorEvent.DelayMsChanged -> updateState { copy(delayMs = event.value) }
            is MockRuleEditorEvent.DelayMinMsChanged -> updateState { copy(delayMinMs = event.value) }
            is MockRuleEditorEvent.DelayMaxMsChanged -> updateState { copy(delayMaxMs = event.value) }
        }
    }

    fun buildRule(): MockRule {
        val s = uiState.value
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

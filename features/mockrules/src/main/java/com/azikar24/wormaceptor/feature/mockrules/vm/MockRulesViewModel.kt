package com.azikar24.wormaceptor.feature.mockrules.vm

import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.common.presentation.NoOpNavigator
import com.azikar24.wormaceptor.core.engine.MockEngine
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.domain.contracts.MockRuleRepository
import com.azikar24.wormaceptor.domain.entities.mock.MockDelay
import com.azikar24.wormaceptor.domain.entities.mock.MockResponse
import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import com.azikar24.wormaceptor.domain.entities.mock.RequestMatcher
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.util.UUID

internal class MockRulesViewModel(
    private val repository: MockRuleRepository,
    private val engine: MockEngine,
    private val queryEngine: QueryEngine?,
) : BaseViewModel<MockRulesViewState, MockRulesEffect, MockRulesViewEvent, NoOpNavigator>(
    initialState = MockRulesViewState(),
    navigator = NoOpNavigator,
) {

    private var existingRule: MockRule? = null
    private var loadedKey: String? = null

    init {
        repository.getAll()
            .distinctUntilChanged()
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

    override fun handleEvent(event: MockRulesViewEvent) {
        when (event) {
            is MockRulesViewEvent.List -> handleListEvent(event)
            is MockRulesViewEvent.Editor -> handleEditorEvent(event)
        }
    }

    private fun handleListEvent(event: MockRulesViewEvent.List) {
        when (event) {
            is MockRulesViewEvent.List.ToggleMocking ->
                engine.setMockingEnabled(!engine.mockingEnabled.value)

            is MockRulesViewEvent.List.ToggleRule -> viewModelScope.launch {
                val rule = repository.getById(event.ruleId) ?: return@launch
                repository.update(rule.copy(enabled = !rule.enabled))
            }

            is MockRulesViewEvent.List.DeleteRule -> viewModelScope.launch {
                repository.delete(event.ruleId)
                updateState { copy(pendingDeleteRule = null) }
            }

            is MockRulesViewEvent.List.DeleteAllRules -> viewModelScope.launch {
                repository.deleteAll()
                engine.resetCounters()
                updateState { copy(showDeleteAllDialog = false) }
            }

            is MockRulesViewEvent.List.ShowDeleteAllDialog ->
                updateState { copy(showDeleteAllDialog = true) }

            is MockRulesViewEvent.List.DismissDeleteAllDialog ->
                updateState { copy(showDeleteAllDialog = false) }

            is MockRulesViewEvent.List.RequestDeleteRule ->
                updateState { copy(pendingDeleteRule = event.rule) }

            is MockRulesViewEvent.List.DismissDeleteRuleDialog ->
                updateState { copy(pendingDeleteRule = null) }
        }
    }

    private fun handleEditorEvent(event: MockRulesViewEvent.Editor) {
        when (event) {
            is MockRulesViewEvent.Editor.LoadRule ->
                if (claimLoad(event.loadKey)) loadRule(event.ruleId)
            is MockRulesViewEvent.Editor.LoadFromTransaction ->
                if (claimLoad(event.loadKey)) loadFromTransaction(event.transactionId)
            is MockRulesViewEvent.Editor.NoticeShown ->
                updateEditor { copy(notice = null) }

            is MockRulesViewEvent.Editor.SaveRule -> saveRule()

            is MockRulesViewEvent.Editor.NameChanged ->
                updateEditor { copy(name = event.value) }
            is MockRulesViewEvent.Editor.UrlPatternChanged ->
                updateEditor { copy(urlPattern = event.value) }
            is MockRulesViewEvent.Editor.MatchTypeChanged ->
                updateEditor { copy(matchType = event.value) }
            is MockRulesViewEvent.Editor.MethodChanged ->
                updateEditor { copy(method = event.value) }
            is MockRulesViewEvent.Editor.MethodDropdownExpandedChanged ->
                updateEditor { copy(methodDropdownExpanded = event.expanded) }
            is MockRulesViewEvent.Editor.StatusCodeChanged ->
                updateEditor { copy(statusCodeText = event.value.filter(Char::isDigit)) }
            is MockRulesViewEvent.Editor.StatusMessageChanged ->
                updateEditor { copy(statusMessage = event.value) }
            is MockRulesViewEvent.Editor.ContentTypeChanged ->
                updateEditor { copy(contentType = event.value) }
            is MockRulesViewEvent.Editor.ResponseBodyChanged ->
                updateEditor { copy(responseBody = event.value) }
            is MockRulesViewEvent.Editor.DelayTypeChanged ->
                updateEditor { copy(delayType = event.value) }
            is MockRulesViewEvent.Editor.DelayMsChanged ->
                updateEditor { copy(delayMs = event.value) }
            is MockRulesViewEvent.Editor.DelayMinMsChanged ->
                updateEditor { copy(delayMinMs = event.value) }
            is MockRulesViewEvent.Editor.DelayMaxMsChanged ->
                updateEditor { copy(delayMaxMs = event.value) }
        }
    }

    /** Returns false when [loadKey] was already loaded, i.e. the editor was recreated, not reopened. */
    private fun claimLoad(loadKey: String): Boolean {
        if (loadKey == loadedKey) return false
        loadedKey = loadKey
        return true
    }

    private fun loadRule(ruleId: String?) {
        if (ruleId == null || ruleId == "new") {
            existingRule = null
            updateState { copy(editor = EditorState(isLoaded = true)) }
            return
        }
        updateState { copy(editor = EditorState()) }
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
                            statusCodeText = rule.response.statusCode.toString(),
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

    private fun loadFromTransaction(transactionId: String) {
        existingRule = null
        updateState { copy(editor = EditorState()) }
        viewModelScope.launch {
            val id = try {
                UUID.fromString(transactionId)
            } catch (_: IllegalArgumentException) {
                null
            }
            val transaction = id?.let { queryEngine?.getDetails(it) }
            if (transaction == null) {
                updateState {
                    copy(editor = EditorState(isLoaded = true, notice = EditorNotice.TransactionNotFound))
                }
                return@launch
            }
            val body = transaction.response?.bodyRef?.let { queryEngine?.getBodyBytes(it) }
            val prefill = buildTransactionPrefill(transaction, body)
            val notice = EditorNotice.ResponseBodyOmitted.takeIf { prefill.bodyOmitted }
            updateState { copy(editor = prefill.editor.copy(notice = notice)) }
        }
    }

    private fun saveRule() {
        val editor = uiState.value.editor
        // Guards double taps: a second save would insert a duplicate and pop the back stack twice.
        if (editor.isSaving || !editor.isValid) return
        updateEditor { copy(isSaving = true) }
        viewModelScope.launch {
            try {
                persistRule(buildRule())
                existingRule = null
                updateState { copy(editor = EditorState()) }
                emitEffect(MockRulesEffect.NavigateBack)
            } catch (e: SQLiteException) {
                onSaveFailed(e)
            } catch (e: CancellationException) {
                // CancellationException is an IllegalStateException; it must not be reported as a save failure.
                throw e
            } catch (e: IllegalStateException) {
                onSaveFailed(e)
            } finally {
                updateEditor { copy(isSaving = false) }
            }
        }
    }

    private suspend fun persistRule(rule: MockRule) {
        if (repository.getById(rule.id) != null) {
            repository.update(rule)
        } else {
            repository.insert(rule)
        }
    }

    private fun onSaveFailed(error: Exception) {
        Log.w(TAG, "Failed to save mock rule", error)
        emitEffect(MockRulesEffect.SaveFailed)
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
            statusCode = s.statusCodeText.toInt(),
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

private const val TAG = "MockRulesViewModel"

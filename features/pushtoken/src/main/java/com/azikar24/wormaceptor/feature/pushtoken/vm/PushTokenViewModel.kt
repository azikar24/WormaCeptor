package com.azikar24.wormaceptor.feature.pushtoken.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.PushTokenEngine
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

/** ViewModel for the Push Token management screen. */
class PushTokenViewModel(
    private val engine: PushTokenEngine,
) : BaseViewModel<PushTokenViewState, PushTokenEffect, PushTokenEvent>(
    initialState = PushTokenViewState(),
) {

    init {
        combine(
            engine.currentToken,
            engine.tokenHistory,
            engine.isLoading,
            engine.error,
        ) { currentToken, tokenHistory, isLoading, error ->
            updateState {
                copy(
                    currentToken = currentToken,
                    tokenHistory = tokenHistory.toImmutableList(),
                    isLoading = isLoading,
                    error = error,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun handleEvent(event: PushTokenEvent) {
        when (event) {
            is PushTokenEvent.FetchToken -> engine.fetchCurrentToken()
            is PushTokenEvent.RefreshToken -> engine.requestNewToken()
            is PushTokenEvent.DeleteToken -> engine.deleteToken()
            is PushTokenEvent.CopyToken -> handleCopyToken()
            is PushTokenEvent.ClearHistory -> engine.clearHistory()
            is PushTokenEvent.DismissError -> engine.clearError()
        }
    }

    private fun handleCopyToken() {
        val token = uiState.value.currentToken?.token ?: return
        emitEffect(PushTokenEffect.CopyToClipboard(token))
    }

    /** Constants for the Push Token feature. */
    companion object {
        /** Maximum number of history entries to display in the UI. */
        const val MAX_DISPLAY_HISTORY = 20
    }
}

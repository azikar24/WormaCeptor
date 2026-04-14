package com.azikar24.wormaceptor.feature.pushtoken.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.common.presentation.NoOpNavigator
import com.azikar24.wormaceptor.core.engine.PushTokenEngine
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

/** ViewModel for the Push Token management screen. */
class PushTokenViewModel(
    private val engine: PushTokenEngine,
) : BaseViewModel<PushTokenViewState, PushTokenEffect, PushTokenViewEvent, NoOpNavigator>(
    initialState = PushTokenViewState(),
    navigator = NoOpNavigator,
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

    override fun handleEvent(event: PushTokenViewEvent) {
        when (event) {
            is PushTokenViewEvent.FetchToken -> engine.fetchCurrentToken()
            is PushTokenViewEvent.RefreshToken -> engine.requestNewToken()
            is PushTokenViewEvent.DeleteToken -> engine.deleteToken()
            is PushTokenViewEvent.CopyToken -> handleCopyToken()
            is PushTokenViewEvent.ClearHistory -> engine.clearHistory()
            is PushTokenViewEvent.DismissError -> engine.clearError()
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

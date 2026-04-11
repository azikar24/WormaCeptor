package com.azikar24.wormaceptor.feature.crypto.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.CryptoEngine
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

/**
 * ViewModel for the Crypto feature, following the MVI pattern via [BaseViewModel].
 *
 * Collects state from [CryptoEngine] flows and consolidates into [CryptoViewState].
 */
class CryptoViewModel(
    private val engine: CryptoEngine,
) : BaseViewModel<CryptoViewState, CryptoViewEffect, CryptoViewEvent>(CryptoViewState()) {

    init {
        collectEngineFlows()
    }

    override fun handleEvent(event: CryptoViewEvent) {
        when (event) {
            is CryptoViewEvent.Config -> when (event) {
                is CryptoViewEvent.Config.ApplyPreset -> engine.setConfig(event.preset.config)
                is CryptoViewEvent.Config.SetAlgorithm -> engine.setAlgorithm(event.algorithm)
                is CryptoViewEvent.Config.SetMode -> engine.setMode(event.mode)
                is CryptoViewEvent.Config.SetPadding -> engine.updateConfig { copy(padding = event.padding) }
                is CryptoViewEvent.Config.SetKeyFormat -> engine.setKeyFormat(event.format)
                is CryptoViewEvent.Config.SetKey -> engine.setKey(event.key)
                is CryptoViewEvent.Config.SetIv -> engine.setIv(event.iv)
                is CryptoViewEvent.Config.GenerateKey -> handleGenerateKey()
                is CryptoViewEvent.Config.GenerateIv -> handleGenerateIv()
            }
            is CryptoViewEvent.Input -> when (event) {
                is CryptoViewEvent.Input.UpdateText -> updateState { copy(inputText = event.text) }
            }
            is CryptoViewEvent.Operation -> when (event) {
                is CryptoViewEvent.Operation.Encrypt -> handleEncrypt()
                is CryptoViewEvent.Operation.Decrypt -> handleDecrypt()
            }
            is CryptoViewEvent.Result -> when (event) {
                is CryptoViewEvent.Result.Copy -> emitEffect(CryptoViewEffect.CopyToClipboard(event.text))
                is CryptoViewEvent.Result.Clear -> engine.clearResult()
                is CryptoViewEvent.Result.UseAsInput -> handleUseAsInput(event.text)
            }
            is CryptoViewEvent.History -> when (event) {
                is CryptoViewEvent.History.RequestClearAll ->
                    updateState { copy(showClearHistoryConfirmation = true) }
                is CryptoViewEvent.History.ConfirmClearAll -> {
                    engine.clearHistory()
                    updateState { copy(showClearHistoryConfirmation = false) }
                }
                is CryptoViewEvent.History.DismissClearConfirmation ->
                    updateState { copy(showClearHistoryConfirmation = false) }
                is CryptoViewEvent.History.Remove -> engine.removeFromHistory(event.id)
                is CryptoViewEvent.History.Load -> {
                    updateState { copy(inputText = event.result.input, showHistory = false) }
                    emitEffect(CryptoViewEffect.HistoryLoaded)
                }
            }
            is CryptoViewEvent.Navigation -> when (event) {
                is CryptoViewEvent.Navigation.ShowHistory ->
                    updateState { copy(showHistory = true) }
                is CryptoViewEvent.Navigation.HideHistory ->
                    updateState { copy(showHistory = false) }
            }
        }
    }

    private fun handleGenerateKey() {
        val key = engine.generateKey()
        engine.setKey(key)
        emitEffect(CryptoViewEffect.KeyGenerated)
    }

    private fun handleGenerateIv() {
        val iv = engine.generateIv()
        engine.setIv(iv)
        emitEffect(CryptoViewEffect.IvGenerated)
    }

    private fun handleEncrypt() {
        val input = uiState.value.inputText
        if (input.isNotBlank()) {
            engine.encrypt(input)
        }
    }

    private fun handleDecrypt() {
        val input = uiState.value.inputText
        if (input.isNotBlank()) {
            engine.decrypt(input)
        }
    }

    private fun handleUseAsInput(text: String) {
        updateState { copy(inputText = text) }
        emitEffect(CryptoViewEffect.OutputLoadedAsInput)
    }

    private fun collectEngineFlows() {
        viewModelScope.launch {
            engine.config.collect { config ->
                updateState { copy(config = config) }
            }
        }
        viewModelScope.launch {
            engine.currentResult.collect { result ->
                updateState { copy(currentResult = result) }
            }
        }
        viewModelScope.launch {
            engine.history.collect { history ->
                updateState { copy(history = history.toImmutableList()) }
            }
        }
        viewModelScope.launch {
            engine.isProcessing.collect { processing ->
                updateState { copy(isProcessing = processing) }
            }
        }
        viewModelScope.launch {
            engine.error.collect { error ->
                updateState { copy(error = error) }
            }
        }
    }
}

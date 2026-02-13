package com.azikar24.wormaceptor.feature.crypto

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.CryptoEngine
import com.azikar24.wormaceptor.domain.entities.CipherMode
import com.azikar24.wormaceptor.domain.entities.CryptoAlgorithm
import com.azikar24.wormaceptor.domain.entities.CryptoConfig
import com.azikar24.wormaceptor.domain.entities.CryptoPreset
import com.azikar24.wormaceptor.domain.entities.CryptoResult
import com.azikar24.wormaceptor.domain.entities.KeyFormat
import com.azikar24.wormaceptor.domain.entities.PaddingScheme
import kotlinx.coroutines.flow.StateFlow

/**
 * Entry point for the Response Encryption/Decryption feature.
 * Provides factory methods and composable entry point.
 */
object CryptoFeature {
    /**
     * Creates a CryptoViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(engine: CryptoEngine): CryptoViewModelFactory = CryptoViewModelFactory(engine)
}

/**
 * ViewModel for the Crypto feature.
 */
class CryptoViewModel(private val engine: CryptoEngine) : ViewModel() {
    val config: StateFlow<CryptoConfig> = engine.config
    val currentResult: StateFlow<CryptoResult?> = engine.currentResult
    val history: StateFlow<List<CryptoResult>> = engine.history
    val isProcessing: StateFlow<Boolean> = engine.isProcessing
    val error: StateFlow<String?> = engine.error

    var inputText by mutableStateOf("")
        private set

    fun updateInputText(text: String) {
        inputText = text
    }

    fun setAlgorithm(algorithm: CryptoAlgorithm) = engine.setAlgorithm(algorithm)
    fun setMode(mode: CipherMode) = engine.setMode(mode)
    fun setPadding(padding: PaddingScheme) = engine.updateConfig { copy(padding = padding) }
    fun setKey(key: String) = engine.setKey(key)
    fun setIv(iv: String) = engine.setIv(iv)
    fun setKeyFormat(format: KeyFormat) = engine.setKeyFormat(format)

    fun applyPreset(preset: CryptoPreset) {
        engine.setConfig(preset.config)
    }

    fun generateKey(): String {
        val key = engine.generateKey()
        engine.setKey(key)
        return key
    }

    fun generateIv(): String {
        val iv = engine.generateIv()
        engine.setIv(iv)
        return iv
    }

    fun encrypt() {
        if (inputText.isNotBlank()) {
            engine.encrypt(inputText)
        }
    }

    fun decrypt() {
        if (inputText.isNotBlank()) {
            engine.decrypt(inputText)
        }
    }

    fun clearResult() = engine.clearResult()
    fun clearHistory() = engine.clearHistory()
    fun removeFromHistory(id: String) = engine.removeFromHistory(id)

    fun loadFromHistory(result: CryptoResult) {
        inputText = result.input
    }
}

/**
 * Factory for creating CryptoViewModel instances.
 */
class CryptoViewModelFactory(
    private val engine: CryptoEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CryptoViewModel::class.java)) {
            return CryptoViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

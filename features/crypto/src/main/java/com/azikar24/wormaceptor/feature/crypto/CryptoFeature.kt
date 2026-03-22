package com.azikar24.wormaceptor.feature.crypto

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.azikar24.wormaceptor.feature.crypto.ui.CryptoHistoryScreen
import com.azikar24.wormaceptor.feature.crypto.ui.CryptoTool
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.koinInject

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
    /** Current cryptographic configuration (algorithm, mode, padding, key, IV). */
    val config: StateFlow<CryptoConfig> = engine.config

    /** Result of the most recent encrypt/decrypt operation, or null. */
    val currentResult: StateFlow<CryptoResult?> = engine.currentResult

    /** List of previous encryption/decryption results. */
    val history: StateFlow<List<CryptoResult>> = engine.history

    /** Whether an encrypt or decrypt operation is currently running. */
    val isProcessing: StateFlow<Boolean> = engine.isProcessing

    /** Current error message from the engine, or null if no error. */
    val error: StateFlow<String?> = engine.error

    /** Plaintext input for encryption or ciphertext input for decryption. */
    var inputText by mutableStateOf("")
        private set

    /** Updates the plaintext input for encryption or decryption. */
    fun updateInputText(text: String) {
        inputText = text
    }

    /** Sets the cryptographic algorithm (e.g. AES, DES). */
    fun setAlgorithm(algorithm: CryptoAlgorithm) = engine.setAlgorithm(algorithm)

    /** Sets the cipher block mode (e.g. CBC, ECB). */
    fun setMode(mode: CipherMode) = engine.setMode(mode)

    /** Sets the padding scheme (e.g. PKCS5, NoPadding). */
    fun setPadding(padding: PaddingScheme) = engine.updateConfig { copy(padding = padding) }

    /** Sets the encryption key string. */
    fun setKey(key: String) = engine.setKey(key)

    /** Sets the initialization vector string. */
    fun setIv(iv: String) = engine.setIv(iv)

    /** Sets the key encoding format (e.g. Base64, Hex). */
    fun setKeyFormat(format: KeyFormat) = engine.setKeyFormat(format)

    /** Applies a predefined configuration preset (algorithm, mode, padding). */
    fun applyPreset(preset: CryptoPreset) {
        engine.setConfig(preset.config)
    }

    /** Generates a random key for the current algorithm and stores it. */
    fun generateKey(): String {
        val key = engine.generateKey()
        engine.setKey(key)
        return key
    }

    /** Generates a random initialization vector and stores it. */
    fun generateIv(): String {
        val iv = engine.generateIv()
        engine.setIv(iv)
        return iv
    }

    /** Encrypts the current input text using the configured algorithm and key. */
    fun encrypt() {
        if (inputText.isNotBlank()) {
            engine.encrypt(inputText)
        }
    }

    /** Decrypts the current input text using the configured algorithm and key. */
    fun decrypt() {
        if (inputText.isNotBlank()) {
            engine.decrypt(inputText)
        }
    }

    /** Clears the current encryption/decryption result. */
    fun clearResult() = engine.clearResult()

    /** Clears all entries from the operation history. */
    fun clearHistory() = engine.clearHistory()

    /** Removes a single entry from the operation history by its ID. */
    fun removeFromHistory(id: String) = engine.removeFromHistory(id)

    /** Restores the input text from a previous history result. */
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

/**
 * Main composable entry point for the Crypto feature.
 * Manages navigation between the crypto tool and history screens.
 */
@Composable
fun CryptoScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: CryptoEngine = koinInject()
    var showHistory by remember { mutableStateOf(false) }
    if (showHistory) {
        CryptoHistoryScreen(
            engine = engine,
            onNavigateBack = { showHistory = false },
            onLoadResult = { showHistory = false },
            modifier = modifier,
        )
    } else {
        CryptoTool(
            engine = engine,
            onNavigateBack = onNavigateBack,
            onNavigateToHistory = { showHistory = true },
            modifier = modifier,
        )
    }
}

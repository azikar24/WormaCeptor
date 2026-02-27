package com.azikar24.wormaceptor.core.engine

import android.util.Base64
import com.azikar24.wormaceptor.domain.entities.CipherMode
import com.azikar24.wormaceptor.domain.entities.CryptoAlgorithm
import com.azikar24.wormaceptor.domain.entities.CryptoConfig
import com.azikar24.wormaceptor.domain.entities.CryptoOperation
import com.azikar24.wormaceptor.domain.entities.CryptoResult
import com.azikar24.wormaceptor.domain.entities.KeyFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Engine for performing cryptographic operations on data.
 *
 * Supports:
 * - Multiple algorithms: AES-128, AES-256, DES, 3DES
 * - Multiple modes: CBC, ECB, GCM, CTR, CFB, OFB
 * - Various padding schemes: PKCS5, PKCS7, NoPadding
 * - Operation history tracking
 * - Key generation utilities
 *
 * Usage:
 * 1. Create an instance of CryptoEngine
 * 2. Set configuration via setConfig() or use a preset
 * 3. Call encrypt() or decrypt() with your data
 * 4. Access results via currentResult or history
 */
@Suppress("TooManyFunctions")
class CryptoEngine {

    // Current configuration
    private val _config = MutableStateFlow(CryptoConfig.default())

    /** The current cryptographic configuration (algorithm, mode, key, IV). */
    val config: StateFlow<CryptoConfig> = _config.asStateFlow()

    // Current operation result
    private val _currentResult = MutableStateFlow<CryptoResult?>(null)

    /** The result of the most recent encrypt or decrypt operation. */
    val currentResult: StateFlow<CryptoResult?> = _currentResult.asStateFlow()

    // Operation history (most recent first)
    private val _history = MutableStateFlow<List<CryptoResult>>(emptyList())

    /** History of cryptographic operation results, most recent first. */
    val history: StateFlow<List<CryptoResult>> = _history.asStateFlow()

    // Loading state
    private val _isProcessing = MutableStateFlow(false)

    /** Whether an encrypt or decrypt operation is currently in progress. */
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)

    /** The most recent error message, or null if no error occurred. */
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Sets a new cryptographic configuration.
     *
     * @param config The new configuration to apply
     */
    fun setConfig(config: CryptoConfig) {
        _config.value = config
        _error.value = null
    }

    /**
     * Updates a single field in the current configuration.
     */
    fun updateConfig(update: CryptoConfig.() -> CryptoConfig) {
        _config.value = _config.value.update()
        _error.value = null
    }

    /**
     * Sets the encryption algorithm.
     */
    fun setAlgorithm(algorithm: CryptoAlgorithm) {
        _config.value = _config.value.copy(algorithm = algorithm)
    }

    /**
     * Sets the cipher mode.
     */
    fun setMode(mode: CipherMode) {
        _config.value = _config.value.copy(mode = mode)
    }

    /**
     * Sets the encryption key.
     */
    fun setKey(key: String) {
        _config.value = _config.value.copy(key = key)
    }

    /**
     * Sets the initialization vector.
     */
    fun setIv(iv: String) {
        _config.value = _config.value.copy(iv = iv)
    }

    /**
     * Sets the key format.
     */
    fun setKeyFormat(format: KeyFormat) {
        _config.value = _config.value.copy(keyFormat = format)
    }

    /**
     * Encrypts the given plaintext using the current configuration.
     *
     * @param plaintext The text to encrypt
     * @return The encryption result
     */
    fun encrypt(plaintext: String): CryptoResult {
        _isProcessing.value = true
        _error.value = null

        val startTime = System.currentTimeMillis()
        val config = _config.value
        val id = UUID.randomUUID().toString()

        val result = try {
            validateConfig(config)

            val keyBytes = parseKeyOrIv(config.key, config.keyFormat)
            val ivBytes = if (config.mode.requiresIv) {
                parseKeyOrIv(config.iv, config.keyFormat)
            } else {
                null
            }

            val cipher = Cipher.getInstance(config.getTransformation())
            val secretKey = SecretKeySpec(keyBytes, config.algorithm.algorithmName)

            when {
                config.mode == CipherMode.GCM -> {
                    val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, ivBytes)
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
                }
                config.mode.requiresIv && ivBytes != null -> {
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(ivBytes))
                }
                else -> {
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                }
            }

            val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

            val durationMs = System.currentTimeMillis() - startTime
            CryptoResult.encryptSuccess(
                id = id,
                input = plaintext,
                output = encryptedBase64,
                algorithm = config.algorithm,
                mode = config.mode,
                durationMs = durationMs,
            )
        } catch (e: Exception) {
            val durationMs = System.currentTimeMillis() - startTime
            val errorMessage = e.message ?: "Unknown encryption error"
            _error.value = errorMessage
            CryptoResult.failure(
                id = id,
                operation = CryptoOperation.ENCRYPT,
                input = plaintext,
                algorithm = config.algorithm,
                mode = config.mode,
                errorMessage = errorMessage,
                durationMs = durationMs,
            )
        }

        _currentResult.value = result
        addToHistory(result)
        _isProcessing.value = false
        return result
    }

    /**
     * Decrypts the given ciphertext using the current configuration.
     *
     * @param ciphertext The Base64-encoded ciphertext to decrypt
     * @return The decryption result
     */
    fun decrypt(ciphertext: String): CryptoResult {
        _isProcessing.value = true
        _error.value = null

        val startTime = System.currentTimeMillis()
        val config = _config.value
        val id = UUID.randomUUID().toString()

        val result = try {
            validateConfig(config)

            val keyBytes = parseKeyOrIv(config.key, config.keyFormat)
            val ivBytes = if (config.mode.requiresIv) {
                parseKeyOrIv(config.iv, config.keyFormat)
            } else {
                null
            }

            val encryptedBytes = Base64.decode(ciphertext, Base64.NO_WRAP)

            val cipher = Cipher.getInstance(config.getTransformation())
            val secretKey = SecretKeySpec(keyBytes, config.algorithm.algorithmName)

            when {
                config.mode == CipherMode.GCM -> {
                    val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, ivBytes)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
                }
                config.mode.requiresIv && ivBytes != null -> {
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ivBytes))
                }
                else -> {
                    cipher.init(Cipher.DECRYPT_MODE, secretKey)
                }
            }

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            val decryptedText = String(decryptedBytes, Charsets.UTF_8)

            val durationMs = System.currentTimeMillis() - startTime
            CryptoResult.decryptSuccess(
                id = id,
                input = ciphertext,
                output = decryptedText,
                algorithm = config.algorithm,
                mode = config.mode,
                durationMs = durationMs,
            )
        } catch (e: Exception) {
            val durationMs = System.currentTimeMillis() - startTime
            val errorMessage = when {
                e.message?.contains(
                    "padding",
                ) == true -> "Decryption failed: Invalid padding (wrong key/IV or corrupted data)"
                e.message?.contains("tag") == true -> "Decryption failed: Authentication tag mismatch (wrong key/IV)"
                e.message?.contains(
                    "IllegalBlockSize",
                ) == true -> "Decryption failed: Invalid block size (corrupted data)"
                else -> e.message ?: "Unknown decryption error"
            }
            _error.value = errorMessage
            CryptoResult.failure(
                id = id,
                operation = CryptoOperation.DECRYPT,
                input = ciphertext,
                algorithm = config.algorithm,
                mode = config.mode,
                errorMessage = errorMessage,
                durationMs = durationMs,
            )
        }

        _currentResult.value = result
        addToHistory(result)
        _isProcessing.value = false
        return result
    }

    /**
     * Encrypts raw bytes and returns encrypted bytes.
     *
     * @param data The bytes to encrypt
     * @return The encrypted bytes, or null if encryption failed
     */
    fun encryptBytes(data: ByteArray): ByteArray? {
        val config = _config.value

        return try {
            validateConfig(config)

            val keyBytes = parseKeyOrIv(config.key, config.keyFormat)
            val ivBytes = if (config.mode.requiresIv) {
                parseKeyOrIv(config.iv, config.keyFormat)
            } else {
                null
            }

            val cipher = Cipher.getInstance(config.getTransformation())
            val secretKey = SecretKeySpec(keyBytes, config.algorithm.algorithmName)

            when {
                config.mode == CipherMode.GCM -> {
                    val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, ivBytes)
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
                }
                config.mode.requiresIv && ivBytes != null -> {
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(ivBytes))
                }
                else -> {
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                }
            }

            cipher.doFinal(data)
        } catch (e: Exception) {
            _error.value = e.message ?: "Encryption failed"
            null
        }
    }

    /**
     * Decrypts raw bytes and returns decrypted bytes.
     *
     * @param data The bytes to decrypt
     * @return The decrypted bytes, or null if decryption failed
     */
    fun decryptBytes(data: ByteArray): ByteArray? {
        val config = _config.value

        return try {
            validateConfig(config)

            val keyBytes = parseKeyOrIv(config.key, config.keyFormat)
            val ivBytes = if (config.mode.requiresIv) {
                parseKeyOrIv(config.iv, config.keyFormat)
            } else {
                null
            }

            val cipher = Cipher.getInstance(config.getTransformation())
            val secretKey = SecretKeySpec(keyBytes, config.algorithm.algorithmName)

            when {
                config.mode == CipherMode.GCM -> {
                    val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, ivBytes)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
                }
                config.mode.requiresIv && ivBytes != null -> {
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ivBytes))
                }
                else -> {
                    cipher.init(Cipher.DECRYPT_MODE, secretKey)
                }
            }

            cipher.doFinal(data)
        } catch (e: Exception) {
            _error.value = e.message ?: "Decryption failed"
            null
        }
    }

    /**
     * Generates a random key for the current algorithm.
     *
     * @return The generated key in the current key format
     */
    fun generateKey(): String {
        val keyLengthBytes = _config.value.algorithm.keyLengthBits / 8
        val keyBytes = ByteArray(keyLengthBytes)
        SecureRandom().nextBytes(keyBytes)
        return formatBytes(keyBytes, _config.value.keyFormat)
    }

    /**
     * Generates a random IV for the current mode.
     *
     * @return The generated IV in the current key format
     */
    fun generateIv(): String {
        val ivLengthBytes = if (_config.value.mode == CipherMode.GCM) GCM_IV_LENGTH_BYTES else IV_LENGTH_BYTES
        val ivBytes = ByteArray(ivLengthBytes)
        SecureRandom().nextBytes(ivBytes)
        return formatBytes(ivBytes, _config.value.keyFormat)
    }

    /**
     * Formats bytes to string based on the key format.
     */
    private fun formatBytes(
        bytes: ByteArray,
        format: KeyFormat,
    ): String {
        return when (format) {
            KeyFormat.BASE64 -> Base64.encodeToString(bytes, Base64.NO_WRAP)
            KeyFormat.HEX -> bytes.joinToString("") { "%02x".format(it) }
            KeyFormat.UTF8 -> String(bytes, Charsets.UTF_8)
        }
    }

    /**
     * Clears the current result.
     */
    fun clearResult() {
        _currentResult.value = null
        _error.value = null
    }

    /**
     * Clears all operation history.
     */
    fun clearHistory() {
        _history.value = emptyList()
    }

    /**
     * Removes a specific result from history.
     */
    fun removeFromHistory(id: String) {
        _history.value = _history.value.filter { it.id != id }
    }

    /**
     * Validates the configuration before performing an operation.
     */
    private fun validateConfig(config: CryptoConfig) {
        require(config.key.isNotBlank()) { "Key is required" }

        if (config.mode.requiresIv) {
            require(config.iv.isNotBlank()) { "IV is required for ${config.mode.displayName} mode" }
        }

        // Validate key length
        val keyBytes = parseKeyOrIv(config.key, config.keyFormat)
        val expectedKeyLength = config.algorithm.keyLengthBits / 8

        require(keyBytes.size == expectedKeyLength) {
            "Invalid key length: expected $expectedKeyLength bytes for ${config.algorithm.displayName}, got ${keyBytes.size} bytes"
        }
    }

    /**
     * Parses a key or IV string into bytes based on the format.
     */
    private fun parseKeyOrIv(
        value: String,
        format: KeyFormat,
    ): ByteArray {
        return when (format) {
            KeyFormat.BASE64 -> Base64.decode(value, Base64.NO_WRAP)
            KeyFormat.HEX -> value.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            KeyFormat.UTF8 -> value.toByteArray(Charsets.UTF_8)
        }
    }

    /**
     * Adds a result to the history, keeping only the most recent entries.
     */
    private fun addToHistory(result: CryptoResult) {
        val currentHistory = _history.value.toMutableList()
        currentHistory.add(0, result)
        _history.value = currentHistory.take(MAX_HISTORY_SIZE)
    }

    /** Configuration constants and crypto parameters. */
    companion object {
        /** Maximum number of results to keep in history */
        const val MAX_HISTORY_SIZE = 50

        /** GCM tag length in bits */
        private const val GCM_TAG_LENGTH_BITS = 128

        /** Standard IV length in bytes */
        private const val IV_LENGTH_BYTES = 16

        /** GCM IV length in bytes (12 is recommended) */
        private const val GCM_IV_LENGTH_BYTES = 12
    }
}

package com.azikar24.wormaceptor.domain.entities

/**
 * Result of a cryptographic operation (encryption or decryption).
 *
 * @property id Unique identifier for this result
 * @property operation The type of operation performed
 * @property input The input data (plaintext for encryption, ciphertext for decryption)
 * @property output The output data (ciphertext for encryption, plaintext for decryption), null if failed
 * @property algorithm The algorithm used
 * @property mode The cipher mode used
 * @property success Whether the operation completed successfully
 * @property errorMessage Error message if the operation failed
 * @property timestamp When this operation was performed
 * @property durationMs How long the operation took in milliseconds
 */
data class CryptoResult(
    val id: String,
    val operation: CryptoOperation,
    val input: String,
    val output: String?,
    val algorithm: CryptoAlgorithm,
    val mode: CipherMode,
    val success: Boolean,
    val errorMessage: String?,
    val timestamp: Long,
    val durationMs: Long,
) {
    companion object {
        /**
         * Creates a successful encryption result.
         */
        fun encryptSuccess(
            id: String,
            input: String,
            output: String,
            algorithm: CryptoAlgorithm,
            mode: CipherMode,
            durationMs: Long,
        ) = CryptoResult(
            id = id,
            operation = CryptoOperation.ENCRYPT,
            input = input,
            output = output,
            algorithm = algorithm,
            mode = mode,
            success = true,
            errorMessage = null,
            timestamp = System.currentTimeMillis(),
            durationMs = durationMs,
        )

        /**
         * Creates a successful decryption result.
         */
        fun decryptSuccess(
            id: String,
            input: String,
            output: String,
            algorithm: CryptoAlgorithm,
            mode: CipherMode,
            durationMs: Long,
        ) = CryptoResult(
            id = id,
            operation = CryptoOperation.DECRYPT,
            input = input,
            output = output,
            algorithm = algorithm,
            mode = mode,
            success = true,
            errorMessage = null,
            timestamp = System.currentTimeMillis(),
            durationMs = durationMs,
        )

        /**
         * Creates a failed result.
         */
        fun failure(
            id: String,
            operation: CryptoOperation,
            input: String,
            algorithm: CryptoAlgorithm,
            mode: CipherMode,
            errorMessage: String,
            durationMs: Long,
        ) = CryptoResult(
            id = id,
            operation = operation,
            input = input,
            output = null,
            algorithm = algorithm,
            mode = mode,
            success = false,
            errorMessage = errorMessage,
            timestamp = System.currentTimeMillis(),
            durationMs = durationMs,
        )
    }
}

/**
 * Types of cryptographic operations.
 */
enum class CryptoOperation(val displayName: String) {
    ENCRYPT("Encrypt"),
    DECRYPT("Decrypt"),
}

/**
 * Preset crypto configurations for common use cases.
 *
 * @property displayName Human-readable name for UI
 * @property description Brief description of the preset
 * @property config The preset configuration
 */
enum class CryptoPreset(
    val displayName: String,
    val description: String,
    val config: CryptoConfig,
) {
    AES_256_GCM(
        "AES-256 GCM",
        "Recommended for modern applications",
        CryptoConfig(
            algorithm = CryptoAlgorithm.AES_256,
            mode = CipherMode.GCM,
            padding = PaddingScheme.NO_PADDING,
            key = "",
            iv = "",
            keyFormat = KeyFormat.BASE64,
        ),
    ),
    AES_256_CBC(
        "AES-256 CBC",
        "Common for API responses",
        CryptoConfig(
            algorithm = CryptoAlgorithm.AES_256,
            mode = CipherMode.CBC,
            padding = PaddingScheme.PKCS5,
            key = "",
            iv = "",
            keyFormat = KeyFormat.BASE64,
        ),
    ),
    AES_128_CBC(
        "AES-128 CBC",
        "Legacy compatibility",
        CryptoConfig(
            algorithm = CryptoAlgorithm.AES_128,
            mode = CipherMode.CBC,
            padding = PaddingScheme.PKCS5,
            key = "",
            iv = "",
            keyFormat = KeyFormat.BASE64,
        ),
    ),
    TRIPLE_DES_CBC(
        "3DES CBC",
        "Legacy systems",
        CryptoConfig(
            algorithm = CryptoAlgorithm.TRIPLE_DES,
            mode = CipherMode.CBC,
            padding = PaddingScheme.PKCS5,
            key = "",
            iv = "",
            keyFormat = KeyFormat.BASE64,
        ),
    ),
}

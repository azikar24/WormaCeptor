package com.azikar24.wormaceptor.domain.entities

/**
 * Configuration for cryptographic operations.
 *
 * @property algorithm The encryption algorithm to use
 * @property mode The cipher mode (CBC, ECB, GCM, etc.)
 * @property padding The padding scheme
 * @property key The encryption/decryption key (Base64 encoded or hex string)
 * @property iv The initialization vector for modes that require it (Base64 encoded or hex string)
 * @property keyFormat The format of the key input
 */
data class CryptoConfig(
    val algorithm: CryptoAlgorithm,
    val mode: CipherMode,
    val padding: PaddingScheme,
    val key: String,
    val iv: String,
    val keyFormat: KeyFormat,
) {
    /**
     * Returns the full transformation string for Cipher.getInstance().
     * e.g., "AES/CBC/PKCS5Padding"
     */
    fun getTransformation(): String {
        return "${algorithm.algorithmName}/${mode.modeName}/${padding.paddingName}"
    }

    /**
     * Validates that the configuration is complete and valid.
     */
    @OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)
    fun isValid(): Boolean {
        // Key is required
        if (key.isBlank()) return false

        // IV is required for CBC and GCM modes
        if (mode.requiresIv && iv.isBlank()) return false

        // Validate key length for algorithm
        val keyBytes = try {
            when (keyFormat) {
                KeyFormat.BASE64 -> kotlin.io.encoding.Base64.decode(key)
                KeyFormat.HEX -> key.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                KeyFormat.UTF8 -> key.toByteArray(Charsets.UTF_8)
            }
        } catch (e: Exception) {
            return false
        }

        return when (algorithm) {
            CryptoAlgorithm.AES_128 -> keyBytes.size == 16
            CryptoAlgorithm.AES_256 -> keyBytes.size == 32
            CryptoAlgorithm.DES -> keyBytes.size == 8
            CryptoAlgorithm.TRIPLE_DES -> keyBytes.size == 24
            CryptoAlgorithm.RSA -> true // RSA key validation is more complex
        }
    }

    companion object {
        /**
         * Creates a default configuration with AES-256/CBC.
         */
        fun default() = CryptoConfig(
            algorithm = CryptoAlgorithm.AES_256,
            mode = CipherMode.CBC,
            padding = PaddingScheme.PKCS5,
            key = "",
            iv = "",
            keyFormat = KeyFormat.BASE64,
        )
    }
}

/**
 * Supported encryption algorithms.
 *
 * @property algorithmName The name used in Cipher.getInstance()
 * @property displayName Human-readable name for UI
 * @property keyLengthBits The required key length in bits
 */
enum class CryptoAlgorithm(
    val algorithmName: String,
    val displayName: String,
    val keyLengthBits: Int,
) {
    AES_128("AES", "AES-128", 128),
    AES_256("AES", "AES-256", 256),
    DES("DES", "DES", 64),
    TRIPLE_DES("DESede", "3DES", 192),
    RSA("RSA", "RSA", 2048),
}

/**
 * Cipher modes of operation.
 *
 * @property modeName The mode name used in the transformation string
 * @property displayName Human-readable name for UI
 * @property requiresIv Whether this mode requires an initialization vector
 */
enum class CipherMode(
    val modeName: String,
    val displayName: String,
    val requiresIv: Boolean,
) {
    CBC("CBC", "CBC", true),
    ECB("ECB", "ECB", false),
    GCM("GCM", "GCM", true),
    CTR("CTR", "CTR", true),
    CFB("CFB", "CFB", true),
    OFB("OFB", "OFB", true),
}

/**
 * Padding schemes for block ciphers.
 *
 * @property paddingName The padding name used in the transformation string
 * @property displayName Human-readable name for UI
 */
enum class PaddingScheme(
    val paddingName: String,
    val displayName: String,
) {
    PKCS5("PKCS5Padding", "PKCS5"),
    PKCS7("PKCS7Padding", "PKCS7"),
    NO_PADDING("NoPadding", "None"),
    ISO10126("ISO10126Padding", "ISO10126"),
}

/**
 * Format of the key/IV input.
 *
 * @property displayName Human-readable name for UI
 */
enum class KeyFormat(val displayName: String) {
    BASE64("Base64"),
    HEX("Hex"),
    UTF8("UTF-8 Text"),
}

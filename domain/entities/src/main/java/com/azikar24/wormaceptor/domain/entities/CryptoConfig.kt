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
        } catch (_: Exception) {
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

    /** Factory methods and default presets for [CryptoConfig]. */
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
    /** AES with 128-bit key. */
    AES_128("AES", "AES-128", 128),

    /** AES with 256-bit key. */
    AES_256("AES", "AES-256", 256),

    /** DES with 64-bit key (legacy, not recommended). */
    DES("DES", "DES", 64),

    /** Triple DES with 192-bit key. */
    TRIPLE_DES("DESede", "3DES", 192),

    /** RSA asymmetric encryption with 2048-bit key. */
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
    /** Cipher Block Chaining - requires IV, widely used. */
    CBC("CBC", "CBC", true),

    /** Electronic Codebook - no IV, identical blocks produce identical ciphertext. */
    ECB("ECB", "ECB", false),

    /** Galois/Counter Mode - authenticated encryption, requires IV. */
    GCM("GCM", "GCM", true),

    /** Counter Mode - converts block cipher to stream cipher, requires IV. */
    CTR("CTR", "CTR", true),

    /** Cipher Feedback - self-synchronizing stream mode, requires IV. */
    CFB("CFB", "CFB", true),

    /** Output Feedback - synchronous stream mode, requires IV. */
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
    /** PKCS#5 padding, standard for block ciphers. */
    PKCS5("PKCS5Padding", "PKCS5"),

    /** PKCS#7 padding, functionally equivalent to PKCS#5 for common block sizes. */
    PKCS7("PKCS7Padding", "PKCS7"),

    /** No padding; input must be an exact multiple of the block size. */
    NO_PADDING("NoPadding", "None"),

    /** ISO 10126 padding with random bytes. */
    ISO10126("ISO10126Padding", "ISO10126"),
}

/**
 * Format of the key/IV input.
 *
 * @property displayName Human-readable name for UI
 */
enum class KeyFormat(val displayName: String) {
    /** Key encoded as a Base64 string. */
    BASE64("Base64"),

    /** Key encoded as a hexadecimal string. */
    HEX("Hex"),

    /** Key provided as raw UTF-8 text. */
    UTF8("UTF-8 Text"),
}

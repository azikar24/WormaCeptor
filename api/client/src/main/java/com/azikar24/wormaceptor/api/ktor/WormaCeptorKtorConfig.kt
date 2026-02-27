package com.azikar24.wormaceptor.api.ktor

import com.azikar24.wormaceptor.api.WormaCeptorApi

/**
 * Configuration for the WormaCeptor Ktor client plugin.
 *
 * Usage:
 * ```kotlin
 * val client = HttpClient(CIO) {
 *     install(WormaCeptorKtorPlugin) {
 *         maxContentLength = 500_000L
 *         retainDataFor = WormaCeptorKtorConfig.RetentionPeriod.ONE_WEEK
 *         redactHeader("Authorization")
 *         redactJsonValue("password")
 *     }
 * }
 * ```
 */
@Suppress("unused")
class WormaCeptorKtorConfig {

    /**
     * Data retention period for captured network transactions.
     */
    enum class RetentionPeriod {
        /** Retain captured data for one hour. */
        ONE_HOUR,

        /** Retain captured data for one day. */
        ONE_DAY,

        /** Retain captured data for one week. */
        ONE_WEEK,

        /** Retain captured data for one month. */
        ONE_MONTH,

        /** Retain captured data indefinitely. */
        FOREVER,
    }

    /**
     * Maximum content length to capture for request and response bodies.
     * Bodies larger than this limit will be truncated. Default: 250,000 bytes.
     */
    var maxContentLength: Long = DEFAULT_MAX_CONTENT_LENGTH

    /**
     * Data retention period. Transactions older than this are cleaned up on plugin install.
     * Default: null (no cleanup).
     */
    var retainDataFor: RetentionPeriod? = null

    /**
     * Adds a header name to be redacted. The header value will be replaced with asterisks.
     * Comparison is case-insensitive.
     *
     * @param name The header name to redact (e.g., "Authorization", "Cookie")
     */
    fun redactHeader(name: String) {
        WormaCeptorApi.redactionConfig.redactHeader(name)
    }

    /**
     * Adds a regex pattern to redact in request and response bodies.
     *
     * @param pattern A regex pattern to match (e.g., "api_key=\\w+")
     */
    fun redactBody(pattern: String) {
        WormaCeptorApi.redactionConfig.redactBody(pattern)
    }

    /**
     * Redacts JSON values for the specified key in request and response bodies.
     *
     * @param key The JSON key whose value should be redacted (e.g., "password", "token")
     */
    fun redactJsonValue(key: String) {
        WormaCeptorApi.redactionConfig.redactJsonValue(key)
    }

    /**
     * Redacts XML element values for the specified tag in request and response bodies.
     *
     * @param tag The XML tag whose content should be redacted (e.g., "Password")
     */
    fun redactXmlValue(tag: String) {
        WormaCeptorApi.redactionConfig.redactXmlValue(tag)
    }

    internal companion object {
        const val DEFAULT_MAX_CONTENT_LENGTH = 250_000L

        @Suppress("MagicNumber")
        fun retentionToMillis(period: RetentionPeriod): Long = when (period) {
            RetentionPeriod.ONE_HOUR -> 60 * 60 * 1000L
            RetentionPeriod.ONE_DAY -> 24 * 60 * 60 * 1000L
            RetentionPeriod.ONE_WEEK -> 7 * 24 * 60 * 60 * 1000L
            RetentionPeriod.ONE_MONTH -> 30 * 24 * 60 * 60 * 1000L
            RetentionPeriod.FOREVER -> 0L
        }
    }
}

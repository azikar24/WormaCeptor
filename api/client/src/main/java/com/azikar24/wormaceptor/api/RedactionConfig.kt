package com.azikar24.wormaceptor.api

/**
 * Configuration for data redaction in WormaCeptor.
 * Allows masking sensitive information in headers and request/response bodies.
 */
class RedactionConfig {
    internal val headersToRedact = mutableSetOf<String>()
    internal val bodyPatternsToRedact = mutableSetOf<Regex>()
    internal var replacementText: String = "********"

    /**
     * Adds a header name to be redacted. Comparison is case-insensitive.
     */
    fun redactHeader(name: String): RedactionConfig {
        headersToRedact.add(name.lowercase())
        return this
    }

    /**
     * Adds a regex pattern to be redacted in the request and response body.
     */
    fun redactBody(pattern: String): RedactionConfig {
        bodyPatternsToRedact.add(Regex(pattern, RegexOption.IGNORE_CASE))
        return this
    }

    /**
     * Sets the replacement text for redacted content. Default is "********".
     */
    fun replacement(text: String): RedactionConfig {
        replacementText = text
        return this
    }
}

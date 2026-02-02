package com.azikar24.wormaceptor.api

/**
 * Configuration for data redaction in WormaCeptor.
 *
 * Allows masking sensitive information in HTTP headers and request/response bodies.
 * Configure redaction rules before capturing network traffic to prevent sensitive
 * data from being stored or displayed.
 *
 * Usage:
 * ```kotlin
 * WormaCeptorApi.redactionConfig
 *     .redactHeader("Authorization")
 *     .redactHeader("Cookie")
 *     .redactJsonValue("password")
 *     .redactJsonValue("api_key")
 *     .replacement("[REDACTED]")
 * ```
 */
class RedactionConfig {
    internal val headersToRedact = mutableSetOf<String>()
    internal val bodyRedactions = mutableListOf<BodyRedaction>()
    internal var replacementText: String = "********"

    /**
     * Internal representation of a body redaction pattern with its replacement strategy.
     *
     * @property pattern The regex pattern to match
     * @property replacer Function that produces the replacement string given a match and replacement text
     */
    internal data class BodyRedaction(
        val pattern: Regex,
        val replacer: (MatchResult, String) -> String,
    )

    /**
     * Adds a header name to be redacted. The header value will be replaced with the
     * configured replacement text. Comparison is case-insensitive.
     *
     * @param name The header name to redact (e.g., "Authorization", "Cookie", "X-Api-Key")
     * @return this configuration for chaining
     */
    fun redactHeader(name: String): RedactionConfig {
        headersToRedact.add(name.lowercase())
        return this
    }

    /**
     * Adds a regex pattern to be redacted in request and response bodies.
     * The entire matched pattern will be replaced with the replacement text.
     *
     * For JSON/XML value-only redaction, use [redactJsonValue] or [redactXmlValue] instead.
     *
     * @param pattern A regex pattern to match (e.g., "api_key=\\w+", "Bearer \\S+")
     * @return this configuration for chaining
     */
    fun redactBody(pattern: String): RedactionConfig {
        val regex = Regex(pattern, RegexOption.IGNORE_CASE)
        bodyRedactions.add(BodyRedaction(regex) { _, replacement -> replacement })
        return this
    }

    /**
     * Redacts JSON values for the specified key.
     * Handles both string values and non-string values (numbers, booleans, null).
     *
     * Example: `redactJsonValue("password")` will transform:
     * - `"password":"secret123"` -> `"password":"********"`
     * - `"password": "secret123"` -> `"password": "********"`
     * - `"password":null` -> `"password":"********"`
     *
     * @param key The JSON key whose value should be redacted
     * @return this configuration for chaining
     */
    fun redactJsonValue(key: String): RedactionConfig {
        // Pattern matches: "key" : "value" or "key" : value (for non-strings)
        // Group 1: "key" with optional whitespace and colon
        // Group 2: the value (either quoted string or unquoted literal)
        val pattern = Regex(
            """"${Regex.escape(key)}"\s*:\s*("(?:[^"\\]|\\.)*"|[^,}\]\s]+)""",
            RegexOption.IGNORE_CASE,
        )
        bodyRedactions.add(
            BodyRedaction(pattern) { match, replacement ->
                val keyPart = match.value.substringBefore(match.groupValues[1])
                """$keyPart"$replacement""""
            },
        )
        return this
    }

    /**
     * Redacts XML element values for the specified tag.
     * Tag matching is case-insensitive.
     *
     * Example: `redactXmlValue("password")` will transform:
     * - `<password>secret123</password>` -> `<password>********</password>`
     * - `<Password>secret123</Password>` -> `<Password>********</Password>`
     *
     * @param tag The XML tag whose content should be redacted
     * @return this configuration for chaining
     */
    fun redactXmlValue(tag: String): RedactionConfig {
        // Pattern matches: <tag>value</tag> with case-insensitive tag matching
        val pattern = Regex(
            """(<${Regex.escape(tag)}>)(.*?)(</\s*${Regex.escape(tag)}>)""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        )
        bodyRedactions.add(
            BodyRedaction(pattern) { match, replacement ->
                "${match.groupValues[1]}$replacement${match.groupValues[3]}"
            },
        )
        return this
    }

    /**
     * Sets the replacement text for redacted content.
     *
     * @param text The text to substitute for redacted content (default: "********")
     * @return this configuration for chaining
     */
    fun replacement(text: String): RedactionConfig {
        replacementText = text
        return this
    }

    /**
     * Applies all configured redactions to the given text.
     *
     * @param text The text to process
     * @return The text with all matching patterns replaced
     */
    internal fun applyRedactions(text: String): String {
        var result = text
        bodyRedactions.forEach { redaction ->
            result = redaction.pattern.replace(result) { match ->
                redaction.replacer(match, replacementText)
            }
        }
        return result
    }
}

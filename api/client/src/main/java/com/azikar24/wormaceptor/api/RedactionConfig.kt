package com.azikar24.wormaceptor.api

/**
 * Configuration for data redaction in WormaCeptor.
 * Allows masking sensitive information in headers and request/response bodies.
 */
class RedactionConfig {
    internal val headersToRedact = mutableSetOf<String>()
    internal val bodyRedactions = mutableListOf<BodyRedaction>()
    internal var replacementText: String = "********"

    /**
     * Represents a body redaction pattern with its replacement strategy.
     */
    internal data class BodyRedaction(
        val pattern: Regex,
        val replacer: (MatchResult, String) -> String,
    )

    /**
     * Adds a header name to be redacted. Comparison is case-insensitive.
     */
    fun redactHeader(name: String): RedactionConfig {
        headersToRedact.add(name.lowercase())
        return this
    }

    /**
     * Adds a regex pattern to be redacted in the request and response body.
     * The entire matched pattern will be replaced with the replacement text.
     *
     * For JSON/XML value-only redaction, use [redactJsonValue] or [redactXmlValue] instead.
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
     *
     * Example: `redactXmlValue("password")` will transform:
     * - `<password>secret123</password>` -> `<password>********</password>`
     * - `<Password>secret123</Password>` -> `<Password>********</Password>`
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
     * Sets the replacement text for redacted content. Default is "********".
     */
    fun replacement(text: String): RedactionConfig {
        replacementText = text
        return this
    }

    /**
     * Applies all configured redactions to the given text.
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

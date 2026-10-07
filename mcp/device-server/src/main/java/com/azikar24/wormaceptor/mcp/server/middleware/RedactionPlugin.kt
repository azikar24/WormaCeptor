package com.azikar24.wormaceptor.mcp.server.middleware

internal object RedactionHelper {

    fun redactHeaders(
        headers: Map<String, List<String>>,
        redactedHeaders: Set<String>,
    ): Map<String, List<String>> {
        if (redactedHeaders.isEmpty()) return headers

        return headers.mapValues { (key, values) ->
            if (redactedHeaders.any { it.equals(key, ignoreCase = true) }) {
                values.map { REDACTED_PLACEHOLDER }
            } else {
                values
            }
        }
    }

    fun redactBody(
        body: String,
        redactedPatterns: List<Regex>,
    ): String {
        if (redactedPatterns.isEmpty()) return body

        var result = body
        redactedPatterns.forEach { pattern ->
            result = pattern.replace(result, REDACTED_PLACEHOLDER)
        }
        return result
    }

    private const val REDACTED_PLACEHOLDER = "[REDACTED]"
}

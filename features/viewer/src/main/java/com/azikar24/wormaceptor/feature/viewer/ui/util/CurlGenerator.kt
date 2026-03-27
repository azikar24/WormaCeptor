package com.azikar24.wormaceptor.feature.viewer.ui.util

/**
 * Generates shell-safe cURL commands from HTTP transaction data.
 * All values are single-quoted to prevent injection and variable expansion.
 */
object CurlGenerator {

    private val METHODS_WITH_BODY = setOf("POST", "PUT", "PATCH", "DELETE")

    /** Builds a shell-safe cURL command from the given HTTP transaction components. */
    fun generate(
        method: String,
        url: String,
        headers: Map<String, List<String>>,
        body: String? = null,
    ): String = buildString {
        val sanitizedMethod = sanitizeMethod(method)
        append("curl -X $sanitizedMethod ${shellQuote(url)}")

        headers.forEach { (key, values) ->
            values.forEach { value ->
                append(" -H ${shellQuote("$key: $value")}")
            }
        }

        if (!body.isNullOrEmpty() && sanitizedMethod.uppercase() in METHODS_WITH_BODY) {
            append(" -d ${shellQuote(body)}")
        }
    }

    /** Single-quotes a string, escaping embedded single quotes with `'\''`. */
    internal fun shellQuote(value: String): String {
        val escaped = value.replace("'", "'\\''")
        return "'$escaped'"
    }

    /** Strips non-alpha characters to prevent shell injection via method names. */
    internal fun sanitizeMethod(method: String): String {
        return method.replace(Regex("[^A-Za-z]"), "").ifEmpty { "GET" }
    }
}

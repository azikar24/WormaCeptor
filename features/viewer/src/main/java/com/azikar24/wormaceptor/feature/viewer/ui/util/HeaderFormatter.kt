package com.azikar24.wormaceptor.feature.viewer.ui.util

/**
 * Formats HTTP headers from a map into a human-readable string.
 *
 * Each header is rendered as `key: value1, value2` with entries separated by newlines.
 *
 * @param headers Map of header names to their value lists.
 * @return A newline-separated string representation of the headers.
 */
fun formatHeaders(headers: Map<String, List<String>>): String {
    return headers.entries.joinToString("\n") { (key, values) ->
        "$key: ${values.joinToString(", ")}"
    }
}

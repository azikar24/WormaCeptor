package com.azikar24.wormaceptor.feature.viewer

internal class FormatHeadersUseCase {
    operator fun invoke(headers: Map<String, List<String>>): String =
        headers.entries.joinToString("\n") { (key, values) ->
            "$key: ${values.joinToString(", ")}"
        }
}

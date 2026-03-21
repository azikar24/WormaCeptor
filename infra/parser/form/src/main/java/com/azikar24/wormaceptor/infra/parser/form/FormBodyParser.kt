package com.azikar24.wormaceptor.infra.parser.form

import com.azikar24.wormaceptor.domain.contracts.BaseBodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.FormDataParser
import com.azikar24.wormaceptor.domain.contracts.ParsedBody
import com.azikar24.wormaceptor.domain.entities.FormParameter
import java.net.URLDecoder

/**
 * Parser for URL-encoded form data (application/x-www-form-urlencoded).
 *
 * Decodes key-value pairs from URL-encoded strings, handling
 * percent-encoded characters. Also implements [FormDataParser] for
 * typed access from viewer composables.
 */
class FormBodyParser : BaseBodyParser(), FormDataParser {

    override val supportedContentTypes: List<String> = listOf(
        "application/x-www-form-urlencoded",
    )

    override val priority: Int = PRIORITY

    override val defaultContentType: ContentType = ContentType.FORM_DATA

    override fun canParse(
        contentType: String?,
        body: ByteArray,
    ): Boolean {
        if (contentType != null) {
            val mime = contentType.split(";").firstOrNull()?.trim()?.lowercase() ?: ""
            if (mime.contains("x-www-form-urlencoded")) {
                return true
            }
        }
        return false
    }

    override fun parseBody(body: ByteArray): ParsedBody {
        val text = String(body, Charsets.UTF_8)
        val params = parse(text)
        val formatted = params.joinToString("\n") { "${it.key}=${it.value}" }
        return ParsedBody(
            formatted = formatted,
            contentType = ContentType.FORM_DATA,
            metadata = mapOf("paramCount" to params.size.toString()),
            isValid = params.isNotEmpty(),
        )
    }

    override fun parse(formData: String): List<FormParameter> {
        if (formData.isBlank()) return emptyList()

        return try {
            formData
                .split("&")
                .filter { it.isNotBlank() }
                .mapNotNull { pair ->
                    val parts = pair.split("=", limit = 2)
                    if (parts.isEmpty()) return@mapNotNull null

                    val key = try {
                        URLDecoder.decode(parts[0], "UTF-8")
                    } catch (_: Exception) {
                        parts[0]
                    }

                    val value = if (parts.size > 1) {
                        try {
                            URLDecoder.decode(parts[1], "UTF-8")
                        } catch (_: Exception) {
                            parts[1]
                        }
                    } else {
                        ""
                    }

                    FormParameter(key = key, value = value)
                }
        } catch (_: Exception) {
            listOf(FormParameter(key = "raw", value = formData))
        }
    }

    companion object {
        private const val PRIORITY = 300
    }
}

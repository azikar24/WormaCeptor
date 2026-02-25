package com.azikar24.wormaceptor.infra.parser.json

import com.azikar24.wormaceptor.domain.contracts.BaseBodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ParsedBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

/**
 * Parser for JSON content.
 *
 * Features:
 * - Pretty-print with configurable indentation
 * - JSON5 tolerance (trailing commas, single-line comments)
 * - Metadata extraction (root type, key count)
 */
class JsonBodyParser(
    private val indentSpaces: Int = 2,
) : BaseBodyParser() {

    override val supportedContentTypes: List<String> = listOf(
        "application/json",
        "text/json",
        "application/vnd.api+json",
        "application/hal+json",
        "application/ld+json",
        "application/json-patch+json",
    )

    override val priority: Int = 250

    override val defaultContentType: ContentType = ContentType.JSON

    override fun canParse(
        contentType: String?,
        body: ByteArray,
    ): Boolean {
        // Check content type first
        if (contentType != null) {
            val mimeType = contentType.split(";").firstOrNull()?.trim()?.lowercase()
            if (mimeType != null && supportedContentTypes.any { mimeType.contains(it) || it.contains(mimeType) }) {
                return true
            }
            // Also check for +json suffix
            if (mimeType?.endsWith("+json") == true) {
                return true
            }
        }

        // Content inspection: check if body starts with JSON markers
        if (body.isEmpty()) return false

        val trimmed = trimWhitespace(body)
        if (trimmed.isEmpty()) return false

        val firstByte = trimmed[0].toInt().toChar()
        return firstByte == '{' || firstByte == '['
    }

    override fun parseBody(body: ByteArray): ParsedBody {
        return try {
            val jsonString = String(body, Charsets.UTF_8)
            val cleanedJson = cleanJson5Syntax(jsonString)
            val tokener = JSONTokener(cleanedJson)
            val parsed = tokener.nextValue()

            val (formatted, metadata) = when (parsed) {
                is JSONObject -> {
                    Pair(
                        parsed.toString(indentSpaces),
                        mapOf(
                            "rootType" to "object",
                            "keyCount" to parsed.length().toString(),
                        ),
                    )
                }
                is JSONArray -> {
                    Pair(
                        parsed.toString(indentSpaces),
                        mapOf(
                            "rootType" to "array",
                            "elementCount" to parsed.length().toString(),
                        ),
                    )
                }
                else -> {
                    Pair(parsed.toString(), mapOf("rootType" to "primitive"))
                }
            }

            ParsedBody(
                formatted = formatted,
                contentType = ContentType.JSON,
                metadata = metadata,
                isValid = true,
            )
        } catch (e: JSONException) {
            // Return raw content with error
            ParsedBody(
                formatted = String(body, Charsets.UTF_8),
                contentType = ContentType.JSON,
                isValid = false,
                errorMessage = "Invalid JSON: ${e.message}",
            )
        }
    }

    /**
     * Clean JSON5 syntax to make it valid JSON.
     * Handles:
     * - Trailing commas
     * - Single-line comments
     */
    private fun cleanJson5Syntax(json: String): String {
        val sb = StringBuilder()
        var i = 0
        var inString = false
        var escape = false

        while (i < json.length) {
            val c = json[i]

            if (escape) {
                sb.append(c)
                escape = false
                i++
                continue
            }

            if (c == '\\' && inString) {
                sb.append(c)
                escape = true
                i++
                continue
            }

            if (c == '"') {
                inString = !inString
                sb.append(c)
                i++
                continue
            }

            if (!inString) {
                // Handle single-line comments
                if (c == '/' && i + 1 < json.length && json[i + 1] == '/') {
                    // Skip until end of line
                    while (i < json.length && json[i] != '\n') {
                        i++
                    }
                    continue
                }

                // Handle trailing commas before ] or }
                if (c == ',') {
                    // Look ahead for ] or } (skipping whitespace)
                    var j = i + 1
                    while (j < json.length && json[j].isWhitespace()) {
                        j++
                    }
                    if (j < json.length && (json[j] == ']' || json[j] == '}')) {
                        // Skip the trailing comma
                        i++
                        continue
                    }
                }
            }

            sb.append(c)
            i++
        }

        return sb.toString()
    }

    private fun trimWhitespace(body: ByteArray): ByteArray {
        var start = 0
        while (start < body.size && body[start].toInt().toChar().isWhitespace()) {
            start++
        }
        return if (start < body.size) body.copyOfRange(start, body.size) else ByteArray(0)
    }
}

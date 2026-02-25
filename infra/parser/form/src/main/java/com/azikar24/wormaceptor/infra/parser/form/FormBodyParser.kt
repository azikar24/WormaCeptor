package com.azikar24.wormaceptor.infra.parser.form

import com.azikar24.wormaceptor.domain.contracts.BaseBodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ParsedBody
import java.net.URLDecoder

/**
 * Parser for URL-encoded form data (application/x-www-form-urlencoded).
 *
 * Features:
 * - URL-decoded key-value display
 * - Support for nested parameters using bracket notation
 * - Array notation handling (param[]=value)
 */
class FormBodyParser : BaseBodyParser() {

    override val supportedContentTypes: List<String> = listOf(
        "application/x-www-form-urlencoded",
    )

    override val priority: Int = 220

    override val defaultContentType: ContentType = ContentType.FORM_DATA

    override fun canParse(
        contentType: String?,
        body: ByteArray,
    ): Boolean {
        // Check content type first
        if (contentType != null) {
            val mimeType = contentType.split(";").firstOrNull()?.trim()?.lowercase()
            if (mimeType == "application/x-www-form-urlencoded") {
                return true
            }
        }

        // Content inspection: check for key=value&key2=value2 pattern
        if (body.isEmpty()) return false

        val content = String(body, Charsets.UTF_8)
        return isLikelyFormData(content)
    }

    override fun parseBody(body: ByteArray): ParsedBody {
        return try {
            val content = String(body, Charsets.UTF_8)
            val params = parseFormData(content)
            val formatted = formatParams(params)

            ParsedBody(
                formatted = formatted,
                contentType = ContentType.FORM_DATA,
                metadata = mapOf(
                    "parameterCount" to params.size.toString(),
                ),
                isValid = true,
            )
        } catch (e: Exception) {
            ParsedBody(
                formatted = String(body, Charsets.UTF_8),
                contentType = ContentType.FORM_DATA,
                isValid = false,
                errorMessage = "Form data parsing error: ${e.message}",
            )
        }
    }

    private fun isLikelyFormData(content: String): Boolean {
        // Must contain at least one = sign
        if (!content.contains('=')) return false

        // Should not start with JSON/XML markers
        val trimmed = content.trim()
        if (trimmed.startsWith("{") || trimmed.startsWith("[") ||
            trimmed.startsWith("<") || trimmed.startsWith("<!")
        ) {
            return false
        }

        // Check for valid form data pattern
        val pairs = content.split('&')
        if (pairs.isEmpty()) return false

        // At least one valid key=value pair
        return pairs.any { pair ->
            val parts = pair.split('=', limit = 2)
            parts.size >= 1 && parts[0].isNotEmpty() &&
                isValidParameterName(parts[0])
        }
    }

    private fun isValidParameterName(name: String): Boolean {
        // URL-decoded name should contain valid characters
        return try {
            val decoded = URLDecoder.decode(name, "UTF-8")
            decoded.all { c ->
                c.isLetterOrDigit() || c in setOf('_', '-', '.', '[', ']')
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun parseFormData(content: String): List<FormParameter> {
        val params = mutableListOf<FormParameter>()

        content.split('&').forEach { pair ->
            if (pair.isEmpty()) return@forEach

            val parts = pair.split('=', limit = 2)
            val rawKey = parts[0]
            val rawValue = if (parts.size > 1) parts[1] else ""

            try {
                val key = URLDecoder.decode(rawKey, "UTF-8")
                val value = URLDecoder.decode(rawValue, "UTF-8")
                val (baseName, arrayIndex, nestedKeys) = parseParameterName(key)

                params.add(
                    FormParameter(
                        rawKey = rawKey,
                        key = baseName,
                        value = value,
                        arrayIndex = arrayIndex,
                        nestedKeys = nestedKeys,
                    ),
                )
            } catch (e: Exception) {
                // Keep raw values if decoding fails
                params.add(
                    FormParameter(
                        rawKey = rawKey,
                        key = rawKey,
                        value = rawValue,
                        decodingFailed = true,
                    ),
                )
            }
        }

        return params
    }

    /**
     * Parse parameter name with bracket notation.
     * Examples:
     * - "name" -> ("name", null, [])
     * - "items[]" -> ("items", -1, [])  // -1 indicates array push
     * - "items[0]" -> ("items", 0, [])
     * - "user[name]" -> ("user", null, ["name"])
     * - "user[address][city]" -> ("user", null, ["address", "city"])
     */
    private fun parseParameterName(name: String): Triple<String, Int?, List<String>> {
        val bracketIndex = name.indexOf('[')
        if (bracketIndex == -1) {
            return Triple(name, null, emptyList())
        }

        val baseName = name.substring(0, bracketIndex)
        val nestedKeys = mutableListOf<String>()
        var arrayIndex: Int? = null

        val bracketPattern = """\[([^\]]*)\]""".toRegex()
        val matches = bracketPattern.findAll(name.substring(bracketIndex))

        matches.forEachIndexed { index, match ->
            val bracketContent = match.groupValues[1]
            when {
                bracketContent.isEmpty() -> {
                    // Empty brackets [] indicate array push
                    if (index == 0) arrayIndex = -1
                }
                bracketContent.all { it.isDigit() } -> {
                    // Numeric index
                    if (index == 0) {
                        arrayIndex = bracketContent.toIntOrNull()
                    } else {
                        nestedKeys.add(bracketContent)
                    }
                }
                else -> {
                    // Named key
                    nestedKeys.add(bracketContent)
                }
            }
        }

        return Triple(baseName, arrayIndex, nestedKeys)
    }

    private fun formatParams(params: List<FormParameter>): String {
        if (params.isEmpty()) return ""

        val maxKeyWidth = params.maxOfOrNull { param ->
            formatKey(param).length
        } ?: 0

        return params.joinToString("\n") { param ->
            val formattedKey = formatKey(param)
            val padding = " ".repeat(maxOf(0, maxKeyWidth - formattedKey.length + 1))
            val valueDisplay = if (param.value.length > 100) {
                param.value.take(100) + "... (${param.value.length} chars)"
            } else {
                param.value
            }

            val warning = if (param.decodingFailed) " [decode failed]" else ""
            "$formattedKey$padding= $valueDisplay$warning"
        }
    }

    private fun formatKey(param: FormParameter): String {
        val sb = StringBuilder(param.key)

        when {
            param.arrayIndex == -1 -> sb.append("[]")
            param.arrayIndex != null -> sb.append("[${param.arrayIndex}]")
        }

        param.nestedKeys.forEach { key ->
            sb.append("[$key]")
        }

        return sb.toString()
    }
}

/**
 * Represents a parsed form parameter.
 */
data class FormParameter(
    val rawKey: String,
    val key: String,
    val value: String,
    val arrayIndex: Int? = null,
    val nestedKeys: List<String> = emptyList(),
    val decodingFailed: Boolean = false,
)

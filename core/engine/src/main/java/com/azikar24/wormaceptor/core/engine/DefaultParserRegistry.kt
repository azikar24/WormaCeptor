package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.contracts.BodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ParsedBody
import java.util.Locale

/**
 * Default implementation of [ParserRegistry].
 *
 * Parsers are sorted by priority (descending) and the first parser
 * that can handle the content is used. Includes header-based MIME type
 * detection and content inspection fallback.
 */
class DefaultParserRegistry : ParserRegistry {
    private val parsers = mutableListOf<BodyParser>()

    override fun register(parser: BodyParser) {
        parsers.add(parser)
        parsers.sortByDescending { it.priority }
    }

    override fun parseBody(
        contentType: String?,
        body: ByteArray,
    ): ParsedBody {
        if (body.isEmpty()) {
            return ParsedBody(
                formatted = "",
                contentType = ContentType.UNKNOWN,
                isValid = true,
            )
        }

        val parser = findParser(contentType, body)
        return parser?.parse(body) ?: createFallbackParsedBody(body)
    }

    override fun detectContentType(
        contentType: String?,
        body: ByteArray,
    ): ContentType {
        // Header-based detection first
        val headerType = detectFromHeader(contentType)
        if (headerType != null) return headerType

        if (body.isEmpty()) return ContentType.UNKNOWN

        // Try registered parsers
        val parser = findParser(contentType, body)
        if (parser != null) {
            return parser.parse(body).contentType
        }

        // Content inspection fallback
        if (isLikelyText(body)) {
            val text = String(body, Charsets.UTF_8)
            return detectFromContent(text)
        }

        return ContentType.BINARY
    }

    override fun detectContentType(
        contentTypeHeader: String?,
        body: String?,
    ): ContentType {
        // Header-based detection first (works even without body)
        val headerType = detectFromHeader(contentTypeHeader)
        if (headerType != null) return headerType

        if (body == null) return ContentType.UNKNOWN

        // Content inspection fallback
        return detectFromContent(body)
    }

    override fun extractMultipartBoundary(contentType: String): String? {
        val regex = Regex("""boundary\s*=\s*"?([^";]+)"?""", RegexOption.IGNORE_CASE)
        return regex.find(contentType)?.groupValues?.getOrNull(1)
    }

    @Suppress("CyclomaticComplexity")
    private fun detectFromHeader(contentType: String?): ContentType? {
        if (contentType == null) return null
        val mime = contentType.split(";").firstOrNull()?.trim()?.lowercase() ?: return null

        return when {
            mime.contains("json") || mime.endsWith("+json") -> ContentType.JSON
            mime.contains("xml") || mime.endsWith("+xml") -> ContentType.XML
            mime.contains("html") -> ContentType.HTML
            mime.contains("x-www-form-urlencoded") -> ContentType.FORM_DATA
            mime.contains("multipart") -> ContentType.MULTIPART
            mime.contains("protobuf") || mime.contains("grpc") ||
                mime.endsWith("+proto") -> ContentType.PROTOBUF
            mime.contains("pdf") -> ContentType.PDF
            mime.contains("image/png") -> ContentType.IMAGE_PNG
            mime.contains("image/jpeg") || mime.contains("image/jpg") -> ContentType.IMAGE_JPEG
            mime.contains("image/gif") -> ContentType.IMAGE_GIF
            mime.contains("image/webp") -> ContentType.IMAGE_WEBP
            mime.contains("image/svg") -> ContentType.IMAGE_SVG
            mime.startsWith("image/") -> ContentType.IMAGE_OTHER
            mime.startsWith("text/") -> ContentType.PLAIN_TEXT
            else -> null
        }
    }

    private fun detectFromContent(body: String): ContentType {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return ContentType.PLAIN_TEXT

        return when {
            isLikelyJson(trimmed) -> ContentType.JSON
            trimmed.startsWith("<?xml") -> ContentType.XML
            trimmed.startsWith("<") && !trimmed.startsWith("<!DOCTYPE html") &&
                !trimmed.lowercase().startsWith("<html") -> ContentType.XML
            trimmed.lowercase().contains("<!doctype html") ||
                trimmed.lowercase().startsWith("<html") -> ContentType.HTML
            trimmed.contains("=") && trimmed.contains("&") &&
                !trimmed.contains("<") && !trimmed.contains("{") -> ContentType.FORM_DATA
            else -> ContentType.PLAIN_TEXT
        }
    }

    private fun isLikelyJson(trimmed: String): Boolean {
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) ||
            (trimmed.startsWith("[") && trimmed.endsWith("]"))
        ) {
            return try {
                if (trimmed.startsWith("{")) {
                    org.json.JSONObject(trimmed)
                } else {
                    org.json.JSONArray(trimmed)
                }
                true
            } catch (_: Exception) {
                false
            }
        }
        return false
    }

    private fun findParser(
        contentType: String?,
        body: ByteArray,
    ): BodyParser? {
        return parsers.firstOrNull { it.canParse(contentType, body) }
    }

    private fun createFallbackParsedBody(body: ByteArray): ParsedBody {
        return if (isLikelyText(body)) {
            ParsedBody(
                formatted = String(body, Charsets.UTF_8),
                contentType = ContentType.PLAIN_TEXT,
                isValid = true,
            )
        } else {
            ParsedBody(
                formatted = formatBinaryPreview(body),
                contentType = ContentType.BINARY,
                metadata = mapOf("size" to body.size.toString()),
                isValid = true,
            )
        }
    }

    @Suppress("MagicNumber")
    private fun isLikelyText(body: ByteArray): Boolean {
        if (body.isEmpty()) return true

        val sampleSize = minOf(body.size, 1024)
        var nonPrintableCount = 0

        for (i in 0 until sampleSize) {
            val b = body[i].toInt() and 0xFF
            if (b < 9 || b in 14..31 || b == 127) {
                nonPrintableCount++
            }
        }

        return nonPrintableCount.toFloat() / sampleSize < 0.1f
    }

    @Suppress("MagicNumber")
    private fun formatBinaryPreview(body: ByteArray): String {
        val previewSize = minOf(body.size, 256)
        val hex = body.take(previewSize).joinToString(" ") {
            String.format(Locale.US, "%02X", it)
        }
        return if (body.size > previewSize) {
            "$hex\n... (${body.size} bytes total)"
        } else {
            hex
        }
    }
}

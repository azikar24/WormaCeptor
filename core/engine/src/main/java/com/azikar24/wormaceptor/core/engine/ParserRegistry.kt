package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.contracts.BodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ParsedBody

/**
 * Registry for body parsers.
 *
 * Manages parser registration and provides content detection and parsing
 * capabilities using registered parsers.
 */
interface ParserRegistry {
    /**
     * Registers a parser with the registry.
     * Parsers are checked in order of priority (highest first).
     *
     * @param parser The parser to register
     */
    fun register(parser: BodyParser)

    /**
     * Parses body content using the most appropriate registered parser.
     *
     * @param contentType The Content-Type header value, may be null
     * @param body The raw body bytes
     * @return ParsedBody with formatted content and metadata
     */
    fun parseBody(contentType: String?, body: ByteArray): ParsedBody

    /**
     * Detects the content type without fully parsing the body.
     *
     * @param contentType The Content-Type header value, may be null
     * @param body The raw body bytes
     * @return The detected ContentType
     */
    fun detectContentType(contentType: String?, body: ByteArray): ContentType
}

/**
 * Default implementation of [ParserRegistry].
 *
 * Parsers are sorted by priority (descending) and the first parser
 * that can handle the content is used.
 */
class DefaultParserRegistry : ParserRegistry {
    private val parsers = mutableListOf<BodyParser>()

    override fun register(parser: BodyParser) {
        parsers.add(parser)
        parsers.sortByDescending { it.priority }
    }

    override fun parseBody(contentType: String?, body: ByteArray): ParsedBody {
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

    override fun detectContentType(contentType: String?, body: ByteArray): ContentType {
        if (body.isEmpty()) {
            return ContentType.UNKNOWN
        }

        val parser = findParser(contentType, body)
        if (parser != null) {
            // Get content type from parser's supported types
            return parser.parse(body).contentType
        }

        // Fallback: try to determine if it's text or binary
        return if (isLikelyText(body)) ContentType.PLAIN_TEXT else ContentType.BINARY
    }

    private fun findParser(contentType: String?, body: ByteArray): BodyParser? {
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

    private fun isLikelyText(body: ByteArray): Boolean {
        if (body.isEmpty()) return true

        // Check first N bytes for printable ASCII/UTF-8
        val sampleSize = minOf(body.size, 1024)
        var nonPrintableCount = 0

        for (i in 0 until sampleSize) {
            val b = body[i].toInt() and 0xFF
            // Allow common control chars (tab, newline, carriage return) and printable ASCII
            if (b < 9 || (b in 14..31) || b == 127) {
                nonPrintableCount++
            }
        }

        // If more than 10% non-printable, treat as binary
        return nonPrintableCount.toFloat() / sampleSize < 0.1f
    }

    private fun formatBinaryPreview(body: ByteArray): String {
        val previewSize = minOf(body.size, 256)
        val hex = body.take(previewSize).joinToString(" ") {
            String.format("%02X", it)
        }
        return if (body.size > previewSize) {
            "$hex\n... (${body.size} bytes total)"
        } else {
            hex
        }
    }
}

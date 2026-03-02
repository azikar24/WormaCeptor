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
    fun parseBody(
        contentType: String?,
        body: ByteArray,
    ): ParsedBody

    /**
     * Detects the content type from header and/or body bytes.
     *
     * @param contentType The Content-Type header value, may be null
     * @param body The raw body bytes
     * @return The detected ContentType
     */
    fun detectContentType(
        contentType: String?,
        body: ByteArray,
    ): ContentType

    /**
     * Detects the content type from header and/or body string.
     *
     * Convenience overload for string-based content detection.
     *
     * @param contentTypeHeader The Content-Type header value, may be null
     * @param body The body as a string, may be null
     * @return The detected ContentType
     */
    fun detectContentType(
        contentTypeHeader: String?,
        body: String?,
    ): ContentType

    /**
     * Extracts the boundary parameter from a multipart Content-Type header.
     *
     * @param contentType The full Content-Type header value
     * @return The boundary string, or null if not found
     */
    fun extractMultipartBoundary(contentType: String): String?
}

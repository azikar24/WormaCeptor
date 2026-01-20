package com.azikar24.wormaceptor.domain.contracts

/**
 * Interface for body parsers that detect and format different content types.
 *
 * Implementations should be registered with [ParserRegistry] for automatic
 * content type detection and parsing.
 */
interface BodyParser {
    /**
     * List of MIME content types this parser can handle.
     * Example: ["application/json", "text/json"]
     */
    val supportedContentTypes: List<String>

    /**
     * Priority for parser selection. Higher values are checked first.
     * Recommended ranges:
     * - 100-199: Binary formats (protobuf)
     * - 200-299: Structured formats (json, xml)
     * - 300-399: Text formats (html, form data)
     * - 0-99: Fallback parsers
     */
    val priority: Int

    /**
     * Determines if this parser can handle the given content.
     *
     * @param contentType The Content-Type header value, may be null
     * @param body The raw body bytes
     * @return true if this parser can process the content
     */
    fun canParse(contentType: String?, body: ByteArray): Boolean

    /**
     * Parses the body and returns formatted output.
     *
     * @param body The raw body bytes
     * @return Parsed and formatted body with metadata
     */
    fun parse(body: ByteArray): ParsedBody
}

/**
 * Result of parsing a body.
 *
 * @property formatted The formatted string representation
 * @property contentType The detected content type
 * @property metadata Additional metadata extracted during parsing
 * @property isValid Whether the body was successfully parsed
 * @property errorMessage Error message if parsing failed
 */
data class ParsedBody(
    val formatted: String,
    val contentType: ContentType,
    val metadata: Map<String, String> = emptyMap(),
    val isValid: Boolean = true,
    val errorMessage: String? = null,
)

/**
 * Supported content types for body parsing.
 */
enum class ContentType {
    JSON,
    XML,
    HTML,
    PROTOBUF,
    FORM_DATA,
    MULTIPART,
    PLAIN_TEXT,
    BINARY,
    PDF,
    IMAGE_PNG,
    IMAGE_JPEG,
    IMAGE_GIF,
    IMAGE_WEBP,
    IMAGE_SVG,
    IMAGE_BMP,
    IMAGE_ICO,
    IMAGE_OTHER,
    UNKNOWN,
}

/**
 * Checks if the content type represents an image format.
 */
fun ContentType.isImage(): Boolean = when (this) {
    ContentType.IMAGE_PNG,
    ContentType.IMAGE_JPEG,
    ContentType.IMAGE_GIF,
    ContentType.IMAGE_WEBP,
    ContentType.IMAGE_SVG,
    ContentType.IMAGE_BMP,
    ContentType.IMAGE_ICO,
    ContentType.IMAGE_OTHER,
    -> true
    else -> false
}

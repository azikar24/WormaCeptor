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
    fun canParse(
        contentType: String?,
        body: ByteArray,
    ): Boolean

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
    /** application/json structured data. */
    JSON,

    /** application/xml or text/xml structured data. */
    XML,

    /** text/html web page content. */
    HTML,

    /** application/protobuf binary serialization. */
    PROTOBUF,

    /** application/x-www-form-urlencoded key-value pairs. */
    FORM_DATA,

    /** Multipart content with boundary-separated parts. */
    MULTIPART,

    /** text/plain unstructured text. */
    PLAIN_TEXT,

    /** Opaque binary data with no specific format. */
    BINARY,

    /** application/pdf document. */
    PDF,

    /** image/png raster image. */
    IMAGE_PNG,

    /** image/jpeg raster image. */
    IMAGE_JPEG,

    /** image/gif animated or static image. */
    IMAGE_GIF,

    /** image/webp raster image. */
    IMAGE_WEBP,

    /** image/svg+xml vector image. */
    IMAGE_SVG,

    /** image/bmp bitmap image. */
    IMAGE_BMP,

    /** image/x-icon favicon. */
    IMAGE_ICO,

    /** Image format not covered by a specific variant. */
    IMAGE_OTHER,

    /** Content type could not be determined. */
    UNKNOWN,
}

/**
 * Creates a ParsedBody for empty body content.
 * Use this in parser implementations to handle empty input consistently.
 *
 * @param contentType The content type to report
 * @param formatted Optional formatted string (defaults to empty)
 * @return ParsedBody representing an empty but valid body
 */
fun emptyParsedBody(
    contentType: ContentType,
    formatted: String = "",
): ParsedBody = ParsedBody(
    formatted = formatted,
    contentType = contentType,
    isValid = true,
)

/**
 * Abstract base class for [BodyParser] implementations that provides
 * consistent empty body handling.
 *
 * Subclasses implement [parseBody] for non-empty content. The empty body
 * check is handled automatically using [defaultContentType] and
 * [emptyBodyFormatted].
 */
abstract class BaseBodyParser : BodyParser {

    /**
     * The content type returned when the body is empty.
     */
    protected abstract val defaultContentType: ContentType

    /**
     * Optional formatted text for the empty body response.
     * Override to provide a custom placeholder (e.g. "[Empty PDF]").
     */
    protected open val emptyBodyFormatted: String = ""

    final override fun parse(body: ByteArray): ParsedBody {
        if (body.isEmpty()) {
            return emptyParsedBody(defaultContentType, emptyBodyFormatted)
        }
        return parseBody(body)
    }

    /**
     * Parses a non-empty body. Called only when [body] is not empty.
     *
     * @param body The raw body bytes (guaranteed non-empty)
     * @return Parsed and formatted body with metadata
     */
    protected abstract fun parseBody(body: ByteArray): ParsedBody
}

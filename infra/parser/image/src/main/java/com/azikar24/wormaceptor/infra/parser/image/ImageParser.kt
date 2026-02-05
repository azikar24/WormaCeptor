package com.azikar24.wormaceptor.infra.parser.image

import com.azikar24.wormaceptor.domain.contracts.BodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ParsedBody
import java.util.Locale

/**
 * Parser for image content types.
 *
 * Detects image formats using magic bytes and extracts metadata
 * (dimensions, color space, etc.) without requiring external image libraries.
 *
 * Supported formats:
 * - PNG: 8-byte signature
 * - JPEG: FF D8 FF signature
 * - GIF: GIF87a/GIF89a signature
 * - WebP: RIFF/WEBP container
 * - BMP: BM signature
 * - ICO: 00 00 01 00 signature
 * - SVG: XML-based, detected by content
 */
class ImageParser : BodyParser {

    override val supportedContentTypes: List<String> = listOf(
        "image/png",
        "image/jpeg",
        "image/jpg",
        "image/gif",
        "image/webp",
        "image/svg+xml",
        "image/bmp",
        "image/x-bmp",
        "image/x-icon",
        "image/vnd.microsoft.icon",
        "image/ico",
    )

    /**
     * Priority 150 - between binary formats (100-199) and structured text (200-299).
     * Images are binary but have structured headers we can parse.
     */
    override val priority: Int = 150

    override fun canParse(contentType: String?, body: ByteArray): Boolean {
        // First check content-type header
        if (contentType != null) {
            val normalizedType = contentType.lowercase().split(";").first().trim()
            if (supportedContentTypes.any { normalizedType.startsWith(it) }) {
                return true
            }
            // Also check for generic "image/" prefix
            if (normalizedType.startsWith("image/")) {
                return true
            }
        }

        // Fall back to magic byte detection
        return MagicByteDetector.detect(body) != null
    }

    override fun parse(body: ByteArray): ParsedBody {
        if (body.isEmpty()) {
            return ParsedBody(
                formatted = "[Empty image data]",
                contentType = ContentType.BINARY,
                isValid = false,
                errorMessage = "Empty body",
            )
        }

        // Detect format
        val format = MagicByteDetector.detect(body)

        if (format == null) {
            return ParsedBody(
                formatted = "[Unknown image format - ${body.size} bytes]",
                contentType = ContentType.IMAGE_OTHER,
                metadata = mapOf(
                    "size" to body.size.toString(),
                    "sizeFormatted" to formatSize(body.size.toLong()),
                ),
                isValid = false,
                errorMessage = "Could not detect image format from magic bytes",
            )
        }

        // Extract metadata
        val metadata = try {
            ImageMetadataExtractor.extract(body, format)
        } catch (e: Exception) {
            ImageMetadata.unknown(body.size.toLong()).copy(format = format.displayName)
        }

        // Build formatted display string
        val formatted = buildFormattedString(metadata)

        return ParsedBody(
            formatted = formatted,
            contentType = format.contentType,
            metadata = buildMetadataMap(metadata),
            isValid = metadata.width > 0 && metadata.height > 0,
        )
    }

    /**
     * Parses image data and returns detailed metadata.
     *
     * This is a convenience method for callers who need the full ImageMetadata
     * object rather than just the ParsedBody.
     *
     * @param body The raw image bytes
     * @return ImageMetadata with extracted information, or unknown if parsing fails
     */
    fun parseMetadata(body: ByteArray): ImageMetadata {
        if (body.isEmpty()) {
            return ImageMetadata.unknown(0)
        }

        val format = MagicByteDetector.detect(body)
            ?: return ImageMetadata.unknown(body.size.toLong())

        return try {
            ImageMetadataExtractor.extract(body, format)
        } catch (e: Exception) {
            ImageMetadata.unknown(body.size.toLong()).copy(format = format.displayName)
        }
    }

    /**
     * Detects the image format without parsing full metadata.
     *
     * @param body The raw image bytes
     * @return The detected ImageFormat, or null if not recognized
     */
    fun detectFormat(body: ByteArray): ImageFormat? = MagicByteDetector.detect(body)

    /**
     * Detects the ContentType for the given image data.
     *
     * @param body The raw image bytes
     * @return The appropriate ContentType enum value
     */
    fun detectContentType(body: ByteArray): ContentType {
        val format = MagicByteDetector.detect(body)
        return format?.contentType ?: ContentType.IMAGE_OTHER
    }

    private fun buildFormattedString(metadata: ImageMetadata): String {
        return buildString {
            appendLine("[${metadata.format} Image]")
            if (metadata.width > 0 && metadata.height > 0) {
                appendLine("Dimensions: ${metadata.dimensionsString}")
            }
            appendLine("Size: ${metadata.fileSizeString}")
            metadata.colorSpace?.let { appendLine("Color Space: $it") }
            if (metadata.hasAlpha) {
                appendLine("Alpha Channel: Yes")
            }
            metadata.bitDepth?.let { appendLine("Bit Depth: $it-bit") }
        }
    }

    private fun buildMetadataMap(metadata: ImageMetadata): Map<String, String> {
        return buildMap {
            put("format", metadata.format)
            put("width", metadata.width.toString())
            put("height", metadata.height.toString())
            put("dimensions", metadata.dimensionsString)
            put("size", metadata.fileSize.toString())
            put("sizeFormatted", metadata.fileSizeString)
            put("hasAlpha", metadata.hasAlpha.toString())
            metadata.colorSpace?.let { put("colorSpace", it) }
            metadata.bitDepth?.let { put("bitDepth", it.toString()) }
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }

    companion object {
        /**
         * Checks if the given content type string represents an image.
         */
        fun isImageMimeType(contentType: String?): Boolean {
            if (contentType == null) return false
            val normalized = contentType.lowercase().split(";").first().trim()
            return normalized.startsWith("image/")
        }
    }
}

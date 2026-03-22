package com.azikar24.wormaceptor.infra.parser.image

import com.azikar24.wormaceptor.domain.contracts.BaseBodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ImageMetadataExtractor
import com.azikar24.wormaceptor.domain.contracts.ParsedBody
import com.azikar24.wormaceptor.domain.entities.ImageMetadata
import java.util.Locale

/**
 * Parser for image content types.
 *
 * Detects image formats using magic bytes and extracts metadata
 * (dimensions, color space, etc.) without requiring external image libraries.
 *
 * Also implements [ImageMetadataExtractor] for typed Koin injection,
 * allowing viewer composables to access image metadata without depending on infra.
 *
 * Supported formats: PNG, JPEG, GIF, WebP, BMP, ICO, SVG.
 */
class ImageParser : BaseBodyParser(), ImageMetadataExtractor {

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
    override val priority: Int = PRIORITY

    override val defaultContentType: ContentType = ContentType.IMAGE_OTHER

    override val emptyBodyFormatted: String = "[Empty image data]"

    override fun canParse(
        contentType: String?,
        body: ByteArray,
    ): Boolean {
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

    override fun parseBody(body: ByteArray): ParsedBody {
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

        val metadata = try {
            ImageMetadataExtractorImpl.extract(body, format)
        } catch (_: Exception) {
            ImageMetadata.unknown(body.size.toLong()).copy(format = format.displayName)
        }

        val formatted = buildFormattedString(metadata)

        return ParsedBody(
            formatted = formatted,
            contentType = format.contentType,
            metadata = buildMetadataMap(metadata),
            isValid = metadata.width > 0 && metadata.height > 0,
        )
    }

    // --- ImageMetadataExtractor interface ---

    override fun extractMetadata(data: ByteArray): ImageMetadata {
        if (data.isEmpty()) {
            return ImageMetadata.unknown(0)
        }

        val format = MagicByteDetector.detect(data)
            ?: return ImageMetadata.unknown(data.size.toLong())

        return try {
            ImageMetadataExtractorImpl.extract(data, format)
        } catch (_: Exception) {
            ImageMetadata.unknown(data.size.toLong()).copy(format = format.displayName)
        }
    }

    override fun isImageData(data: ByteArray): Boolean {
        return MagicByteDetector.detect(data) != null
    }

    // --- Private helpers ---

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

    @Suppress("MagicNumber")
    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }

    private companion object {
        private const val PRIORITY = 150
    }
}

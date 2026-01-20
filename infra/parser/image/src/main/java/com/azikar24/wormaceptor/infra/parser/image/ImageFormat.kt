package com.azikar24.wormaceptor.infra.parser.image

import com.azikar24.wormaceptor.domain.contracts.ContentType

/**
 * Supported image formats with their magic byte signatures.
 */
enum class ImageFormat(
    val mimeTypes: List<String>,
    val contentType: ContentType,
    val displayName: String,
) {
    PNG(
        mimeTypes = listOf("image/png"),
        contentType = ContentType.IMAGE_PNG,
        displayName = "PNG",
    ),
    JPEG(
        mimeTypes = listOf("image/jpeg", "image/jpg"),
        contentType = ContentType.IMAGE_JPEG,
        displayName = "JPEG",
    ),
    GIF(
        mimeTypes = listOf("image/gif"),
        contentType = ContentType.IMAGE_GIF,
        displayName = "GIF",
    ),
    WEBP(
        mimeTypes = listOf("image/webp"),
        contentType = ContentType.IMAGE_WEBP,
        displayName = "WebP",
    ),
    BMP(
        mimeTypes = listOf("image/bmp", "image/x-bmp"),
        contentType = ContentType.IMAGE_BMP,
        displayName = "BMP",
    ),
    ICO(
        mimeTypes = listOf("image/x-icon", "image/vnd.microsoft.icon", "image/ico"),
        contentType = ContentType.IMAGE_ICO,
        displayName = "ICO",
    ),
    SVG(
        mimeTypes = listOf("image/svg+xml"),
        contentType = ContentType.IMAGE_SVG,
        displayName = "SVG",
    ),
    ;

    companion object {
        /**
         * All supported MIME types across all formats.
         */
        val allMimeTypes: List<String> = entries.flatMap { it.mimeTypes }

        /**
         * Finds format by MIME type.
         */
        fun fromMimeType(mimeType: String): ImageFormat? {
            val normalized = mimeType.lowercase().trim()
            return entries.find { format ->
                format.mimeTypes.any { it.equals(normalized, ignoreCase = true) }
            }
        }
    }
}

package com.azikar24.wormaceptor.infra.parser.image

import com.azikar24.wormaceptor.domain.contracts.ContentType

/**
 * Supported image formats with their magic byte signatures.
 *
 * @property mimeTypes MIME type strings associated with this format.
 * @property contentType Internal content type classification for the format.
 * @property displayName Human-readable name for the format (e.g., "PNG", "JPEG").
 */
enum class ImageFormat(
    val mimeTypes: List<String>,
    val contentType: ContentType,
    val displayName: String,
) {
    /** Portable Network Graphics format. */
    PNG(
        mimeTypes = listOf("image/png"),
        contentType = ContentType.IMAGE_PNG,
        displayName = "PNG",
    ),

    /** Joint Photographic Experts Group format. */
    JPEG(
        mimeTypes = listOf("image/jpeg", "image/jpg"),
        contentType = ContentType.IMAGE_JPEG,
        displayName = "JPEG",
    ),

    /** Graphics Interchange Format. */
    GIF(
        mimeTypes = listOf("image/gif"),
        contentType = ContentType.IMAGE_GIF,
        displayName = "GIF",
    ),

    /** WebP image format developed by Google. */
    WEBP(
        mimeTypes = listOf("image/webp"),
        contentType = ContentType.IMAGE_WEBP,
        displayName = "WebP",
    ),

    /** Windows Bitmap format. */
    BMP(
        mimeTypes = listOf("image/bmp", "image/x-bmp"),
        contentType = ContentType.IMAGE_BMP,
        displayName = "BMP",
    ),

    /** Windows Icon format. */
    ICO(
        mimeTypes = listOf("image/x-icon", "image/vnd.microsoft.icon", "image/ico"),
        contentType = ContentType.IMAGE_ICO,
        displayName = "ICO",
    ),

    /** Scalable Vector Graphics format. */
    SVG(
        mimeTypes = listOf("image/svg+xml"),
        contentType = ContentType.IMAGE_SVG,
        displayName = "SVG",
    ),
}

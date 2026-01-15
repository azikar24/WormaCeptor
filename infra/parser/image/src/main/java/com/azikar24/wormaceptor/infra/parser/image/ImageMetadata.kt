package com.azikar24.wormaceptor.infra.parser.image

/**
 * Metadata extracted from an image file.
 *
 * @property width Image width in pixels
 * @property height Image height in pixels
 * @property format Human-readable format name (e.g., "PNG", "JPEG")
 * @property colorSpace Color space if detectable (e.g., "sRGB", "CMYK")
 * @property hasAlpha Whether the image has an alpha channel
 * @property bitDepth Bits per channel if detectable
 * @property fileSize Size of the image data in bytes
 */
data class ImageMetadata(
    val width: Int,
    val height: Int,
    val format: String,
    val colorSpace: String? = null,
    val hasAlpha: Boolean = false,
    val bitDepth: Int? = null,
    val fileSize: Long
) {
    /**
     * Returns formatted dimensions string (e.g., "1920x1080").
     */
    val dimensionsString: String
        get() = "${width}x${height}"

    /**
     * Returns human-readable file size string.
     */
    val fileSizeString: String
        get() = when {
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            else -> String.format("%.2f MB", fileSize / (1024.0 * 1024.0))
        }

    companion object {
        /**
         * Creates an unknown/failed metadata instance.
         */
        fun unknown(fileSize: Long) = ImageMetadata(
            width = 0,
            height = 0,
            format = "Unknown",
            fileSize = fileSize
        )
    }
}

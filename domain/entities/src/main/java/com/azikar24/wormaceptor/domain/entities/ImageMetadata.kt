package com.azikar24.wormaceptor.domain.entities

import java.util.Locale

/**
 * Metadata extracted from an image file.
 *
 * @property width Image width in pixels.
 * @property height Image height in pixels.
 * @property format Human-readable format name (e.g., "PNG", "JPEG").
 * @property colorSpace Color space if detectable (e.g., "sRGB", "CMYK").
 * @property hasAlpha Whether the image has an alpha channel.
 * @property bitDepth Bits per channel if detectable.
 * @property fileSize Size of the image data in bytes.
 */
data class ImageMetadata(
    val width: Int,
    val height: Int,
    val format: String,
    val colorSpace: String? = null,
    val hasAlpha: Boolean = false,
    val bitDepth: Int? = null,
    val fileSize: Long,
) {
    /**
     * Returns formatted dimensions string (e.g., "1920x1080").
     */
    val dimensionsString: String
        get() = "${width}x$height"

    /**
     * Returns human-readable file size string.
     */
    val fileSizeString: String
        get() = when {
            fileSize < BYTES_PER_KB -> "$fileSize B"
            fileSize < BYTES_PER_MB -> "${fileSize / BYTES_PER_KB} KB"
            else -> String.format(Locale.US, "%.2f MB", fileSize / BYTES_PER_MB.toDouble())
        }

    /** Factory methods for [ImageMetadata]. */
    companion object {
        private const val BYTES_PER_KB = 1024L
        private const val BYTES_PER_MB = 1024L * 1024L

        /**
         * Creates an unknown/failed metadata instance.
         */
        fun unknown(fileSize: Long) = ImageMetadata(
            width = 0,
            height = 0,
            format = "Unknown",
            fileSize = fileSize,
        )
    }
}

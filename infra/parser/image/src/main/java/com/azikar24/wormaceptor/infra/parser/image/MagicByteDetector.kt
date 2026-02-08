package com.azikar24.wormaceptor.infra.parser.image

/**
 * Detects image format from magic bytes (file signatures).
 *
 * Magic bytes are the first few bytes of a file that identify its format.
 * This allows detection even when Content-Type header is missing or incorrect.
 */
object MagicByteDetector {

    // PNG: 89 50 4E 47 0D 0A 1A 0A
    private val PNG_SIGNATURE = byteArrayOf(
        0x89.toByte(),
        0x50,
        0x4E,
        0x47,
        0x0D,
        0x0A,
        0x1A,
        0x0A,
    )

    // JPEG: FF D8 FF
    private val JPEG_SIGNATURE = byteArrayOf(
        0xFF.toByte(),
        0xD8.toByte(),
        0xFF.toByte(),
    )

    // GIF87a or GIF89a: 47 49 46 38 (37|39) 61
    private val GIF_SIGNATURE = byteArrayOf(0x47, 0x49, 0x46, 0x38) // "GIF8"

    // WebP: 52 49 46 46 ?? ?? ?? ?? 57 45 42 50
    // RIFF....WEBP
    private val RIFF_SIGNATURE = byteArrayOf(0x52, 0x49, 0x46, 0x46) // "RIFF"
    private val WEBP_SIGNATURE = byteArrayOf(0x57, 0x45, 0x42, 0x50) // "WEBP"

    // BMP: 42 4D
    private val BMP_SIGNATURE = byteArrayOf(0x42, 0x4D) // "BM"

    // ICO: 00 00 01 00
    private val ICO_SIGNATURE = byteArrayOf(0x00, 0x00, 0x01, 0x00)

    // SVG detection - check for XML/SVG tags
    private val XML_DECLARATION = "<?xml"
    private val SVG_TAG = "<svg"
    private val SVG_DOCTYPE = "<!DOCTYPE svg"

    /**
     * Detects the image format from the byte array.
     *
     * @param data The raw image bytes
     * @return The detected ImageFormat, or null if not recognized
     */
    fun detect(data: ByteArray): ImageFormat? {
        if (data.isEmpty()) return null

        return when {
            data.startsWith(PNG_SIGNATURE) -> ImageFormat.PNG
            data.startsWith(JPEG_SIGNATURE) -> ImageFormat.JPEG
            matchesGif(data) -> ImageFormat.GIF
            matchesWebP(data) -> ImageFormat.WEBP
            data.startsWith(BMP_SIGNATURE) -> ImageFormat.BMP
            data.size >= 4 && data.startsWith(ICO_SIGNATURE) -> ImageFormat.ICO
            matchesSvg(data) -> ImageFormat.SVG
            else -> null
        }
    }

    private fun matchesGif(data: ByteArray): Boolean {
        if (!data.startsWith(GIF_SIGNATURE)) return false
        if (data.size < 6) return false
        // Check for '7a' or '9a' after "GIF8"
        val version = data[4]
        val suffix = data[5]
        return (version == 0x37.toByte() || version == 0x39.toByte()) && suffix == 0x61.toByte()
    }

    private fun matchesWebP(data: ByteArray): Boolean {
        if (data.size < 12) return false
        if (!data.startsWith(RIFF_SIGNATURE)) return false
        // WEBP identifier is at offset 8
        return data.sliceArray(8..11).contentEquals(WEBP_SIGNATURE)
    }

    private fun matchesSvg(data: ByteArray): Boolean {
        if (data.size < 5) return false

        // Try to detect SVG by looking for characteristic strings
        val preview = try {
            String(data.take(1000).toByteArray(), Charsets.UTF_8)
        } catch (e: Exception) {
            return false
        }

        val lowercasePreview = preview.lowercase()
        return lowercasePreview.contains(SVG_TAG) ||
            lowercasePreview.contains(SVG_DOCTYPE) ||
            (lowercasePreview.contains(XML_DECLARATION) && lowercasePreview.contains("svg"))
    }

    /**
     * Checks if the data starts with a specific signature.
     */
    private fun ByteArray.startsWith(signature: ByteArray): Boolean {
        if (this.size < signature.size) return false
        for (i in signature.indices) {
            if (this[i] != signature[i]) return false
        }
        return true
    }
}

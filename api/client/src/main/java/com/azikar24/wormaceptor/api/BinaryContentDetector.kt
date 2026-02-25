package com.azikar24.wormaceptor.api

/**
 * Detects binary content by Content-Type header or file magic bytes.
 * Shared between OkHttp and Ktor interceptors to avoid code duplication.
 */
internal object BinaryContentDetector {

    @Suppress("MagicNumber")
    private object MagicBytes {
        val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
        val JPEG_SIGNATURE = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        val GIF_SIGNATURE = byteArrayOf(0x47, 0x49, 0x46, 0x38)
        val WEBP_RIFF_HEADER = byteArrayOf(0x52, 0x49, 0x46, 0x46)
        val WEBP_SIGNATURE = byteArrayOf(0x57, 0x45, 0x42, 0x50)
        const val WEBP_SIGNATURE_OFFSET = 8
        val PDF_SIGNATURE = byteArrayOf(0x25, 0x50, 0x44, 0x46)
        val ZIP_SIGNATURE = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
        val GZIP_SIGNATURE = byteArrayOf(0x1F, 0x8B.toByte())
        val BMP_SIGNATURE = byteArrayOf(0x42, 0x4D)
    }

    private val binaryContentTypes = setOf(
        "image/",
        "audio/",
        "video/",
        "application/octet-stream",
        "application/pdf",
        "application/zip",
        "application/gzip",
        "application/x-tar",
        "application/x-rar",
        "application/x-7z-compressed",
        "application/vnd.",
        "font/",
        "model/",
    )

    private const val MIN_MAGIC_BYTES_SIZE = 8
    private const val MIN_WEBP_SIZE = 12

    fun isBinaryContentType(contentType: String?): Boolean {
        if (contentType == null) return false
        val normalized = contentType.lowercase().trim()
        return binaryContentTypes.any { normalized.startsWith(it) }
    }

    fun isBinaryByMagicBytes(data: ByteArray): Boolean {
        if (data.size < MIN_MAGIC_BYTES_SIZE) return false

        return when {
            data.startsWith(MagicBytes.PNG_SIGNATURE) -> true
            data.startsWith(MagicBytes.JPEG_SIGNATURE) -> true
            data.startsWith(MagicBytes.GIF_SIGNATURE) -> true
            data.size >= MIN_WEBP_SIZE &&
                data.startsWith(MagicBytes.WEBP_RIFF_HEADER) &&
                data.startsWith(MagicBytes.WEBP_SIGNATURE, MagicBytes.WEBP_SIGNATURE_OFFSET) -> true
            data.startsWith(MagicBytes.PDF_SIGNATURE) -> true
            data.startsWith(MagicBytes.ZIP_SIGNATURE) -> true
            data.startsWith(MagicBytes.GZIP_SIGNATURE) -> true
            data.startsWith(MagicBytes.BMP_SIGNATURE) -> true
            else -> false
        }
    }

    private fun ByteArray.startsWith(
        prefix: ByteArray,
        offset: Int = 0,
    ): Boolean {
        if (this.size < offset + prefix.size) return false
        for (i in prefix.indices) {
            if (this[offset + i] != prefix[i]) return false
        }
        return true
    }
}

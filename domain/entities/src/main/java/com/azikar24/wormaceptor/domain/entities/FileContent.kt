/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents the content of a file with different presentation formats.
 */
sealed class FileContent {
    /**
     * Text file content that can be displayed as a string.
     */
    data class Text(
        val content: String,
        val encoding: String = "UTF-8",
        val lineCount: Int = content.lines().size,
    ) : FileContent()

    /**
     * Binary file content with a hex preview.
     */
    data class Binary(
        val bytes: ByteArray,
        val previewHex: String,
        val displaySize: Int = bytes.size,
    ) : FileContent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Binary

            if (!bytes.contentEquals(other.bytes)) return false
            if (previewHex != other.previewHex) return false
            if (displaySize != other.displaySize) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + previewHex.hashCode()
            result = 31 * result + displaySize
            return result
        }
    }

    /**
     * Image file content with bitmap data.
     */
    data class Image(
        val bytes: ByteArray,
        val width: Int,
        val height: Int,
        val mimeType: String,
    ) : FileContent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Image

            if (!bytes.contentEquals(other.bytes)) return false
            if (width != other.width) return false
            if (height != other.height) return false
            if (mimeType != other.mimeType) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + width
            result = 31 * result + height
            result = 31 * result + mimeType.hashCode()
            return result
        }
    }

    /**
     * File is too large to display.
     */
    data class TooLarge(
        val sizeBytes: Long,
        val maxSize: Long,
    ) : FileContent()

    /**
     * Error occurred while reading the file.
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : FileContent()
}

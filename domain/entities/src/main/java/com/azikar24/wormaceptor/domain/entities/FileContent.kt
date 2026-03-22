package com.azikar24.wormaceptor.domain.entities

/**
 * Represents the content of a file with different presentation formats.
 */
sealed class FileContent {
    /**
     * Text file content that can be displayed as a string.
     *
     * @property content The full text content of the file.
     * @property encoding Character encoding used to read the file.
     * @property lineCount Number of lines in the content.
     */
    data class Text(
        val content: String,
        val encoding: String = "UTF-8",
        val lineCount: Int = content.lines().size,
    ) : FileContent()

    /**
     * JSON file content with formatted/pretty-printed content.
     *
     * @property rawContent Original unformatted JSON string.
     * @property formattedContent Pretty-printed JSON for display.
     * @property isValid Whether the JSON could be parsed without errors.
     * @property lineCount Number of lines in the formatted content.
     */
    data class Json(
        val rawContent: String,
        val formattedContent: String,
        val isValid: Boolean,
        val lineCount: Int = formattedContent.lines().size,
    ) : FileContent()

    /**
     * XML file content with formatted content.
     *
     * @property rawContent Original unformatted XML string.
     * @property formattedContent Pretty-printed XML for display.
     * @property isValid Whether the XML could be parsed without errors.
     * @property lineCount Number of lines in the formatted content.
     */
    data class Xml(
        val rawContent: String,
        val formattedContent: String,
        val isValid: Boolean,
        val lineCount: Int = formattedContent.lines().size,
    ) : FileContent()

    /**
     * Binary file content with raw bytes for hex viewing.
     *
     * @property bytes Raw byte content of the file.
     * @property displaySize Number of bytes to show in the hex viewer.
     */
    data class Binary(
        val bytes: ByteArray,
        val displaySize: Int = bytes.size,
    ) : FileContent() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Binary

            if (!bytes.contentEquals(other.bytes)) return false
            if (displaySize != other.displaySize) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + displaySize
            return result
        }
    }

    /**
     * Image file content with bitmap data.
     *
     * @property bytes Raw image bytes for decoding.
     * @property width Image width in pixels.
     * @property height Image height in pixels.
     * @property mimeType Image MIME type (e.g., "image/png").
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
     * PDF file content with path for rendering.
     *
     * @property filePath Absolute path to the PDF file on disk.
     * @property pageCount Number of pages in the PDF document.
     * @property sizeBytes Size of the PDF file in bytes.
     */
    data class Pdf(
        val filePath: String,
        val pageCount: Int,
        val sizeBytes: Long,
    ) : FileContent()

    /**
     * File is too large to display.
     *
     * @property sizeBytes Actual file size in bytes.
     * @property maxSize Maximum displayable size threshold in bytes.
     */
    data class TooLarge(
        val sizeBytes: Long,
        val maxSize: Long,
    ) : FileContent()

    /**
     * Error occurred while reading the file.
     *
     * @property message Human-readable error description.
     * @property throwable Underlying exception, or null if not available.
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : FileContent()
}

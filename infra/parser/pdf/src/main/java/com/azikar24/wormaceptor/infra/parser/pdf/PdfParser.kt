package com.azikar24.wormaceptor.infra.parser.pdf

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.azikar24.wormaceptor.domain.contracts.BaseBodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ParsedBody
import com.azikar24.wormaceptor.domain.entities.PdfMetadata
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Parser for PDF documents.
 *
 * Detects PDF content using:
 * 1. Content-Type header containing "pdf"
 * 2. Magic bytes: %PDF- (0x25 0x50 0x44 0x46 0x2D)
 *
 * Uses Android's PdfRenderer to extract metadata.
 */
class PdfParser(
    private val context: Context,
) : BaseBodyParser() {

    override val supportedContentTypes: List<String> = listOf(
        "application/pdf",
        "application/x-pdf",
        "application/acrobat",
        "application/vnd.pdf",
        "text/pdf",
        "text/x-pdf",
    )

    override val priority: Int = PRIORITY

    override val defaultContentType: ContentType = ContentType.PDF

    override val emptyBodyFormatted: String = "[Empty PDF]"

    override fun canParse(
        contentType: String?,
        body: ByteArray,
    ): Boolean {
        if (contentType?.contains("pdf", ignoreCase = true) == true) {
            return true
        }
        return hasPdfMagicBytes(body)
    }

    @Suppress("TooGenericExceptionCaught")
    override fun parseBody(body: ByteArray): ParsedBody {
        if (!hasPdfMagicBytes(body)) {
            return ParsedBody(
                formatted = "[Invalid PDF - Missing magic bytes]",
                contentType = ContentType.PDF,
                isValid = false,
                errorMessage = "Invalid PDF: Missing %PDF- header",
            )
        }

        return try {
            val metadata = extractMetadata(body)
            val formatted = formatPdfSummary(metadata)

            ParsedBody(
                formatted = formatted,
                contentType = ContentType.PDF,
                metadata = metadata.toMetadataMap(),
                isValid = true,
            )
        } catch (e: SecurityException) {
            val version = extractPdfVersion(body)
            val metadata = PdfMetadata(
                pageCount = 0,
                fileSize = body.size.toLong(),
                version = version,
                isEncrypted = true,
            )

            ParsedBody(
                formatted = formatPdfSummary(metadata),
                contentType = ContentType.PDF,
                metadata = metadata.toMetadataMap(),
                isValid = true,
                errorMessage = "PDF is password-protected",
            )
        } catch (e: Exception) {
            val version = extractPdfVersion(body)
            ParsedBody(
                formatted = "[PDF - Unable to extract metadata]\nVersion: $version\nSize: ${formatFileSize(
                    body.size.toLong(),
                )}",
                contentType = ContentType.PDF,
                metadata = mapOf(
                    "version" to version,
                    "fileSize" to body.size.toString(),
                ),
                isValid = false,
                errorMessage = "Failed to parse PDF: ${e.message}",
            )
        }
    }

    private fun extractMetadata(body: ByteArray): PdfMetadata {
        var pdfRenderer: PdfRenderer? = null
        var fileDescriptor: ParcelFileDescriptor? = null
        var tempFile: File? = null

        try {
            tempFile = File.createTempFile("pdf_parse_", ".pdf", context.cacheDir)
            FileOutputStream(tempFile).use { fos ->
                fos.write(body)
            }

            fileDescriptor = ParcelFileDescriptor.open(
                tempFile,
                ParcelFileDescriptor.MODE_READ_ONLY,
            )

            pdfRenderer = PdfRenderer(fileDescriptor)
            val pageCount = pdfRenderer.pageCount
            val version = extractPdfVersion(body)
            val documentInfo = extractDocInfo(body)

            return PdfMetadata(
                pageCount = pageCount,
                title = documentInfo["Title"],
                author = documentInfo["Author"],
                creator = documentInfo["Creator"],
                creationDate = documentInfo["CreationDate"],
                modificationDate = documentInfo["ModDate"],
                fileSize = body.size.toLong(),
                version = version,
                isEncrypted = false,
                producer = documentInfo["Producer"],
                subject = documentInfo["Subject"],
                keywords = documentInfo["Keywords"],
            )
        } finally {
            pdfRenderer?.close()
            fileDescriptor?.close()
            tempFile?.delete()
        }
    }

    private fun formatPdfSummary(metadata: PdfMetadata): String = buildString {
        append("[PDF Document]\n")
        append("Version: ${metadata.version}\n")
        append("Pages: ${metadata.pageCount}\n")
        append("Size: ${formatFileSize(metadata.fileSize)}\n")

        if (metadata.isEncrypted) {
            append("Status: Password-protected\n")
        }

        metadata.title?.let { append("Title: $it\n") }
        metadata.author?.let { append("Author: $it\n") }
        metadata.subject?.let { append("Subject: $it\n") }
        metadata.creator?.let { append("Creator: $it\n") }
        metadata.producer?.let { append("Producer: $it\n") }
        metadata.creationDate?.let { append("Created: ${convertPdfDate(it)}\n") }
        metadata.modificationDate?.let { append("Modified: ${convertPdfDate(it)}\n") }
        metadata.keywords?.let { append("Keywords: $it\n") }
    }.trimEnd()

    /** PDF magic-byte detection and metadata extraction helpers. */
    companion object {
        private const val PRIORITY = 150

        private val PDF_MAGIC_BYTES = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D)

        private fun hasPdfMagicBytes(body: ByteArray): Boolean {
            if (body.size < PDF_MAGIC_BYTES.size) {
                return false
            }

            for (i in PDF_MAGIC_BYTES.indices) {
                if (body[i] != PDF_MAGIC_BYTES[i]) {
                    return false
                }
            }

            return true
        }

        @Suppress("MagicNumber")
        fun extractPdfVersion(body: ByteArray): String {
            if (body.size < 8) {
                return "Unknown"
            }

            return try {
                val header = body.take(20).toByteArray().decodeToString()
                val versionMatch = Regex("%PDF-(\\d+\\.\\d+)").find(header)
                versionMatch?.groupValues?.get(1) ?: "Unknown"
            } catch (_: Exception) {
                "Unknown"
            }
        }

        private fun extractDocInfo(body: ByteArray): Map<String, String> {
            val info = mutableMapOf<String, String>()

            try {
                val content = String(body, Charsets.ISO_8859_1)

                val fields = listOf(
                    "Title",
                    "Author",
                    "Subject",
                    "Keywords",
                    "Creator",
                    "Producer",
                    "CreationDate",
                    "ModDate",
                )

                for (field in fields) {
                    extractPdfStringField(content, field)?.let { value ->
                        info[field] = value
                    }
                }
            } catch (_: Exception) {
                // Ignore parsing errors - metadata is optional
            }

            return info
        }

        private fun extractPdfStringField(
            content: String,
            fieldName: String,
        ): String? {
            val patterns = listOf(
                Regex("/$fieldName\\s*\\(([^)]+)\\)"),
                Regex("/$fieldName\\s*<([0-9A-Fa-f]+)>"),
            )

            for (pattern in patterns) {
                val match = pattern.find(content)
                if (match != null) {
                    val value = match.groupValues[1]
                    return if (pattern.pattern.contains("<")) {
                        decodeHexString(value)
                    } else {
                        decodePdfLiteralString(value)
                    }
                }
            }

            return null
        }

        private fun decodeHexString(hex: String): String {
            return try {
                hex.chunked(2)
                    .map { it.toInt(16).toChar() }
                    .joinToString("")
            } catch (_: Exception) {
                hex
            }
        }

        private fun decodePdfLiteralString(literal: String): String {
            return literal
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\(", "(")
                .replace("\\)", ")")
                .replace("\\\\", "\\")
        }

        @Suppress("MagicNumber")
        private fun convertPdfDate(pdfDate: String): String {
            if (!pdfDate.startsWith("D:")) {
                return pdfDate
            }

            return try {
                val dateStr = pdfDate.removePrefix("D:")
                val year = dateStr.take(4)
                val month = dateStr.substring(4, 6)
                val day = dateStr.substring(6, 8)

                val time = if (dateStr.length >= 14) {
                    val hour = dateStr.substring(8, 10)
                    val minute = dateStr.substring(10, 12)
                    val second = dateStr.substring(12, 14)
                    " $hour:$minute:$second"
                } else {
                    ""
                }

                "$year-$month-$day$time"
            } catch (_: Exception) {
                pdfDate
            }
        }

        @Suppress("MagicNumber")
        fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
                bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024))
                else -> String.format(Locale.US, "%.1f GB", bytes / (1024.0 * 1024 * 1024))
            }
        }
    }
}

/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.infra.parser.pdf

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.azikar24.wormaceptor.domain.contracts.BodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ParsedBody
import com.azikar24.wormaceptor.domain.contracts.emptyParsedBody
import java.io.File
import java.io.FileOutputStream

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
) : BodyParser {

    override val supportedContentTypes: List<String> = listOf(
        "application/pdf",
        "application/x-pdf",
        "application/acrobat",
        "application/vnd.pdf",
        "text/pdf",
        "text/x-pdf",
    )

    /**
     * Priority: 150 (binary format, higher than text formats)
     */
    override val priority: Int = 150

    override fun canParse(contentType: String?, body: ByteArray): Boolean {
        // Check content type header
        if (contentType?.contains("pdf", ignoreCase = true) == true) {
            return true
        }

        // Check magic bytes: %PDF- (25 50 44 46 2D in hex)
        return isPdfMagicBytes(body)
    }

    override fun parse(body: ByteArray): ParsedBody {
        if (body.isEmpty()) {
            return emptyParsedBody(ContentType.PDF, "[Empty PDF]")
        }

        // Verify magic bytes
        if (!isPdfMagicBytes(body)) {
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
            // Password-protected PDF
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

    /**
     * Extracts metadata from PDF using Android's PdfRenderer.
     */
    private fun extractMetadata(body: ByteArray): PdfMetadata {
        var pdfRenderer: PdfRenderer? = null
        var fileDescriptor: ParcelFileDescriptor? = null
        var tempFile: File? = null

        try {
            // Write PDF to temp file for PdfRenderer
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
            val documentInfo = extractDocumentInfo(body)

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

    /**
     * Formats PDF metadata as a human-readable summary.
     */
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
        metadata.creationDate?.let { append("Created: ${formatPdfDate(it)}\n") }
        metadata.modificationDate?.let { append("Modified: ${formatPdfDate(it)}\n") }
        metadata.keywords?.let { append("Keywords: $it\n") }
    }.trimEnd()

    companion object {
        /**
         * PDF magic bytes: %PDF- (25 50 44 46 2D in hex)
         */
        private val PDF_MAGIC_BYTES = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D)

        /**
         * Checks if the byte array starts with PDF magic bytes.
         */
        fun isPdfMagicBytes(body: ByteArray): Boolean {
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

        /**
         * Extracts PDF version from the header.
         * Example: %PDF-1.7 -> "1.7"
         */
        fun extractPdfVersion(body: ByteArray): String {
            if (body.size < 8) {
                return "Unknown"
            }

            return try {
                // Find the version number after %PDF-
                val header = body.take(20).toByteArray().decodeToString()
                val versionMatch = Regex("%PDF-(\\d+\\.\\d+)").find(header)
                versionMatch?.groupValues?.get(1) ?: "Unknown"
            } catch (e: Exception) {
                "Unknown"
            }
        }

        /**
         * Extracts document info dictionary from PDF.
         * This is a basic parser that looks for common metadata fields.
         */
        fun extractDocumentInfo(body: ByteArray): Map<String, String> {
            val info = mutableMapOf<String, String>()

            try {
                val content = String(body, Charsets.ISO_8859_1)

                // List of metadata fields to extract
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
            } catch (e: Exception) {
                // Ignore parsing errors - metadata is optional
            }

            return info
        }

        /**
         * Extracts a string field value from PDF content.
         * Handles both literal strings (parentheses) and hex strings (angle brackets).
         */
        private fun extractPdfStringField(content: String, fieldName: String): String? {
            // Look for /FieldName followed by string value
            val patterns = listOf(
                // Literal string: /Title (Some Title)
                Regex("/$fieldName\\s*\\(([^)]+)\\)"),
                // Hex string: /Title <48656C6C6F>
                Regex("/$fieldName\\s*<([0-9A-Fa-f]+)>"),
            )

            for (pattern in patterns) {
                val match = pattern.find(content)
                if (match != null) {
                    val value = match.groupValues[1]
                    // If hex string, convert to text
                    return if (pattern.pattern.contains("<")) {
                        decodeHexString(value)
                    } else {
                        // Handle PDF escape sequences
                        decodePdfLiteralString(value)
                    }
                }
            }

            return null
        }

        /**
         * Decodes a PDF hex string to text.
         */
        private fun decodeHexString(hex: String): String {
            return try {
                hex.chunked(2)
                    .map { it.toInt(16).toChar() }
                    .joinToString("")
            } catch (e: Exception) {
                hex
            }
        }

        /**
         * Decodes PDF literal string escape sequences.
         */
        private fun decodePdfLiteralString(literal: String): String {
            return literal
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\(", "(")
                .replace("\\)", ")")
                .replace("\\\\", "\\")
        }

        /**
         * Formats PDF date string to human-readable format.
         * PDF dates are in format: D:YYYYMMDDHHmmSSOHH'mm'
         */
        fun formatPdfDate(pdfDate: String): String {
            if (!pdfDate.startsWith("D:")) {
                return pdfDate
            }

            return try {
                val dateStr = pdfDate.removePrefix("D:")
                val year = dateStr.substring(0, 4)
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
            } catch (e: Exception) {
                pdfDate
            }
        }

        /**
         * Formats file size in human-readable format.
         */
        fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
                bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
                else -> String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024))
            }
        }
    }
}

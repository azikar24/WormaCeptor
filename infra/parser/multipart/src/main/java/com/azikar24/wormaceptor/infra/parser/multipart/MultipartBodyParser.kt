package com.azikar24.wormaceptor.infra.parser.multipart

import com.azikar24.wormaceptor.domain.contracts.BaseBodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ParsedBody

/**
 * Parser for multipart form data (multipart/form-data).
 *
 * Features:
 * - Part boundary detection
 * - Individual part parsing with headers
 * - File metadata extraction (filename, content-type)
 */
class MultipartBodyParser : BaseBodyParser() {

    override val supportedContentTypes: List<String> = listOf(
        "multipart/form-data",
        "multipart/mixed",
        "multipart/alternative",
        "multipart/related",
    )

    override val priority: Int = 210

    override val defaultContentType: ContentType = ContentType.MULTIPART

    override fun canParse(
        contentType: String?,
        body: ByteArray,
    ): Boolean {
        // Check content type first - must have boundary parameter
        if (contentType != null) {
            val mimeType = contentType.split(";").firstOrNull()?.trim()?.lowercase()
            if (mimeType?.startsWith("multipart/") == true) {
                // Must have boundary parameter
                return extractBoundary(contentType) != null
            }
        }

        // Content inspection is unreliable for multipart without boundary
        return false
    }

    override fun parseBody(body: ByteArray): ParsedBody {
        return try {
            // Note: When parsing, we need the Content-Type header with boundary
            // For now, attempt to detect boundary from body
            val content = String(body, Charsets.UTF_8)
            val boundary = detectBoundaryFromBody(content)

            if (boundary != null) {
                val parts = parseParts(content, boundary)
                val formatted = formatParts(parts)

                ParsedBody(
                    formatted = formatted,
                    contentType = ContentType.MULTIPART,
                    metadata = mapOf(
                        "partCount" to parts.size.toString(),
                        "boundary" to boundary,
                    ),
                    isValid = true,
                )
            } else {
                ParsedBody(
                    formatted = content,
                    contentType = ContentType.MULTIPART,
                    isValid = false,
                    errorMessage = "Could not detect multipart boundary",
                )
            }
        } catch (e: Exception) {
            ParsedBody(
                formatted = String(body, Charsets.UTF_8),
                contentType = ContentType.MULTIPART,
                isValid = false,
                errorMessage = "Multipart parsing error: ${e.message}",
            )
        }
    }

    private fun extractBoundary(contentType: String): String? {
        val boundaryRegex = """boundary\s*=\s*["']?([^"';\s]+)["']?""".toRegex(RegexOption.IGNORE_CASE)
        return boundaryRegex.find(contentType)?.groupValues?.get(1)
    }

    private fun detectBoundaryFromBody(content: String): String? {
        // Look for lines starting with -- followed by boundary string
        val lines = content.lines()
        for (line in lines) {
            if (line.startsWith("--") && line.length > 2) {
                val potentialBoundary = line.substring(2).trimEnd('\r')
                // Verify this boundary appears again
                if (content.contains("\n--$potentialBoundary")) {
                    return potentialBoundary
                }
            }
        }
        return null
    }

    private fun parseParts(
        content: String,
        boundary: String,
    ): List<MultipartPart> {
        val parts = mutableListOf<MultipartPart>()
        val delimiter = "--$boundary"

        // Split by boundary
        val sections = content.split(delimiter)

        for (section in sections.drop(1)) { // Skip preamble
            if (section.trimStart().startsWith("--")) {
                // This is the epilogue
                break
            }

            val trimmed = section.trimStart('\r', '\n')
            if (trimmed.isEmpty()) continue

            // Find header/body separator (blank line)
            val separatorIndex = findHeaderBodySeparator(trimmed)
            if (separatorIndex == -1) continue

            val headerSection = trimmed.substring(0, separatorIndex)
            val bodyContent = trimmed.substring(separatorIndex).trimStart('\r', '\n')
                .trimEnd('\r', '\n')

            val headers = parseHeaders(headerSection)
            val disposition = parseContentDisposition(headers["Content-Disposition"])

            parts.add(
                MultipartPart(
                    name = disposition.name,
                    filename = disposition.filename,
                    contentType = headers["Content-Type"],
                    headers = headers,
                    content = bodyContent,
                    isBinary = isBinaryContent(headers["Content-Type"], bodyContent),
                ),
            )
        }

        return parts
    }

    private fun findHeaderBodySeparator(content: String): Int {
        // Look for \r\n\r\n or \n\n
        val crlfIdx = content.indexOf("\r\n\r\n")
        if (crlfIdx != -1) return crlfIdx + 4

        val lfIdx = content.indexOf("\n\n")
        if (lfIdx != -1) return lfIdx + 2

        return -1
    }

    private fun parseHeaders(headerSection: String): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        val lines = headerSection.lines()

        for (line in lines) {
            val colonIndex = line.indexOf(':')
            if (colonIndex > 0) {
                val key = line.substring(0, colonIndex).trim()
                val value = line.substring(colonIndex + 1).trim()
                headers[key] = value
            }
        }

        return headers
    }

    private fun parseContentDisposition(value: String?): ContentDisposition {
        if (value == null) return ContentDisposition()

        val nameRegex = """name\s*=\s*["']?([^"';\s]+)["']?""".toRegex(RegexOption.IGNORE_CASE)
        val filenameRegex = """filename\s*=\s*["']?([^"';\r\n]+)["']?""".toRegex(RegexOption.IGNORE_CASE)

        val name = nameRegex.find(value)?.groupValues?.get(1)
        val filename = filenameRegex.find(value)?.groupValues?.get(1)?.trim('"', '\'')

        return ContentDisposition(name = name, filename = filename)
    }

    private fun isBinaryContent(
        contentType: String?,
        content: String,
    ): Boolean {
        if (contentType != null) {
            val type = contentType.lowercase()
            if (type.startsWith("image/") ||
                type.startsWith("audio/") ||
                type.startsWith("video/") ||
                type.startsWith("application/octet-stream") ||
                type.startsWith("application/pdf")
            ) {
                return true
            }
        }

        // Check for non-printable characters
        val nonPrintable = content.count { c ->
            val code = c.code
            code < 9 || code in 14..31 || code == 127
        }
        return nonPrintable.toFloat() / content.length > 0.1f
    }

    private fun formatParts(parts: List<MultipartPart>): String {
        if (parts.isEmpty()) return "[No parts found]"

        return parts.mapIndexed { index, part ->
            buildString {
                appendLine("=== Part ${index + 1} ===")

                if (part.name != null) {
                    appendLine("Name: ${part.name}")
                }
                if (part.filename != null) {
                    appendLine("Filename: ${part.filename}")
                }
                if (part.contentType != null) {
                    appendLine("Content-Type: ${part.contentType}")
                }

                // Show other headers
                part.headers.filterKeys { key ->
                    key !in setOf("Content-Disposition", "Content-Type")
                }.forEach { (key, value) ->
                    appendLine("$key: $value")
                }

                appendLine()

                if (part.isBinary) {
                    val size = part.content.length
                    appendLine("[Binary content: $size bytes]")
                } else {
                    val preview = if (part.content.length > 500) {
                        part.content.take(500) + "\n... (${part.content.length} chars total)"
                    } else {
                        part.content
                    }
                    append(preview)
                }
            }
        }.joinToString("\n\n")
    }
}

/**
 * Represents a single part in a multipart message.
 */
data class MultipartPart(
    val name: String?,
    val filename: String?,
    val contentType: String?,
    val headers: Map<String, String>,
    val content: String,
    val isBinary: Boolean,
)

/**
 * Parsed Content-Disposition header values.
 */
private data class ContentDisposition(
    val name: String? = null,
    val filename: String? = null,
)

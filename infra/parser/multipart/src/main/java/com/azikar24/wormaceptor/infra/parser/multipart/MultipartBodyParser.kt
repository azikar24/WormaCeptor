package com.azikar24.wormaceptor.infra.parser.multipart

import com.azikar24.wormaceptor.domain.contracts.BaseBodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.MultipartParser
import com.azikar24.wormaceptor.domain.contracts.ParsedBody
import com.azikar24.wormaceptor.domain.entities.MultipartPart

/**
 * Parser for multipart form data content.
 *
 * Splits multipart data by boundary and extracts each part's headers,
 * name, filename, content type, and body. Also implements [MultipartParser]
 * for typed access from viewer composables.
 */
class MultipartBodyParser : BaseBodyParser(), MultipartParser {

    override val supportedContentTypes: List<String> = listOf(
        "multipart/form-data",
        "multipart/mixed",
        "multipart/related",
        "multipart/alternative",
    )

    override val priority: Int = PRIORITY

    override val defaultContentType: ContentType = ContentType.MULTIPART

    override fun canParse(
        contentType: String?,
        body: ByteArray,
    ): Boolean {
        if (contentType != null) {
            val mime = contentType.split(";").firstOrNull()?.trim()?.lowercase() ?: ""
            if (mime.startsWith("multipart/")) {
                return true
            }
        }
        return false
    }

    override fun parseBody(body: ByteArray): ParsedBody {
        val text = String(body, Charsets.UTF_8)
        val parts = parse(text, null)
        return ParsedBody(
            formatted = text,
            contentType = ContentType.MULTIPART,
            metadata = mapOf("partCount" to parts.size.toString()),
            isValid = parts.isNotEmpty(),
        )
    }

    override fun parse(
        data: String,
        boundary: String?,
    ): List<MultipartPart> {
        if (data.isBlank()) return emptyList()

        val resolvedBoundary = boundary ?: detectBoundary(data) ?: return emptyList()

        val parts = mutableListOf<MultipartPart>()
        val delimiter = "--$resolvedBoundary"

        val sections = data.split(delimiter)
            .drop(1)
            .filter { !it.trim().startsWith("--") && it.isNotBlank() }

        for (section in sections) {
            if (section.trim() == "--" || section.isBlank()) continue

            val part = parseMultipartPart(section.trim())
            if (part != null) {
                parts.add(part)
            }
        }

        return parts
    }

    private fun parseMultipartPart(section: String): MultipartPart? {
        val headerBodySplit = section.indexOf("\r\n\r\n").takeIf { it >= 0 }
            ?: section.indexOf("\n\n").takeIf { it >= 0 }
            ?: return null

        val headerSection = section.take(headerBodySplit)
        val body = section.substring(
            headerBodySplit + if (section.contains("\r\n\r\n")) 4 else 2,
        ).trimEnd()

        val headers = mutableMapOf<String, String>()
        headerSection.lines().forEach { line ->
            val colonIndex = line.indexOf(':')
            if (colonIndex > 0) {
                val key = line.take(colonIndex).trim()
                val value = line.substring(colonIndex + 1).trim()
                headers[key] = value
            }
        }

        val contentDisposition = headers["Content-Disposition"] ?: ""
        val name = extractDispositionParam(contentDisposition, "name")
        val fileName = extractDispositionParam(contentDisposition, "filename")
        val contentType = headers["Content-Type"]

        val displayHeaders = headers.toMutableMap().apply {
            remove("Content-Disposition")
            remove("Content-Type")
        }

        return MultipartPart(
            name = name ?: "unnamed",
            fileName = fileName,
            contentType = contentType,
            headers = displayHeaders,
            body = body,
            size = body.length,
        )
    }

    private fun extractDispositionParam(
        disposition: String,
        param: String,
    ): String? {
        val regex = Regex("""$param\s*=\s*"?([^";]+)"?""", RegexOption.IGNORE_CASE)
        return regex.find(disposition)?.groupValues?.getOrNull(1)?.trim()
    }

    private fun detectBoundary(data: String): String? {
        val firstLine = data.lineSequence().firstOrNull { it.startsWith("--") } ?: return null
        return firstLine.removePrefix("--").trim().takeIf { it.isNotEmpty() }
    }

    /**
     * Extracts the boundary parameter from a multipart Content-Type header.
     */
    fun extractMultipartBoundary(contentType: String): String? {
        val regex = Regex("""boundary\s*=\s*"?([^";]+)"?""", RegexOption.IGNORE_CASE)
        return regex.find(contentType)?.groupValues?.getOrNull(1)
    }

    companion object {
        private const val PRIORITY = 300
    }
}

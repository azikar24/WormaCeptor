/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.body

import com.azikar24.wormaceptor.domain.contracts.ContentType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Utility functions for body content type detection and parsing.
 */
object BodyParsingUtils {

    /**
     * Detects the content type of a body string based on Content-Type header and content inspection.
     */
    fun detectContentType(contentTypeHeader: String?, body: String?): ContentType {
        if (body == null) return ContentType.UNKNOWN

        // Check Content-Type header first
        if (contentTypeHeader != null) {
            val mimeType = contentTypeHeader.split(";").firstOrNull()?.trim()?.lowercase() ?: ""

            when {
                mimeType.contains("json") || mimeType.endsWith("+json") -> return ContentType.JSON
                mimeType.contains("xml") || mimeType.endsWith("+xml") -> return ContentType.XML
                mimeType.contains("html") -> return ContentType.HTML
                mimeType.contains("x-www-form-urlencoded") -> return ContentType.FORM_DATA
                mimeType.contains("multipart") -> return ContentType.MULTIPART
                mimeType.startsWith("text/") -> return ContentType.PLAIN_TEXT
                mimeType.contains("protobuf") -> return ContentType.PROTOBUF
                mimeType.contains("pdf") -> return ContentType.PDF
                mimeType.contains("image/png") -> return ContentType.IMAGE_PNG
                mimeType.contains("image/jpeg") || mimeType.contains("image/jpg") -> return ContentType.IMAGE_JPEG
                mimeType.contains("image/gif") -> return ContentType.IMAGE_GIF
                mimeType.contains("image/webp") -> return ContentType.IMAGE_WEBP
                mimeType.contains("image/svg") -> return ContentType.IMAGE_SVG
                mimeType.startsWith("image/") -> return ContentType.IMAGE_OTHER
            }
        }

        // Content inspection fallback
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return ContentType.PLAIN_TEXT

        return when {
            // JSON detection
            (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                (trimmed.startsWith("[") && trimmed.endsWith("]")) -> {
                try {
                    if (trimmed.startsWith("{")) {
                        JSONObject(trimmed)
                    } else {
                        JSONArray(trimmed)
                    }
                    ContentType.JSON
                } catch (e: Exception) {
                    ContentType.PLAIN_TEXT
                }
            }

            // XML detection
            trimmed.startsWith("<?xml") ||
                (
                    trimmed.startsWith("<") && !trimmed.startsWith("<!DOCTYPE html") &&
                        !trimmed.lowercase().startsWith("<html")
                    ) -> ContentType.XML

            // HTML detection
            trimmed.lowercase().contains("<!doctype html") ||
                trimmed.lowercase().startsWith("<html") -> ContentType.HTML

            // Form data detection (key=value&key2=value2)
            trimmed.contains("=") && trimmed.contains("&") &&
                !trimmed.contains("<") && !trimmed.contains("{") -> ContentType.FORM_DATA

            else -> ContentType.PLAIN_TEXT
        }
    }

    /**
     * Extracts the boundary parameter from a multipart Content-Type header.
     */
    fun extractMultipartBoundary(contentType: String): String? {
        val regex = Regex("""boundary\s*=\s*"?([^";]+)"?""", RegexOption.IGNORE_CASE)
        return regex.find(contentType)?.groupValues?.getOrNull(1)
    }

    /**
     * Returns a display name for a content type.
     */
    fun getContentTypeDisplayName(contentType: ContentType): String {
        return when (contentType) {
            ContentType.JSON -> "JSON"
            ContentType.XML -> "XML"
            ContentType.HTML -> "HTML"
            ContentType.PROTOBUF -> "Protobuf"
            ContentType.FORM_DATA -> "Form Data"
            ContentType.MULTIPART -> "Multipart"
            ContentType.PLAIN_TEXT -> "Plain Text"
            ContentType.BINARY -> "Binary"
            ContentType.PDF -> "PDF"
            ContentType.IMAGE_PNG,
            ContentType.IMAGE_JPEG,
            ContentType.IMAGE_GIF,
            ContentType.IMAGE_WEBP,
            ContentType.IMAGE_SVG,
            ContentType.IMAGE_BMP,
            ContentType.IMAGE_ICO,
            ContentType.IMAGE_OTHER,
            -> "Image"
            ContentType.UNKNOWN -> "Unknown"
        }
    }

    /**
     * Returns a color hint for a content type (hex string).
     */
    fun getContentTypeColorHint(contentType: ContentType): String {
        return when (contentType) {
            ContentType.JSON -> "#F59E0B" // Amber
            ContentType.XML -> "#8B5CF6" // Purple
            ContentType.HTML -> "#EC4899" // Pink
            ContentType.PROTOBUF -> "#10B981" // Emerald
            ContentType.FORM_DATA -> "#3B82F6" // Blue
            ContentType.MULTIPART -> "#6366F1" // Indigo
            ContentType.PLAIN_TEXT -> "#6B7280" // Gray
            ContentType.BINARY -> "#EF4444" // Red
            ContentType.PDF -> "#DC2626" // Red-600
            ContentType.IMAGE_PNG,
            ContentType.IMAGE_JPEG,
            ContentType.IMAGE_GIF,
            ContentType.IMAGE_WEBP,
            ContentType.IMAGE_SVG,
            ContentType.IMAGE_BMP,
            ContentType.IMAGE_ICO,
            ContentType.IMAGE_OTHER,
            -> "#14B8A6" // Teal
            ContentType.UNKNOWN -> "#9CA3AF" // Gray-400
        }
    }
}

/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.infra.parser.pdf

/**
 * Represents metadata extracted from a PDF document.
 *
 * @property pageCount The number of pages in the PDF.
 * @property title The document title from PDF metadata.
 * @property author The author from PDF metadata.
 * @property creator The creator application from PDF metadata.
 * @property creationDate The creation date string from PDF metadata.
 * @property modificationDate The modification date string from PDF metadata.
 * @property fileSize The size of the PDF in bytes.
 * @property version The PDF version string (e.g., "1.4", "1.7").
 * @property isEncrypted Whether the PDF is password-protected.
 * @property producer The PDF producer application.
 * @property subject The document subject from PDF metadata.
 * @property keywords Keywords from PDF metadata.
 */
data class PdfMetadata(
    val pageCount: Int,
    val title: String? = null,
    val author: String? = null,
    val creator: String? = null,
    val creationDate: String? = null,
    val modificationDate: String? = null,
    val fileSize: Long,
    val version: String,
    val isEncrypted: Boolean = false,
    val producer: String? = null,
    val subject: String? = null,
    val keywords: String? = null,
) {
    /**
     * Converts the metadata to a map for use with ParsedBody.
     */
    fun toMetadataMap(): Map<String, String> = buildMap {
        put("pageCount", pageCount.toString())
        put("fileSize", fileSize.toString())
        put("version", version)
        put("isEncrypted", isEncrypted.toString())
        title?.let { put("title", it) }
        author?.let { put("author", it) }
        creator?.let { put("creator", it) }
        creationDate?.let { put("creationDate", it) }
        modificationDate?.let { put("modificationDate", it) }
        producer?.let { put("producer", it) }
        subject?.let { put("subject", it) }
        keywords?.let { put("keywords", it) }
    }
}

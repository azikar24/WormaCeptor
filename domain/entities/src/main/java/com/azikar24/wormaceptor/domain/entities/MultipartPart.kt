package com.azikar24.wormaceptor.domain.entities

/**
 * A single part in multipart form data.
 *
 * @property name The form field name from the Content-Disposition header.
 * @property fileName The original filename if this part is a file upload, or null.
 * @property contentType The MIME type of this part, or null if unspecified.
 * @property headers Additional headers for this part (excluding Content-Disposition and Content-Type).
 * @property body The decoded body content of this part.
 * @property size The length of the body content in characters.
 */
data class MultipartPart(
    val name: String,
    val fileName: String? = null,
    val contentType: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val body: String,
    val size: Int = body.length,
)

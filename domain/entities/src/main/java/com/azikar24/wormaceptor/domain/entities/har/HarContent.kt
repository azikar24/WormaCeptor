package com.azikar24.wormaceptor.domain.entities.har

/** Response body content. */
data class HarContent(
    val size: Long,
    val mimeType: String,
    val text: String? = null,
    val encoding: String? = null,
)

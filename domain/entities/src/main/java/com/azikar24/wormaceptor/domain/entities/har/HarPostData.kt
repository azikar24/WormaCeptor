package com.azikar24.wormaceptor.domain.entities.har

/** Request body (POST data). */
data class HarPostData(
    val mimeType: String,
    val text: String? = null,
    val params: List<HarPostParam>? = null,
)

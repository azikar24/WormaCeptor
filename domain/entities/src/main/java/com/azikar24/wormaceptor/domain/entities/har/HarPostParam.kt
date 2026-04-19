package com.azikar24.wormaceptor.domain.entities.har

/** A single POST parameter (form-encoded bodies). */
data class HarPostParam(
    val name: String,
    val value: String? = null,
    val fileName: String? = null,
    val contentType: String? = null,
)

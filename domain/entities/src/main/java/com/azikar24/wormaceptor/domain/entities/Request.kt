package com.azikar24.wormaceptor.domain.entities

data class Request(
    val url: String,
    val method: String,
    val headers: Map<String, List<String>>,
    val bodyRef: BlobID?,
    val bodySize: Long = 0
)

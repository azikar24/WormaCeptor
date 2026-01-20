package com.azikar24.wormaceptor.domain.entities

data class Response(
    val code: Int,
    val message: String,
    val headers: Map<String, List<String>>,
    val bodyRef: BlobID?,
    val error: String? = null,
    val protocol: String? = null,
    val tlsVersion: String? = null,
    val bodySize: Long = 0,
)

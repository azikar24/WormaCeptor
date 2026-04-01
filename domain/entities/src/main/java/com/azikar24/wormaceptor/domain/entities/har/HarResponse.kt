package com.azikar24.wormaceptor.domain.entities.har

/** HAR response object. */
data class HarResponse(
    val status: Int,
    val statusText: String,
    val httpVersion: String,
    val cookies: List<HarCookie>,
    val headers: List<HarHeader>,
    val content: HarContent,
    val redirectURL: String,
    val headersSize: Long,
    val bodySize: Long,
)

package com.azikar24.wormaceptor.domain.entities.har

/** HAR request object. */
data class HarRequest(
    val method: String,
    val url: String,
    val httpVersion: String,
    val cookies: List<HarCookie>,
    val headers: List<HarHeader>,
    val queryString: List<HarQueryParam>,
    val postData: HarPostData? = null,
    val headersSize: Long,
    val bodySize: Long,
)

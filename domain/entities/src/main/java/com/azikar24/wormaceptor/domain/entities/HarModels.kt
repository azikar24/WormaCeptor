package com.azikar24.wormaceptor.domain.entities

/**
 * Data classes modelling the HAR 1.2 specification.
 *
 * @see <a href="http://www.softwareishard.com/blog/har-12-spec/">HAR 1.2 Spec</a>
 */

/** Top-level HAR document wrapper. */
data class HarLog(
    val version: String = "1.2",
    val creator: HarCreator,
    val entries: List<HarEntry>,
)

/** Tool that created the HAR file. */
data class HarCreator(
    val name: String,
    val version: String,
)

/** A single HTTP transaction entry in the HAR log. */
data class HarEntry(
    val startedDateTime: String,
    val time: Long,
    val request: HarRequest,
    val response: HarResponse,
    val timings: HarTimings,
    val serverIPAddress: String? = null,
    val connection: String? = null,
    /** Custom extension: WebSocket frames captured during this connection. */
    val webSocketFrames: List<HarWebSocketFrame>? = null,
    /** Custom extension: TLS version negotiated for the connection. */
    val tlsVersion: String? = null,
    /** Custom extension: Cipher suite used for the TLS connection. */
    val cipherSuite: String? = null,
)

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

/** A single name/value header pair. */
data class HarHeader(
    val name: String,
    val value: String,
)

/** A single parsed query-string parameter. */
data class HarQueryParam(
    val name: String,
    val value: String,
)

/** A parsed cookie. */
data class HarCookie(
    val name: String,
    val value: String,
    val path: String? = null,
    val domain: String? = null,
    val expires: String? = null,
    val httpOnly: Boolean? = null,
    val secure: Boolean? = null,
)

/** Timing breakdown of the request lifecycle. Values are in milliseconds; -1 means unavailable. */
data class HarTimings(
    val blocked: Long = -1,
    val dns: Long = -1,
    val connect: Long = -1,
    val ssl: Long = -1,
    val send: Long = -1,
    val wait: Long = -1,
    val receive: Long = -1,
)

/** Response body content. */
data class HarContent(
    val size: Long,
    val mimeType: String,
    val text: String? = null,
    val encoding: String? = null,
)

/** Request body (POST data). */
data class HarPostData(
    val mimeType: String,
    val text: String? = null,
    val params: List<HarPostParam>? = null,
)

/** A single POST parameter (form-encoded bodies). */
data class HarPostParam(
    val name: String,
    val value: String? = null,
    val fileName: String? = null,
    val contentType: String? = null,
)

/** Custom extension: a single WebSocket frame. */
data class HarWebSocketFrame(
    val type: String,
    val direction: String,
    val data: String,
    val timestamp: String,
    val size: Long,
)

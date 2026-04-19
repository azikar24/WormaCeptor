package com.azikar24.wormaceptor.domain.entities.har

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

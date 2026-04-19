package com.azikar24.wormaceptor.domain.entities.har

/** Custom extension: a single WebSocket frame. */
data class HarWebSocketFrame(
    val type: String,
    val direction: String,
    val data: String,
    val timestamp: String,
    val size: Long,
)

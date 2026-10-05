package com.azikar24.wormaceptor.mcp.server.streaming

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class StreamEvent(
    val channel: String,
    val event: String,
    val timestamp: Long,
    val data: JsonElement,
)

@Serializable
internal data class SubscribeMessage(
    val type: String,
    val channels: List<String>,
)

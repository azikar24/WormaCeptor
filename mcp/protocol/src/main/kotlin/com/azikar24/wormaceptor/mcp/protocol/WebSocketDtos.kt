package com.azikar24.wormaceptor.mcp.protocol

import kotlinx.serialization.Serializable

@Serializable
internal data class WebSocketConnectionDto(
    val id: Long,
    val url: String,
    val state: String,
    val openedAt: Long?,
    val closedAt: Long?,
    val closeCode: Int?,
    val closeReason: String?,
    val duration: Long?,
    val isActive: Boolean,
)

@Serializable
internal data class WebSocketMessageDto(
    val id: Long,
    val connectionId: Long,
    val type: String,
    val direction: String,
    val payload: String,
    val timestamp: Long,
    val size: Long,
)

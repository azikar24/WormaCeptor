package com.azikar24.wormaceptor.mcp.server.serialization.dto

import com.azikar24.wormaceptor.core.engine.PushSimulatorEngine
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
internal data class PushTokenInfoDto(
    val token: String,
    val provider: String,
    val createdAt: Long,
    val lastRefreshed: Long,
    val isValid: Boolean,
    val associatedUserId: String?,
    val metadata: Map<String, String>,
)

@Serializable
internal data class SimulatedNotificationDto(
    val title: String,
    val body: String,
    // The bridge sends title, body and optionally channelId and priority.
    val id: String = UUID.randomUUID().toString(),
    val channelId: String = PushSimulatorEngine.DEFAULT_CHANNEL_ID,
    val priority: String = "default",
    val extras: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
)

@Serializable
internal data class NotificationChannelInfoDto(
    val id: String,
    val name: String,
    val description: String?,
    val importance: Int,
)

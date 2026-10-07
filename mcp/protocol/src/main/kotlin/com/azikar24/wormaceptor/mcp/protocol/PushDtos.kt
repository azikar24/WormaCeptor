package com.azikar24.wormaceptor.mcp.protocol

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
    // PushSimulatorEngine.DEFAULT_CHANNEL_ID, pinned by BridgeRequestDecodingTest: this code can't see core:engine.
    val channelId: String = "wormaceptor_test_channel",
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

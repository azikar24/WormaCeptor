package com.azikar24.wormaceptor.mcp.server.serialization.dto

import kotlinx.serialization.Serializable

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
    val id: String,
    val title: String,
    val body: String,
    val channelId: String,
    val priority: String,
    val extras: Map<String, String>,
    val timestamp: Long,
)

@Serializable
internal data class NotificationChannelInfoDto(
    val id: String,
    val name: String,
    val description: String?,
    val importance: Int,
)

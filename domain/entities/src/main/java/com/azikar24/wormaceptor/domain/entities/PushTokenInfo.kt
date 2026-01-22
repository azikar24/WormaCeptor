/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents information about a push notification token.
 *
 * @property token The push token string
 * @property provider The push notification provider (FCM, HMS, etc.)
 * @property createdAt Timestamp when the token was first created (milliseconds)
 * @property lastRefreshed Timestamp when the token was last refreshed (milliseconds)
 * @property isValid Whether the token is currently valid
 * @property associatedUserId Optional user ID associated with this token
 * @property metadata Additional metadata key-value pairs
 */
data class PushTokenInfo(
    val token: String,
    val provider: PushProvider,
    val createdAt: Long,
    val lastRefreshed: Long,
    val isValid: Boolean,
    val associatedUserId: String?,
    val metadata: Map<String, String>,
) {
    /**
     * Supported push notification providers.
     */
    enum class PushProvider {
        FCM, // Firebase Cloud Messaging
        HUAWEI_HMS, // Huawei Mobile Services
        XIAOMI_MIPUSH, // Xiaomi Mi Push
        ONESIGNAL, // OneSignal
        UNKNOWN, // Unknown or undetected provider
    }

    companion object {
        /**
         * Creates an empty PushTokenInfo instance.
         */
        fun empty(): PushTokenInfo = PushTokenInfo(
            token = "",
            provider = PushProvider.UNKNOWN,
            createdAt = 0L,
            lastRefreshed = 0L,
            isValid = false,
            associatedUserId = null,
            metadata = emptyMap(),
        )
    }
}

/**
 * Represents a historical token event.
 *
 * @property token The token associated with this event
 * @property timestamp When this event occurred (milliseconds)
 * @property event The type of token event
 */
data class TokenHistory(
    val token: String,
    val timestamp: Long,
    val event: TokenEvent,
) {
    /**
     * Types of token lifecycle events.
     */
    enum class TokenEvent {
        CREATED, // Token was first created
        REFRESHED, // Token was refreshed/updated
        INVALIDATED, // Token was marked as invalid
        DELETED, // Token was deleted
    }
}

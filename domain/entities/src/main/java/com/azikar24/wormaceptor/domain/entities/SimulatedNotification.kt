/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a simulated push notification for testing purposes.
 */
data class SimulatedNotification(
    val id: String,
    val title: String,
    val body: String,
    val channelId: String,
    val smallIconRes: Int = 0,
    val largeIconUri: String? = null,
    val priority: NotificationPriority = NotificationPriority.DEFAULT,
    val actions: List<NotificationAction> = emptyList(),
    val extras: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis(),
) {
    companion object {
        fun empty() = SimulatedNotification(
            id = "",
            title = "",
            body = "",
            channelId = "",
        )
    }
}

/**
 * Represents a notification action button.
 */
data class NotificationAction(
    val title: String,
    val actionId: String,
)

/**
 * Priority levels for notifications matching Android NotificationCompat priorities.
 */
enum class NotificationPriority(val value: Int) {
    LOW(-1),
    DEFAULT(0),
    HIGH(1),
    MAX(2),
}

/**
 * Represents a saved notification template for reuse.
 */
data class NotificationTemplate(
    val id: String,
    val name: String,
    val notification: SimulatedNotification,
)

/**
 * Information about an app's notification channel.
 */
data class NotificationChannelInfo(
    val id: String,
    val name: String,
    val description: String?,
    val importance: Int,
)

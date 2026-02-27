package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a simulated push notification for testing purposes.
 */
data class SimulatedNotification(
    /** Unique identifier used for notification posting and cancellation. */
    val id: String,
    /** Notification title text. */
    val title: String,
    /** Notification body/content text. */
    val body: String,
    /** Notification channel ID (required for Android O+). */
    val channelId: String,
    /** Drawable resource ID for the small status-bar icon (0 uses default). */
    val smallIconRes: Int = 0,
    /** URI pointing to the large icon image, null for none. */
    val largeIconUri: String? = null,
    /** Notification display priority level. */
    val priority: NotificationPriority = NotificationPriority.DEFAULT,
    /** Interactive action buttons attached to the notification. */
    val actions: List<NotificationAction> = emptyList(),
    /** Arbitrary key-value payload delivered with the notification. */
    val extras: Map<String, String> = emptyMap(),
    /** Epoch millis when the notification was created. */
    val timestamp: Long = System.currentTimeMillis(),
) {
    /** Factory methods for [SimulatedNotification]. */
    companion object {
        /** Creates an empty notification with blank fields. */
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
    /** Label displayed on the action button. */
    val title: String,
    /** Identifier sent back to the app when the action is tapped. */
    val actionId: String,
)

/**
 * Priority levels for notifications matching Android NotificationCompat priorities.
 */
enum class NotificationPriority(
    /** Integer constant matching NotificationCompat.PRIORITY_* values. */
    val value: Int,
) {
    /** Shown only in the shade, no sound or vibration. */
    LOW(-1),

    /** Standard notification behavior. */
    DEFAULT(0),

    /** Shown as a heads-up notification with sound. */
    HIGH(1),

    /** Highest priority, shown persistently until dismissed. */
    MAX(2),
}

/**
 * Represents a saved notification template for reuse.
 */
data class NotificationTemplate(
    /** Unique identifier for the saved template. */
    val id: String,
    /** User-defined display name for the template. */
    val name: String,
    /** The notification configuration stored in this template. */
    val notification: SimulatedNotification,
)

/**
 * Information about an app's notification channel.
 */
data class NotificationChannelInfo(
    /** Channel identifier registered with the system. */
    val id: String,
    /** User-visible channel name shown in system settings. */
    val name: String,
    /** Optional channel description shown in system settings. */
    val description: String?,
    /** Importance level (maps to NotificationManager.IMPORTANCE_* constants). */
    val importance: Int,
)

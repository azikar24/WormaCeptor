package com.azikar24.wormaceptor.core.engine

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.azikar24.wormaceptor.domain.entities.NotificationChannelInfo
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.domain.entities.SimulatedNotification

/**
 * Engine for creating and posting simulated notifications.
 * Supports creating notifications with actions, large icons, and FCM-like data payloads.
 */
class PushSimulatorEngine(private val context: Context) {

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private val notificationManagerCompat: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(context)
    }

    /**
     * Sends a notification based on the provided SimulatedNotification.
     * Creates default channel if the specified channel does not exist.
     *
     * @param notification The notification configuration to send
     * @return The notification ID used (for later cancellation)
     * @throws NotificationPermissionException if notification permission is not granted
     */
    @SuppressLint("MissingPermission") // Permission checked via hasNotificationPermission() before notify()
    fun sendNotification(notification: SimulatedNotification): Int {
        ensureChannelExists(notification.channelId)

        val notificationId = notification.id.hashCode()

        val builder = NotificationCompat.Builder(context, notification.channelId)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setSmallIcon(getSmallIcon(notification.smallIconRes))
            .setPriority(mapPriority(notification.priority))
            .setAutoCancel(true)
            .setWhen(notification.timestamp)

        // Set large icon if provided
        notification.largeIconUri?.let { uriString ->
            try {
                val uri = uriString.toUri()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    bitmap?.let { builder.setLargeIcon(it) }
                }
            } catch (_: Exception) {
                // Ignore invalid URI
            }
        }

        // Add action buttons (up to 3)
        notification.actions.take(3).forEachIndexed { index, action ->
            val actionIntent = Intent(ACTION_BROADCAST_PREFIX + action.actionId).apply {
                putExtra("action_id", action.actionId)
                putExtra("notification_id", notificationId)
                setPackage(context.packageName)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId + index,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            builder.addAction(0, action.title, pendingIntent)
        }

        // Add extras as a bundle
        if (notification.extras.isNotEmpty()) {
            notification.extras.forEach { (key, value) ->
                builder.extras.putString(key, value)
            }
        }

        // Style for longer text
        if (notification.body.length > 40) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
        }

        // Check permission before posting (required for Android 13+)
        if (!hasNotificationPermission()) {
            throw NotificationPermissionException("Notification permission not granted")
        }

        notificationManagerCompat.notify(notificationId, builder.build())

        return notificationId
    }

    /**
     * Cancels a previously sent notification.
     *
     * @param notificationId The ID of the notification to cancel
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    /**
     * Cancels a notification by its string ID (uses hashCode).
     *
     * @param id The string ID of the notification
     */
    fun cancelNotification(id: String) {
        cancelNotification(id.hashCode())
    }

    /**
     * Cancels all notifications from this app.
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    /**
     * Lists all notification channels registered by the app.
     *
     * @return List of notification channel information
     */
    fun getNotificationChannels(): List<NotificationChannelInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.notificationChannels.map { channel ->
                NotificationChannelInfo(
                    id = channel.id,
                    name = channel.name.toString(),
                    description = channel.description,
                    importance = channel.importance,
                )
            }
        } else {
            // Pre-O devices don't have channels, return default
            listOf(
                NotificationChannelInfo(
                    id = DEFAULT_CHANNEL_ID,
                    name = DEFAULT_CHANNEL_NAME,
                    description = DEFAULT_CHANNEL_DESCRIPTION,
                    importance = NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }
    }

    /**
     * Creates the default notification channel for testing.
     * Call this to ensure a channel exists for testing.
     */
    fun createDefaultChannel() {
        createChannel(
            channelId = DEFAULT_CHANNEL_ID,
            channelName = DEFAULT_CHANNEL_NAME,
            description = DEFAULT_CHANNEL_DESCRIPTION,
            importance = NotificationManager.IMPORTANCE_DEFAULT,
        )
    }

    /**
     * Creates a notification channel with the specified parameters.
     *
     * @param channelId The channel ID
     * @param channelName The user-visible channel name
     * @param description The channel description
     * @param importance The channel importance level
     */
    fun createChannel(
        channelId: String,
        channelName: String,
        description: String?,
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                this.description = description
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Simulates receiving an FCM data payload.
     * This creates a notification with the data and stores extras in the notification bundle.
     *
     * @param data The FCM-like data payload
     * @param notification Optional notification configuration; if null, uses data for title/body
     * @return The notification ID
     */
    fun simulateFcmPayload(
        data: Map<String, String>,
        notification: SimulatedNotification? = null,
    ): Int {
        val finalNotification = notification ?: SimulatedNotification(
            id = System.currentTimeMillis().toString(),
            title = data["title"] ?: "FCM Message",
            body = data["body"] ?: data["message"] ?: "",
            channelId = data["channel_id"] ?: DEFAULT_CHANNEL_ID,
            extras = data,
        )

        return sendNotification(finalNotification.copy(extras = data + finalNotification.extras))
    }

    /**
     * Checks if notification permission is granted (Android 13+).
     *
     * @return True if notifications can be posted, false otherwise
     */
    fun hasNotificationPermission(): Boolean {
        return notificationManagerCompat.areNotificationsEnabled()
    }

    private fun ensureChannelExists(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(channelId)
            if (channel == null) {
                // Create the channel if it doesn't exist
                if (channelId == DEFAULT_CHANNEL_ID) {
                    createDefaultChannel()
                } else {
                    // Create a generic channel for unknown IDs
                    createChannel(
                        channelId = channelId,
                        channelName = "Test Channel: $channelId",
                        description = "Auto-created channel for testing",
                    )
                }
            }
        }
    }

    private fun getSmallIcon(iconRes: Int): Int {
        // If no icon provided, use a monochrome system icon.
        // Using the app's launcher icon causes a white blob in the status bar
        // because Android requires small icons to be monochrome (white + transparent).
        return if (iconRes != 0) iconRes else android.R.drawable.ic_popup_reminder
    }

    private fun mapPriority(priority: NotificationPriority): Int {
        return when (priority) {
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            NotificationPriority.DEFAULT -> NotificationCompat.PRIORITY_DEFAULT
            NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationPriority.MAX -> NotificationCompat.PRIORITY_MAX
        }
    }

    /** Default notification channel configuration. */
    companion object {
        /** Channel ID used for the default test notification channel. */
        const val DEFAULT_CHANNEL_ID = "wormaceptor_test_channel"

        /** User-visible name for the default test notification channel. */
        const val DEFAULT_CHANNEL_NAME = "WormaCeptor Test Notifications"

        /** Description for the default test notification channel. */
        const val DEFAULT_CHANNEL_DESCRIPTION = "Channel for testing push notifications"
        private const val ACTION_BROADCAST_PREFIX = "com.azikar24.wormaceptor.NOTIFICATION_ACTION_"
    }
}

/**
 * Exception thrown when notification permission is not granted.
 */
class NotificationPermissionException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

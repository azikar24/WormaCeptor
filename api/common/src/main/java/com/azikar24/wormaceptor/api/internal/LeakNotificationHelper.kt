package com.azikar24.wormaceptor.api.internal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.azikar24.wormaceptor.domain.entities.LeakInfo

class LeakNotificationHelper(
    private val context: Context,
) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WormaCeptor Memory Leaks",
                NotificationManager.IMPORTANCE_HIGH,
            )
            channel.description = "Notifications for detected memory leaks"
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun show(leak: LeakInfo) {
        // Deep link directly to the leak detection screen
        val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse(LEAK_DETECTION_DEEP_LINK)).apply {
            setPackage(context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val className = leak.objectClass.substringAfterLast('.')
        val retainedSizeFormatted = formatBytes(leak.retainedSize)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Memory Leak Detected [${leak.severity}]")
            .setContentText("$className - $retainedSizeFormatted retained")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${leak.leakDescription}\n\nClass: ${leak.objectClass}\nRetained: $retainedSizeFormatted"),
            )

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
            bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    companion object {
        private const val CHANNEL_ID = "wormaceptor_leak_channel"
        private const val NOTIFICATION_ID = 4201
        private const val LEAK_DETECTION_DEEP_LINK = "wormaceptor://tools/leakdetection"
    }
}

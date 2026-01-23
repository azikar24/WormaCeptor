package com.azikar24.wormaceptor.api.internal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.azikar24.wormaceptor.domain.entities.ThreadViolation

class ThreadViolationNotificationHelper(
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
                "WormaCeptor Thread Violations",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            channel.description = "Notifications for detected main thread violations"
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun show(violation: ThreadViolation) {
        val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse(THREAD_VIOLATION_DEEP_LINK)).apply {
            setPackage(context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val typeLabel = violation.violationType.name.replace("_", " ")
        val durationText = violation.durationMs?.let { " - ${it}ms" } ?: ""

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Thread Violation: $typeLabel")
            .setContentText("${violation.description}$durationText")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${violation.description}\n\nThread: ${violation.threadName}$durationText"),
            )

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        private const val CHANNEL_ID = "wormaceptor_thread_violation_channel"
        private const val NOTIFICATION_ID = 4202
        private const val THREAD_VIOLATION_DEEP_LINK = "wormaceptor://tools/threadviolation"
    }
}

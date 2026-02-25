package com.azikar24.wormaceptorapp.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.azikar24.wormaceptorapp.R
import com.azikar24.wormaceptorapp.main.view.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class WormaCeptorMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received from: ${message.from}")

        message.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "WormaCeptor",
                body = notification.body ?: "",
            )
        }

        if (message.data.isNotEmpty()) {
            Log.d(TAG, "FCM data payload: ${message.data}")
            handleDataMessage(message.data)
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val title = data["title"] ?: "WormaCeptor"
        val body = data["body"] ?: data["message"] ?: ""
        if (body.isNotEmpty()) {
            showNotification(title, body)
        }
    }

    private fun showNotification(
        title: String,
        body: String,
    ) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Firebase Messages",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notifications from Firebase Cloud Messaging"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val TAG = "WormaCeptorFCM"
        private const val CHANNEL_ID = "wormaceptor_fcm_channel"
    }
}

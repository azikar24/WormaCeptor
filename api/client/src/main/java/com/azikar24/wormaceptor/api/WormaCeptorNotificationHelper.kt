package com.azikar24.wormaceptor.api

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import java.util.UUID

internal class WormaCeptorNotificationHelper(private val context: Context) {
    
    // Hardcoded resources for now or copy them? 
    // Ideally we should use resources from core/ui, but api module might not have them.
    // We'll use simple strings for prototype.
    companion object {
        private const val CHANNEL_ID = "wormaceptor_v2_channel"
        private const val NOTIFICATION_ID = 4200
        private const val BUFFER_SIZE = 10
    }

    private val notificationManager: NotificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    // Simple buffer
    private val transactionBuffer = ArrayDeque<NetworkTransaction>(BUFFER_SIZE)
    private var transactionCount = 0

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, 
                "WormaCeptor Transactions", 
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun show(transaction: NetworkTransaction) {
        synchronized(this) {
            if (transactionBuffer.size >= BUFFER_SIZE) {
                transactionBuffer.removeFirst()
            }
            transactionBuffer.addLast(transaction)
            transactionCount++
        }

        val launchIntent = WormaCeptorApi.getLaunchIntent(context)
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            launchIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_rotate) // Stock icon for now
            .setContentTitle("WormaCeptor: Recording...")
            .setContentText("${transaction.request.method} ${transaction.request.url}")
            .setContentIntent(pendingIntent)
            .setAutoCancel(false) // Ongoing
            .setOngoing(true) 
            .setOnlyAlertOnce(true)
            .setSubText("$transactionCount transactions")
            
        // Add inbox style
        val inbox = NotificationCompat.InboxStyle()
        synchronized(this) {
            transactionBuffer.reversed().forEach { 
                val code = it.response?.code ?: "..."
                inbox.addLine("[$code] ${it.request.method} ${it.path}")
            }
        }
        builder.setStyle(inbox)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun dismiss() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
    
    private val NetworkTransaction.path get() = try { java.net.URI(this.request.url).path } catch(e:Exception) { this.request.url }
}

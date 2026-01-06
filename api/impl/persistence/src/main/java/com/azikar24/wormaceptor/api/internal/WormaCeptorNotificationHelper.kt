package com.azikar24.wormaceptor.api.internal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import java.util.UUID

internal class WormaCeptorNotificationHelper(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = "wormaceptor_v2_channel"
        private const val NOTIFICATION_ID = 4200
        private const val BUFFER_SIZE = 10
    }

    private val notificationManager: NotificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
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
            .setSmallIcon(android.R.drawable.ic_menu_rotate)
            .setContentTitle("WormaCeptor: Recording...")
            .setContentText("${transaction.request.method} ${transaction.request.url}")
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true) 
            .setOnlyAlertOnce(true)
            .setSubText("$transactionCount transactions")
            
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

    private val NetworkTransaction.path get() = try { java.net.URI(this.request.url).path } catch(e:Exception) { this.request.url }
}

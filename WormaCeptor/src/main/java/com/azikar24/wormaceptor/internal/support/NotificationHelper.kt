/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.LongSparseArray
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.ui.WormaCeptorMainActivity

class NotificationHelper(val context: Context) {
    private val CHANNEL_ID = "wormaceptor_notification"
    private val mNotificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val NOTIFICATION_ID = 1139
    private val mColorUtil: ColorUtil = ColorUtil.getInstance(context)
    private val BUFFER_SIZE = 10

    init {
        setUpChannelIfNecessary()
    }

    private fun setUpChannelIfNecessary() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_category), NotificationManager.IMPORTANCE_LOW)
            channel.setShowBadge(false)
            mNotificationManager.createNotificationChannel(channel)
        }
    }

    fun show(newTransaction: NetworkTransaction, stickyNotification: Boolean) {
        val networkTransactionUIHelper = NetworkTransactionUIHelper(newTransaction)
        addToBuffer(networkTransactionUIHelper)

        if (WormaCeptorMainActivity.IN_FOREGROUND) return

        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    setContentIntent(PendingIntent.getActivity(context, 0, WormaCeptor.getLaunchIntent(context), PendingIntent.FLAG_IMMUTABLE))
                else {
                    setContentIntent(PendingIntent.getActivity(context, 0, WormaCeptor.getLaunchIntent(context), PendingIntent.FLAG_UPDATE_CURRENT))
                }
            }
            .setLocalOnly(true)
            .setSmallIcon(R.drawable.ic_notification_white_24dp)
            .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setOngoing(stickyNotification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .addAction(getDismissAction())
            .addAction(getClearAction())
            .apply {
                val inboxStyle = NotificationCompat.InboxStyle()
                for ((count, i) in (TRANSACTION_BUFFER.size() - 1 downTo 0).withIndex()) {
                    if (count < BUFFER_SIZE) {
                        try {
                            if (count == 0) {
                                setContentText(getNotificationText(TRANSACTION_BUFFER.valueAt(i)))
                            }
                            inboxStyle.addLine(getNotificationText(TRANSACTION_BUFFER.valueAt(i)))
                        } catch (e: Exception) {
                            Logger.e("Unexpected", e)
                        }
                    }
                }
                setStyle(inboxStyle)
            }.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setSubText(TRANSACTION_COUNT.toString())
                } else {
                    setNumber(TRANSACTION_COUNT)
                }
            }
        mNotificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun dismiss() {
        mNotificationManager.cancel(NOTIFICATION_ID)
    }

    @Synchronized
    private fun addToBuffer(transaction: NetworkTransactionUIHelper) {
        if (transaction.getStatus() == NetworkTransactionUIHelper.Status.Requested) {
            TRANSACTION_COUNT++
        }
        TRANSACTION_BUFFER.put(transaction.networkTransaction.id, transaction)
        if (TRANSACTION_BUFFER.size() > BUFFER_SIZE) {
            TRANSACTION_BUFFER.removeAt(0)
        }
    }

    private fun getNotificationText(transaction: NetworkTransactionUIHelper): CharSequence {
        val color = mColorUtil.getTransactionColor(transaction)
        val text = transaction.getNotificationText()
        val spannableString = SpannableString(text)
        spannableString.setSpan(ForegroundColorSpan(color), 0, text.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    private fun getDismissAction(): NotificationCompat.Action {
        val dismissTitle: CharSequence = context.getString(R.string.dismiss)
        val dismissIntent = Intent(context, DismissNotificationService::class.java)
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.getService(context, 12, dismissIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        else {
            PendingIntent.getService(context, 12, dismissIntent, PendingIntent.FLAG_ONE_SHOT)
        }

        return NotificationCompat.Action(0, dismissTitle, intent)
    }

    private fun getClearAction(): NotificationCompat.Action {
        val clearTitle: CharSequence = context.getString(R.string.clear)
        val deleteIntent = Intent(context, ClearTransactionsService::class.java)
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            PendingIntent.getService(context, 12, deleteIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        else {
            PendingIntent.getService(context, 12, deleteIntent, PendingIntent.FLAG_ONE_SHOT)
        }
        return NotificationCompat.Action(R.drawable.ic_delete_white_24dp, clearTitle, intent)
    }

    companion object {
        private var TRANSACTION_COUNT = 0
        private val TRANSACTION_BUFFER = LongSparseArray<NetworkTransactionUIHelper>()

        @Synchronized
        fun clearBuffer() {
            TRANSACTION_BUFFER.clear()
            TRANSACTION_COUNT = 0
        }
    }
}
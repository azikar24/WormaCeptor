/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.app.IntentService
import android.content.Intent
import com.azikar24.wormaceptor.WormaCeptor

class ClearTransactionsService : IntentService("WormaCeptor-ClearTransactionService") {
    override fun onHandleIntent(intent: Intent?) {
        val deletedTransactionCount = WormaCeptor.storage?.transactionDao?.clearAll() ?: 0
        Logger.i("$deletedTransactionCount transaction deleted")

        val notificationHelper = NotificationHelper(this)
        notificationHelper.dismiss()
    }

}
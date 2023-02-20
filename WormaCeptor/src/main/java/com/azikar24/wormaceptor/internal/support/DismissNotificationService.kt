/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.support

import android.app.IntentService
import android.content.Intent

class DismissNotificationService : IntentService("WormaCeptor-DismissNotificationService") {
    override fun onHandleIntent(intent: Intent?) {
        val notificationHelper = NotificationHelper(this)
        notificationHelper.dismiss()
    }
}
/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor

import android.app.Application
import com.azikar24.wormaceptor.persistence.WormaCeptorPersistence

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        WormaCeptor.storage = WormaCeptorPersistence.getInstance(this)
//        WormaCeptor.storage = WormaCeptorIMDB.getInstance()
        WormaCeptor.addAppShortcut(this)
        WormaCeptor.logUnexpectedCrashes()
        WormaCeptor.reportCrashesToEmails = listOf("azikar24@gmail.com", "kroosh4@hotmail.com")
        WormaCeptor.reportCrashesExtras = "version name: ${BuildConfig.VERSION_NAME}"
    }

}
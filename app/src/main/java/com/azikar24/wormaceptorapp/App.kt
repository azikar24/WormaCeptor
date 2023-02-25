/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptorapp

import android.app.Application
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.persistence.WormaCeptorPersistence

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        WormaCeptor.storage = WormaCeptorPersistence.getInstance(this)
//        WormaCeptor.storage = WormaCeptorIMDB.getInstance()
        WormaCeptor.addAppShortcut(this)
        WormaCeptor.logUnexpectedCrashes()
    }

}
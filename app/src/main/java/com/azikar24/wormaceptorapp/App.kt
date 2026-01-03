package com.azikar24.wormaceptorapp

import android.app.Application
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.persistence.WormaCeptorPersistence
//import com.azikar24.wormaceptor.imdb.WormaCeptorIMDB

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        WormaCeptor.init(
            context = this,
            storage = WormaCeptorPersistence.getInstance(this),
//            storage = WormaCeptorIMDB.getInstance(),
            appShortcut = true,
            logCrashes = true,
        )
    }

}
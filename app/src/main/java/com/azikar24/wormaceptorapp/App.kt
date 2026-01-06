package com.azikar24.wormaceptorapp

import android.app.Application
import com.azikar24.wormaceptor.api.WormaCeptorApi

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        WormaCeptorApi.init(
            context = this,
            storage = null, // V2 uses internal ServiceLocator
            appShortcut = true,
            logCrashes = true,
        )
    }

}
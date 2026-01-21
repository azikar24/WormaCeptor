package com.azikar24.wormaceptorapp

import android.app.Application
import com.azikar24.wormaceptor.api.Feature
import com.azikar24.wormaceptor.api.WormaCeptorApi

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        WormaCeptorApi.init(
            context = this,
            logCrashes = true,
            features = setOf(Feature.CONSOLE_LOGS)
        )
    }
}

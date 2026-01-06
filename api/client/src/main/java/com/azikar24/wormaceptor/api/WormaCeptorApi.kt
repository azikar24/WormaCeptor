package com.azikar24.wormaceptor.api

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
object WormaCeptorApi {
    
    fun init(context: Context, storage: Any? = null, appShortcut: Boolean = true, logCrashes: Boolean = true) {
        // Legacy storage/init arguments are ignored in V2
        // Phase 3: Initialize V2 Core
        ServiceLocator.init(context, logCrashes)
    }

    fun startActivityOnShake(activity: ComponentActivity) {
        // Use Platform ShakeDetector (migrated from Legacy)
        com.azikar24.wormaceptor.platform.android.ShakeDetector.start(activity) {
             activity.startActivity(getLaunchIntent(activity))
        }
    }

    fun getLaunchIntent(context: Context): Intent {
        return Intent(context, com.azikar24.wormaceptor.feature.viewer.ViewerActivity::class.java)
    }
}

/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor

import android.app.Application
import android.os.StrictMode
import com.azikar24.wormaceptor.persistence.WormaCeptorPersistence

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
        WormaCeptor.storage = WormaCeptorPersistence.getInstance(this)
//        WormaCeptor.storage = WormaCeptorIMDB.getInstance()
    }

}
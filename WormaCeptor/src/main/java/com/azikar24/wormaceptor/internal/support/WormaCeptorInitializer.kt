package com.azikar24.wormaceptor.internal.support

import android.content.Context
import androidx.startup.Initializer
import com.azikar24.wormaceptor.internal.di.wormaCeptorModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.getOrNull
import org.koin.core.context.startKoin

class WormaCeptorInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        if (getOrNull() == null) {
            startKoin {
                androidContext(context)
                modules(wormaCeptorModule)
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}

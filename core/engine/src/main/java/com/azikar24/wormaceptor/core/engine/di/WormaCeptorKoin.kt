/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

/**
 * Helper object for managing Koin initialization in WormaCeptor library.
 *
 * Handles the case where the host app may or may not have Koin already initialized.
 * If Koin is already running, it loads the engine module into the existing instance.
 * If not, it starts Koin with the engine module.
 */
object WormaCeptorKoin {
    private var initialized = false
    private var ownKoinInstance = false

    /**
     * Initializes Koin with WormaCeptor's engine module.
     *
     * Safe to call multiple times - will only initialize once.
     * If host app already has Koin running, modules are loaded into existing instance.
     *
     * @param context Application context
     */
    @Synchronized
    fun init(context: Context) {
        if (initialized) return

        val koin = GlobalContext.getOrNull()
        if (koin != null) {
            // Koin already running (host app initialized it)
            // Load our modules into existing instance
            koin.loadModules(listOf(engineModule))
        } else {
            // Koin not running, start it ourselves
            startKoin {
                androidContext(context.applicationContext)
                modules(engineModule)
            }
            ownKoinInstance = true
        }
        initialized = true
    }

    /**
     * Gets the Koin instance, initializing if needed.
     */
    fun getKoin() = GlobalContext.get()

    /**
     * Cleans up Koin if we own it.
     * Call this only when completely done with WormaCeptor.
     */
    @Synchronized
    fun cleanup() {
        if (!initialized) return

        if (ownKoinInstance) {
            GlobalContext.stopKoin()
        } else {
            // Just unload our modules from host app's Koin
            GlobalContext.getOrNull()?.unloadModules(listOf(engineModule))
        }
        initialized = false
        ownKoinInstance = false
    }
}

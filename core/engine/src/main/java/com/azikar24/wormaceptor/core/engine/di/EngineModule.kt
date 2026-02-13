package com.azikar24.wormaceptor.core.engine.di

import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import com.azikar24.wormaceptor.core.engine.CryptoEngine
import com.azikar24.wormaceptor.core.engine.DependenciesInspectorEngine
import com.azikar24.wormaceptor.core.engine.FpsMonitorEngine
import com.azikar24.wormaceptor.core.engine.LeakDetectionEngine
import com.azikar24.wormaceptor.core.engine.LoadedLibrariesEngine
import com.azikar24.wormaceptor.core.engine.LocationSimulatorEngine
import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.engine.PushSimulatorEngine
import com.azikar24.wormaceptor.core.engine.PushTokenEngine
import com.azikar24.wormaceptor.core.engine.RateLimitEngine
import com.azikar24.wormaceptor.core.engine.SecureStorageEngine
import com.azikar24.wormaceptor.core.engine.ThreadViolationEngine
import com.azikar24.wormaceptor.core.engine.WebSocketMonitorEngine
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module providing all monitoring engine instances as singletons.
 *
 * Engines are scoped as singletons to persist across Activity lifecycle,
 * ensuring monitoring state survives navigation between host app and WormaCeptor.
 */
val engineModule = module {
    // Performance monitoring engines
    single { MemoryMonitorEngine() }
    single { FpsMonitorEngine() }
    single { CpuMonitorEngine() }
    single { PerformanceOverlayEngine(androidContext()) }

    // Logging and monitoring
    single { LogCaptureEngine() }
    single { WebSocketMonitorEngine() }

    // Detection engines
    single {
        LeakDetectionEngine().apply {
            start(androidContext().applicationContext as android.app.Application)
        }
    }
    single {
        ThreadViolationEngine().apply {
            enable()
        }
    }

    // Network engines
    single { RateLimitEngine() }
    single { WebViewMonitorEngine() }

    // Tool engines (previously created by Feature objects)
    single { CryptoEngine() }
    single { SecureStorageEngine(androidContext()) }
    single { LoadedLibrariesEngine(androidContext()) }
    single { DependenciesInspectorEngine(androidContext()) }
    single { PushTokenEngine(androidContext()) }
    single { LocationSimulatorEngine(androidContext()) }
    single { PushSimulatorEngine(androidContext()) }
}

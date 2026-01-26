/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine.di

import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import com.azikar24.wormaceptor.core.engine.FpsMonitorEngine
import com.azikar24.wormaceptor.core.engine.LeakDetectionEngine
import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.core.engine.MeasurementEngine
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.engine.ThreadViolationEngine
import com.azikar24.wormaceptor.core.engine.ToolOverlayEngine
import com.azikar24.wormaceptor.core.engine.TouchVisualizationEngine
import com.azikar24.wormaceptor.core.engine.ViewBordersEngine
import com.azikar24.wormaceptor.core.engine.WebSocketMonitorEngine
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

    // Visual debug engines
    single { TouchVisualizationEngine(androidContext()) }
    single { ViewBordersEngine() }
    single { MeasurementEngine() }
    single { ToolOverlayEngine(androidContext()) }

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
}

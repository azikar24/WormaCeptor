/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.content.Context

/**
 * Singleton holder for monitoring engines that need to persist across Activity lifecycle.
 *
 * Engines are lazily created on first access and survive Activity destruction/recreation.
 * This ensures that monitoring state (running/stopped, collected data) persists when
 * navigating between the host app and WormaCeptor viewer.
 */
object MonitoringEngineHolder {

    // Performance monitoring engines
    private var _memoryMonitorEngine: MemoryMonitorEngine? = null
    private var _fpsMonitorEngine: FpsMonitorEngine? = null
    private var _cpuMonitorEngine: CpuMonitorEngine? = null

    // Visual debug engines (require Context)
    private var _touchVisualizationEngine: TouchVisualizationEngine? = null
    private var _viewBordersEngine: ViewBordersEngine? = null

    // Other engines
    private var _logCaptureEngine: LogCaptureEngine? = null
    private var _webSocketMonitorEngine: WebSocketMonitorEngine? = null
    private var _leakDetectionEngine: LeakDetectionEngine? = null
    private var _threadViolationEngine: ThreadViolationEngine? = null

    val memoryMonitorEngine: MemoryMonitorEngine
        get() {
            if (_memoryMonitorEngine == null) {
                _memoryMonitorEngine = MemoryMonitorEngine()
            }
            return _memoryMonitorEngine!!
        }

    val fpsMonitorEngine: FpsMonitorEngine
        get() {
            if (_fpsMonitorEngine == null) {
                _fpsMonitorEngine = FpsMonitorEngine()
            }
            return _fpsMonitorEngine!!
        }

    val cpuMonitorEngine: CpuMonitorEngine
        get() {
            if (_cpuMonitorEngine == null) {
                _cpuMonitorEngine = CpuMonitorEngine()
            }
            return _cpuMonitorEngine!!
        }

    fun getTouchVisualizationEngine(context: Context): TouchVisualizationEngine {
        if (_touchVisualizationEngine == null) {
            _touchVisualizationEngine = TouchVisualizationEngine(context.applicationContext)
        }
        return _touchVisualizationEngine!!
    }

    val viewBordersEngine: ViewBordersEngine
        get() {
            if (_viewBordersEngine == null) {
                _viewBordersEngine = ViewBordersEngine()
            }
            return _viewBordersEngine!!
        }

    val logCaptureEngine: LogCaptureEngine
        get() {
            if (_logCaptureEngine == null) {
                _logCaptureEngine = LogCaptureEngine()
            }
            return _logCaptureEngine!!
        }

    val webSocketMonitorEngine: WebSocketMonitorEngine
        get() {
            if (_webSocketMonitorEngine == null) {
                _webSocketMonitorEngine = WebSocketMonitorEngine()
            }
            return _webSocketMonitorEngine!!
        }

    val leakDetectionEngine: LeakDetectionEngine
        get() {
            if (_leakDetectionEngine == null) {
                _leakDetectionEngine = LeakDetectionEngine()
            }
            return _leakDetectionEngine!!
        }

    val threadViolationEngine: ThreadViolationEngine
        get() {
            if (_threadViolationEngine == null) {
                _threadViolationEngine = ThreadViolationEngine()
            }
            return _threadViolationEngine!!
        }

    /**
     * Clears all engine instances.
     * Use this only for testing or complete cleanup scenarios.
     */
    fun clear() {
        _memoryMonitorEngine?.stop()
        _fpsMonitorEngine?.stop()
        _cpuMonitorEngine?.stop()
        _touchVisualizationEngine?.disable()
        _viewBordersEngine?.disable()
        _logCaptureEngine?.stop()
        _leakDetectionEngine?.stop()
        _threadViolationEngine?.disable()

        _memoryMonitorEngine = null
        _fpsMonitorEngine = null
        _cpuMonitorEngine = null
        _touchVisualizationEngine = null
        _viewBordersEngine = null
        _logCaptureEngine = null
        _webSocketMonitorEngine = null
        _leakDetectionEngine = null
        _threadViolationEngine = null
    }
}

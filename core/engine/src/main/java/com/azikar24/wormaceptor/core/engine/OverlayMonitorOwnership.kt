package com.azikar24.wormaceptor.core.engine

/**
 * Tracks whether the performance overlay started a shared monitor engine, so removing a metric
 * from the overlay only stops monitoring the overlay itself started. Monitoring started from a
 * feature screen keeps running.
 */
internal class OverlayMonitorOwnership(
    private val isRunning: () -> Boolean,
    private val start: () -> Unit,
    private val stop: () -> Unit,
) {
    var ownsEngine = false
        private set

    fun acquire() {
        if (isRunning()) return
        start()
        ownsEngine = true
    }

    fun release() {
        if (!ownsEngine) return
        ownsEngine = false
        stop()
    }

    /** Someone else stopped the engine; a later restart is theirs, not the overlay's. */
    fun onEngineStopped() {
        ownsEngine = false
    }
}

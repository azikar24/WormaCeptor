/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.fps.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.FpsMonitorEngine
import com.azikar24.wormaceptor.domain.entities.FpsInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the FPS Monitor screen.
 *
 * Provides access to FPS monitoring data and controls for starting/stopping
 * the monitor and resetting statistics.
 */
class FpsViewModel(
    private val fpsMonitorEngine: FpsMonitorEngine,
) : ViewModel() {

    /**
     * Current FPS information including real-time metrics.
     */
    val currentFpsInfo: StateFlow<FpsInfo> = fpsMonitorEngine.currentFpsInfo

    /**
     * Historical FPS data for charting (last 60 samples).
     */
    val fpsHistory: StateFlow<ImmutableList<FpsInfo>> = fpsMonitorEngine.fpsHistory
        .map { it.toImmutableList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    /**
     * Whether the FPS monitor is currently running.
     */
    val isMonitoring: StateFlow<Boolean> = fpsMonitorEngine.isRunning

    /**
     * Starts FPS monitoring.
     */
    fun startMonitoring() {
        fpsMonitorEngine.start()
    }

    /**
     * Stops FPS monitoring.
     */
    fun stopMonitoring() {
        fpsMonitorEngine.stop()
    }

    /**
     * Toggles monitoring state.
     */
    fun toggleMonitoring() {
        if (isMonitoring.value) {
            stopMonitoring()
        } else {
            startMonitoring()
        }
    }

    /**
     * Resets all FPS statistics and history.
     */
    fun resetStats() {
        fpsMonitorEngine.reset()
    }

    override fun onCleared() {
        super.onCleared()
        // Note: We don't stop the engine here - monitoring persists across navigation.
        // The engine lifecycle is managed by the user via explicit start/stop.
    }
}

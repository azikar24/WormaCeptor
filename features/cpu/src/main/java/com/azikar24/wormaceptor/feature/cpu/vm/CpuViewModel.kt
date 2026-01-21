/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.cpu.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the CPU Monitoring screen.
 *
 * Provides access to CPU metrics and controls for monitoring behavior.
 * Automatically starts monitoring when subscribed to and stops when cleared.
 */
class CpuViewModel(
    private val engine: CpuMonitorEngine,
) : ViewModel() {

    // Current CPU snapshot
    val currentCpu: StateFlow<CpuInfo> = engine.currentCpu
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CpuInfo.empty(),
        )

    // CPU history for charts
    val cpuHistory: StateFlow<ImmutableList<CpuInfo>> = engine.cpuHistory
        .map { it.toImmutableList() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            persistentListOf(),
        )

    // Monitoring state
    val isMonitoring: StateFlow<Boolean> = engine.isMonitoring

    // CPU warning indicator (when usage exceeds threshold)
    val isCpuWarning: StateFlow<Boolean> = engine.currentCpu
        .map { it.overallUsagePercent >= CpuMonitorEngine.CPU_WARNING_THRESHOLD }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false,
        )

    init {
        // Auto-start monitoring when ViewModel is created
        startMonitoring()
    }

    /**
     * Starts CPU monitoring.
     */
    fun startMonitoring() {
        engine.start()
    }

    /**
     * Stops CPU monitoring.
     */
    fun stopMonitoring() {
        engine.stop()
    }

    /**
     * Clears the CPU history.
     */
    fun clearHistory() {
        engine.clearHistory()
    }

    override fun onCleared() {
        super.onCleared()
        // Stop monitoring when ViewModel is cleared
        engine.stop()
    }
}

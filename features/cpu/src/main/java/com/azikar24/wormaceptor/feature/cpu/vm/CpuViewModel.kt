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
 * Monitoring state is managed by the engine and persists across navigation.
 * User must explicitly start/stop monitoring.
 */
class CpuViewModel(
    private val engine: CpuMonitorEngine,
) : ViewModel() {

    /** Current CPU snapshot with usage percentages and per-core data. */
    val currentCpu: StateFlow<CpuInfo> = engine.currentCpu
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CpuInfo.empty(),
        )

    /** Historical CPU snapshots used for rendering time-series charts. */
    val cpuHistory: StateFlow<ImmutableList<CpuInfo>> = engine.cpuHistory
        .map { it.toImmutableList() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            persistentListOf(),
        )

    /** Whether CPU monitoring is currently active. */
    val isMonitoring: StateFlow<Boolean> = engine.isMonitoring

    /** Whether CPU usage exceeds the warning threshold. */
    val isCpuWarning: StateFlow<Boolean> = engine.currentCpu
        .map { it.overallUsagePercent >= CpuMonitorEngine.CPU_WARNING_THRESHOLD }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false,
        )

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
}

/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.memory.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.domain.entities.MemoryInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Memory Monitoring screen.
 *
 * Provides access to memory metrics and controls for monitoring behavior.
 * Monitoring state is managed by the engine and persists across navigation.
 * User must explicitly start/stop monitoring.
 */
class MemoryViewModel(
    private val engine: MemoryMonitorEngine,
) : ViewModel() {

    // Current memory snapshot
    val currentMemory: StateFlow<MemoryInfo> = engine.currentMemory
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            MemoryInfo.empty(),
        )

    // Memory history for charts
    val memoryHistory: StateFlow<ImmutableList<MemoryInfo>> = engine.memoryHistory
        .map { it.toImmutableList() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            persistentListOf(),
        )

    // Monitoring state
    val isMonitoring: StateFlow<Boolean> = engine.isMonitoring

    // Heap warning indicator (when usage exceeds threshold)
    val isHeapWarning: StateFlow<Boolean> = engine.currentMemory
        .map { it.heapUsagePercent >= MemoryMonitorEngine.HEAP_WARNING_THRESHOLD }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false,
        )

    /**
     * Starts memory monitoring.
     */
    fun startMonitoring() {
        engine.start()
    }

    /**
     * Stops memory monitoring.
     */
    fun stopMonitoring() {
        engine.stop()
    }

    /**
     * Forces a garbage collection run.
     */
    fun forceGc() {
        engine.forceGc()
    }

    /**
     * Clears the memory history.
     */
    fun clearHistory() {
        engine.clearHistory()
    }

    override fun onCleared() {
        super.onCleared()
        // Note: We don't stop the engine here - monitoring persists across navigation.
        // The engine lifecycle is managed by the user via explicit start/stop.
    }
}

package com.azikar24.wormaceptor.feature.memory.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

/**
 * ViewModel for the Memory Monitoring screen.
 *
 * Consolidates memory metrics from [MemoryMonitorEngine] into a single [MemoryViewState]
 * and exposes user actions via [MemoryViewEvent].
 * Monitoring state is managed by the engine and persists across navigation.
 * User must explicitly start/stop monitoring.
 */
class MemoryViewModel(
    private val engine: MemoryMonitorEngine,
) : BaseViewModel<MemoryViewState, MemoryViewEffect, MemoryViewEvent>(
    initialState = MemoryViewState(),
) {

    init {
        combine(
            engine.currentMemory,
            engine.memoryHistory,
            engine.isMonitoring,
        ) { currentMemory, history, monitoring ->
            updateState {
                copy(
                    currentMemory = currentMemory,
                    memoryHistory = history.toImmutableList(),
                    isMonitoring = monitoring,
                    isHeapWarning = currentMemory.heapUsagePercent >= MemoryMonitorEngine.HEAP_WARNING_THRESHOLD,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun handleEvent(event: MemoryViewEvent) {
        when (event) {
            is MemoryViewEvent.StartMonitoring -> engine.start()
            is MemoryViewEvent.StopMonitoring -> engine.stop()
            is MemoryViewEvent.ForceGc -> engine.forceGc()
            is MemoryViewEvent.ClearHistory -> engine.clearHistory()
        }
    }
}

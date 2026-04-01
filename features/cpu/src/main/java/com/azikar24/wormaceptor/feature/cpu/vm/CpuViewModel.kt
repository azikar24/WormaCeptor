package com.azikar24.wormaceptor.feature.cpu.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

/**
 * ViewModel for the CPU Monitoring screen.
 *
 * Consolidates CPU metrics from [CpuMonitorEngine] into a single [CpuViewState]
 * and exposes user actions via [CpuEvent].
 */
class CpuViewModel(
    private val engine: CpuMonitorEngine,
) : BaseViewModel<CpuViewState, CpuEffect, CpuEvent>(
    initialState = CpuViewState(),
) {

    init {
        combine(
            engine.currentCpu,
            engine.cpuHistory,
            engine.isMonitoring,
        ) { currentCpu, history, monitoring ->
            updateState {
                copy(
                    currentCpu = currentCpu,
                    cpuHistory = history.toImmutableList(),
                    isMonitoring = monitoring,
                    isCpuWarning = currentCpu.overallUsagePercent >= CpuMonitorEngine.CPU_WARNING_THRESHOLD,
                    formattedUptime = formatUptime(currentCpu.uptime),
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun handleEvent(event: CpuEvent) {
        when (event) {
            is CpuEvent.StartMonitoring -> engine.start()
            is CpuEvent.StopMonitoring -> engine.stop()
            is CpuEvent.ClearHistory -> engine.clearHistory()
        }
    }

    private fun formatUptime(uptimeMs: Long): String {
        if (uptimeMs <= 0) return ""
        val seconds = uptimeMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "${days}d ${hours % 24}h ${minutes % 60}m"
            hours > 0 -> "${hours}h ${minutes % 60}m ${seconds % 60}s"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
}

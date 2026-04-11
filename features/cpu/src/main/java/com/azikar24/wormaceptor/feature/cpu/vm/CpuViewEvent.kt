package com.azikar24.wormaceptor.feature.cpu.vm

/** User actions dispatched from the CPU monitoring UI. */
sealed class CpuViewEvent {
    data object StartMonitoring : CpuViewEvent()
    data object StopMonitoring : CpuViewEvent()
    data object ClearHistory : CpuViewEvent()
}

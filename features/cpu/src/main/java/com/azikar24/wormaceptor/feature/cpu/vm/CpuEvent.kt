package com.azikar24.wormaceptor.feature.cpu.vm

/** User actions dispatched from the CPU monitoring UI. */
sealed class CpuEvent {
    data object StartMonitoring : CpuEvent()
    data object StopMonitoring : CpuEvent()
    data object ClearHistory : CpuEvent()
}

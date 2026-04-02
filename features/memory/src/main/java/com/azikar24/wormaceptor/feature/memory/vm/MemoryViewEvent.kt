package com.azikar24.wormaceptor.feature.memory.vm

/** User actions dispatched from the Memory monitoring UI. */
sealed class MemoryViewEvent {
    data object StartMonitoring : MemoryViewEvent()
    data object StopMonitoring : MemoryViewEvent()
    data object ForceGc : MemoryViewEvent()
    data object ClearHistory : MemoryViewEvent()
}

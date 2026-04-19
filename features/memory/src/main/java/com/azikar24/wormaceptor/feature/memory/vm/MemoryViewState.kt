package com.azikar24.wormaceptor.feature.memory.vm

import com.azikar24.wormaceptor.domain.entities.MemoryInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class MemoryViewState(
    val currentMemory: MemoryInfo = MemoryInfo.empty(),
    val memoryHistory: ImmutableList<MemoryInfo> = persistentListOf(),
    val isMonitoring: Boolean = false,
    val isHeapWarning: Boolean = false,
)

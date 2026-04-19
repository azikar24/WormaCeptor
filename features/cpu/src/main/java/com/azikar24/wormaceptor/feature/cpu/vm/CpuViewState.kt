package com.azikar24.wormaceptor.feature.cpu.vm

import com.azikar24.wormaceptor.domain.entities.CpuInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class CpuViewState(
    val currentCpu: CpuInfo = CpuInfo.empty(),
    val cpuHistory: ImmutableList<CpuInfo> = persistentListOf(),
    val isMonitoring: Boolean = false,
    val isCpuWarning: Boolean = false,
    val formattedUptime: String = "",
)

package com.azikar24.wormaceptor.feature.leakdetection.vm

import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity
import com.azikar24.wormaceptor.domain.entities.LeakSummary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LeakDetectionViewState(
    val filteredLeaks: ImmutableList<LeakInfo> = persistentListOf(),
    val summary: LeakSummary = LeakSummary.empty(),
    val isRunning: Boolean = false,
    val selectedSeverity: LeakSeverity? = null,
    val selectedLeak: LeakInfo? = null,
)

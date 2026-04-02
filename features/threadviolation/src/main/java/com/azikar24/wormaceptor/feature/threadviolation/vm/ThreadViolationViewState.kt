package com.azikar24.wormaceptor.feature.threadviolation.vm

import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.domain.entities.ThreadViolation.ViolationType
import com.azikar24.wormaceptor.domain.entities.ViolationStats
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ThreadViolationViewState(
    val filteredViolations: ImmutableList<ThreadViolation> = persistentListOf(),
    val stats: ViolationStats = ViolationStats.empty(),
    val isMonitoring: Boolean = false,
    val selectedType: ViolationType? = null,
    val selectedViolation: ThreadViolation? = null,
)

package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.domain.entities.Crash
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class CrashListViewState(
    val crashes: ImmutableList<Crash> = persistentListOf(),
    val isCrashesLoading: Boolean = true,
    val isRefreshingCrashes: Boolean = false,
    val showClearCrashesDialog: Boolean = false,
)

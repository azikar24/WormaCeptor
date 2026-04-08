package com.azikar24.wormaceptor.feature.fps.vm

import com.azikar24.wormaceptor.domain.entities.FpsInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class FpsViewState(
    val currentFpsInfo: FpsInfo = FpsInfo.EMPTY,
    val fpsHistory: ImmutableList<FpsInfo> = persistentListOf(),
    val isMonitoring: Boolean = false,
    /** Whether the current FPS level is in the warning range. */
    val isFpsWarning: Boolean = false,
)

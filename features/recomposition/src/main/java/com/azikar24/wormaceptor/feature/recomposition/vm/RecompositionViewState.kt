package com.azikar24.wormaceptor.feature.recomposition.vm

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Immutable snapshot of the recomposition dashboard state. */
data class RecompositionViewState(
    val sessionDurationMs: Long = 0L,
    val totalRecompositions: Long = 0L,
    val topRecomposers: ImmutableList<RecompositionItem> = persistentListOf(),
)

package com.azikar24.wormaceptor.feature.recomposition.vm

/** A single composable entry in the recomposition list. */
data class RecompositionItem(
    val name: String,
    val count: Long,
    val ratePerSecond: Float,
)

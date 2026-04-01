package com.azikar24.wormaceptor.feature.recomposition.vm

/** User actions dispatched from the recomposition summary UI. */
sealed class RecompositionEvent {
    data object Reset : RecompositionEvent()
}

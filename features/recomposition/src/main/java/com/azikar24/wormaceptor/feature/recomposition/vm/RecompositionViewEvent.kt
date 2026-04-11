package com.azikar24.wormaceptor.feature.recomposition.vm

/** User actions dispatched from the recomposition summary UI. */
sealed class RecompositionViewEvent {
    data object Reset : RecompositionViewEvent()
}

package com.azikar24.wormaceptor.feature.recomposition.vm

/** User actions dispatched from the recomposition summary UI. */
internal sealed class RecompositionEvent {
    data object Reset : RecompositionEvent()
}

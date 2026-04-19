package com.azikar24.wormaceptor.feature.recomposition.vm

sealed class RecompositionViewEvent {
    data object Reset : RecompositionViewEvent()
    data object BackPressed : RecompositionViewEvent()
}

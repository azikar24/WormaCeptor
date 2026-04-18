package com.azikar24.wormaceptor.feature.recomposition.vm

sealed class RecompositionEffect {
    data object NavigateBack : RecompositionEffect()
}

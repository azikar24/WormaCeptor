package com.azikar24.wormaceptor.feature.fps.vm

/** User actions dispatched from the FPS monitoring UI. */
sealed class FpsViewEvent {
    data object StartMonitoring : FpsViewEvent()
    data object StopMonitoring : FpsViewEvent()
    data object ToggleMonitoring : FpsViewEvent()
    data object ResetStats : FpsViewEvent()
}

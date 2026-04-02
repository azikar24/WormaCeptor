package com.azikar24.wormaceptor.feature.fps.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.FpsMonitorEngine
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

/**
 * ViewModel for the FPS Monitor screen.
 *
 * Consolidates FPS metrics from [FpsMonitorEngine] into a single [FpsViewState]
 * and exposes user actions via [FpsViewEvent].
 * Monitoring state is managed by the engine and persists across navigation.
 */
class FpsViewModel(
    private val fpsMonitorEngine: FpsMonitorEngine,
) : BaseViewModel<FpsViewState, FpsViewEffect, FpsViewEvent>(
    initialState = FpsViewState(),
) {

    init {
        combine(
            fpsMonitorEngine.currentFpsInfo,
            fpsMonitorEngine.fpsHistory,
            fpsMonitorEngine.isRunning,
        ) { currentFps, history, running ->
            updateState {
                copy(
                    currentFpsInfo = currentFps,
                    fpsHistory = history.toImmutableList(),
                    isMonitoring = running,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun handleEvent(event: FpsViewEvent) {
        when (event) {
            is FpsViewEvent.StartMonitoring -> fpsMonitorEngine.start()
            is FpsViewEvent.StopMonitoring -> fpsMonitorEngine.stop()
            is FpsViewEvent.ToggleMonitoring -> {
                if (uiState.value.isMonitoring) {
                    fpsMonitorEngine.stop()
                } else {
                    fpsMonitorEngine.start()
                }
            }
            is FpsViewEvent.ResetStats -> fpsMonitorEngine.reset()
        }
    }
}

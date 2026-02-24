package com.azikar24.wormaceptor.common.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Base MVI ViewModel providing reactive [uiState], one-time [effects], and a single [sendEvent] entry point.
 */
abstract class BaseViewModel<State, Effect, Event>(initialState: State) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)

    /** Observable UI state that drives recomposition. */
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<Effect>(extraBufferCapacity = Int.MAX_VALUE)

    /** One-time side-effects consumed by the UI layer. */
    val effects: SharedFlow<Effect> = _effects.asSharedFlow()

    /** Single entry point for dispatching UI events into the ViewModel. */
    fun sendEvent(event: Event) {
        handleEvent(event)
    }

    protected abstract fun handleEvent(event: Event)

    protected fun updateState(reducer: State.() -> State) {
        _uiState.update(reducer)
    }

    protected fun emitEffect(effect: Effect) {
        _effects.tryEmit(effect)
    }
}

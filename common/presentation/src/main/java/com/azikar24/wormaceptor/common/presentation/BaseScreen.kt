package com.azikar24.wormaceptor.common.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

/**
 * Connects a [BaseViewModel] to a Composable, collecting [BaseViewModel.uiState] and [BaseViewModel.effects].
 */
@Composable
fun <State, Effect, Event> BaseScreen(
    viewModel: BaseViewModel<State, Effect, Event>,
    onEffect: (Effect) -> Unit,
    content: @Composable (state: State, onEvent: (Event) -> Unit) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            onEffect(effect)
        }
    }

    content(state, viewModel::sendEvent)
}

package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.CryptoEngine
import com.azikar24.wormaceptor.domain.entities.CryptoResult
import com.azikar24.wormaceptor.feature.crypto.CryptoFeature
import com.azikar24.wormaceptor.feature.crypto.R
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewEvent
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewModel
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewState
import kotlinx.coroutines.launch

/**
 * History screen for viewing past crypto operations.
 */
@Composable
fun CryptoHistoryScreen(
    engine: CryptoEngine,
    onNavigateBack: () -> Unit,
    onLoadResult: (CryptoResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val factory = remember { CryptoFeature.createViewModelFactory(engine) }
    val viewModel: CryptoViewModel = viewModel(factory = factory)
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val loadedMessage = stringResource(R.string.crypto_loaded_to_tool)

    BaseScreen(viewModel) { state, onEvent ->
        CryptoHistoryContent(
            state = state,
            snackBarHostState = snackBarHostState,
            onNavigateBack = onNavigateBack,
            onLoadResult = { result ->
                onLoadResult(result)
                scope.launch { snackBarHostState.showSnackbar(loadedMessage) }
            },
            onEvent = onEvent,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CryptoHistoryContent(
    state: CryptoViewState,
    snackBarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onLoadResult: (CryptoResult) -> Unit,
    onEvent: (CryptoViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.crypto_history_title),
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.crypto_back))
                    }
                },
                actions = {
                    if (state.history.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onEvent(CryptoViewEvent.History.ClearAll)
                            },
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                stringResource(R.string.crypto_clear_all),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (state.history.isEmpty()) {
            HistoryEmptyState(modifier = Modifier.padding(padding))
        } else {
            HistoryList(
                history = state.history,
                onLoadResult = onLoadResult,
                onRemove = { id -> onEvent(CryptoViewEvent.History.Remove(id)) },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

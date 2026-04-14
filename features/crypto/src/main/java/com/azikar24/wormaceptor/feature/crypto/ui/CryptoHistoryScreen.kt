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
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorAlertDialog
import com.azikar24.wormaceptor.feature.crypto.R
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewEvent
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CryptoHistoryContent(
    state: CryptoViewState,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
    onEvent: (CryptoViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    if (state.showClearHistoryConfirmation) {
        WormaCeptorAlertDialog(
            title = stringResource(R.string.crypto_clear_history_title),
            message = stringResource(R.string.crypto_clear_history_message),
            confirmLabel = stringResource(R.string.crypto_clear_all),
            onConfirm = { onEvent(CryptoViewEvent.History.ConfirmClearAll) },
            dismissLabel = stringResource(R.string.crypto_cancel),
            onDismiss = { onEvent(CryptoViewEvent.History.DismissClearConfirmation) },
            destructive = true,
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                onEvent(CryptoViewEvent.History.RequestClearAll)
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
                onLoadResult = { result -> onEvent(CryptoViewEvent.History.Load(result)) },
                onRemove = { id -> onEvent(CryptoViewEvent.History.Remove(id)) },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

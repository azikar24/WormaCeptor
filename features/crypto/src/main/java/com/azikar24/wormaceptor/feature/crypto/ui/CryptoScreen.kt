package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorIconButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.CryptoResult
import com.azikar24.wormaceptor.feature.crypto.R
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewEvent
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CryptoToolContent(
    state: CryptoViewState,
    onEvent: (CryptoViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            CryptoTopBar(
                hasHistory = state.history.isNotEmpty(),
                onEvent = onEvent,
            )
        },
    ) { padding ->
        CryptoToolBody(
            state = state,
            onEvent = onEvent,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun CryptoTopBar(
    hasHistory: Boolean,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    WormaCeptorTopBar(
        title = stringResource(R.string.crypto_title),
        onBack = { onEvent(CryptoViewEvent.Navigation.BackPressed) },
        backContentDescription = stringResource(R.string.crypto_back),
        actions = {
            if (hasHistory) {
                WormaCeptorIconButton(onClick = { onEvent(CryptoViewEvent.Navigation.ShowHistory) }) {
                    Icon(
                        Icons.Default.History,
                        stringResource(R.string.crypto_history),
                        tint = WormaCeptorTokens.semantic().textSecondary,
                    )
                }
            }
        },
    )
}

@Composable
private fun CryptoToolBody(
    state: CryptoViewState,
    onEvent: (CryptoViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = WormaCeptorTokens.Spacing.lg,
                top = WormaCeptorTokens.Spacing.lg,
                end = WormaCeptorTokens.Spacing.lg,
                bottom = WormaCeptorTokens.Spacing.lg +
                    WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding(),
            ),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
    ) {
        PresetsSection(
            config = state.config,
            onApplyPreset = { onEvent(CryptoViewEvent.Config.ApplyPreset(it)) },
        )
        AlgorithmModeSection(
            config = state.config,
            onEvent = onEvent,
        )
        KeyIvSection(
            config = state.config,
            onEvent = onEvent,
        )
        InputSection(
            inputText = state.inputText,
            isProcessing = state.isProcessing,
            config = state.config,
            onEvent = onEvent,
        )
        ResultSection(
            currentResult = state.currentResult,
            error = state.error,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun ResultSection(
    currentResult: CryptoResult?,
    error: String?,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    val lastResult = remember { mutableStateOf<CryptoResult?>(null) }
    val lastError = remember { mutableStateOf<String?>(null) }

    if (currentResult != null) lastResult.value = currentResult
    if (error != null) lastError.value = error

    val isVisible = currentResult != null || error != null

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        val displayResult = currentResult ?: lastResult.value
        val displayError = error ?: lastError.value

        displayResult?.let { result ->
            ResultCard(
                result = result,
                onCopy = { onEvent(CryptoViewEvent.Result.Copy(it)) },
                onClear = { onEvent(CryptoViewEvent.Result.Clear) },
                onUseAsInput = { onEvent(CryptoViewEvent.Result.UseAsInput(it)) },
            )
        } ?: displayError?.let { errorMessage ->
            ErrorCard(
                message = errorMessage,
                onDismiss = { onEvent(CryptoViewEvent.Result.Clear) },
            )
        }
    }

    if (!isVisible) {
        lastResult.value = null
        lastError.value = null
    }
}

@Preview(showBackground = true)
@Composable
private fun CryptoToolContentPreview() {
    WormaCeptorTheme {
        CryptoToolContent(
            state = CryptoViewState(inputText = "Hello World"),
            onEvent = {},
        )
    }
}

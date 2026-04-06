package com.azikar24.wormaceptor.feature.crypto

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.CryptoEngine
import com.azikar24.wormaceptor.core.ui.util.copyToClipboard
import com.azikar24.wormaceptor.feature.crypto.ui.CryptoHistoryContent
import com.azikar24.wormaceptor.feature.crypto.ui.CryptoToolContent
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewEffect
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewEvent
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewModel
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewModelFactory
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Entry point for the Response Encryption/Decryption feature.
 * Provides factory methods and composable entry point.
 */
object CryptoFeature {
    /**
     * Creates a CryptoViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(engine: CryptoEngine): CryptoViewModelFactory = CryptoViewModelFactory(engine)
}

/**
 * Main composable entry point for the Crypto feature.
 * Owns the shared ViewModel and routes between tool and history screens.
 */
@Composable
fun CryptoScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: CryptoEngine = koinInject()
    val factory = remember(engine) { CryptoFeature.createViewModelFactory(engine) }
    val viewModel: CryptoViewModel = viewModel(factory = factory)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val copiedMessage = stringResource(R.string.crypto_copied_to_clipboard)
    val outputLoadedMessage = stringResource(R.string.crypto_output_loaded_as_input)
    val clipboardLabel = stringResource(R.string.crypto_clipboard_label)
    val keyGeneratedMessage = stringResource(R.string.crypto_key_generated)
    val ivGeneratedMessage = stringResource(R.string.crypto_iv_generated)
    val loadedMessage = stringResource(R.string.crypto_loaded_to_tool)

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is CryptoViewEffect.CopyToClipboard -> {
                    copyToClipboard(context, clipboardLabel, effect.text)
                    scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                }
                is CryptoViewEffect.KeyGenerated ->
                    scope.launch { snackbarHostState.showSnackbar(keyGeneratedMessage) }
                is CryptoViewEffect.IvGenerated ->
                    scope.launch { snackbarHostState.showSnackbar(ivGeneratedMessage) }
                is CryptoViewEffect.OutputLoadedAsInput ->
                    scope.launch { snackbarHostState.showSnackbar(outputLoadedMessage) }
                is CryptoViewEffect.HistoryLoaded ->
                    scope.launch { snackbarHostState.showSnackbar(loadedMessage) }
            }
        },
    ) { state, onEvent ->
        if (state.showHistory) {
            CryptoHistoryContent(
                state = state,
                snackbarHostState = snackbarHostState,
                onNavigateBack = { onEvent(CryptoViewEvent.Navigation.HideHistory) },
                onEvent = onEvent,
                modifier = modifier,
            )
        } else {
            CryptoToolContent(
                state = state,
                snackbarHostState = snackbarHostState,
                onEvent = onEvent,
                onNavigateBack = onNavigateBack,
                onNavigateToHistory = { onEvent(CryptoViewEvent.Navigation.ShowHistory) },
                modifier = modifier,
            )
        }
    }
}

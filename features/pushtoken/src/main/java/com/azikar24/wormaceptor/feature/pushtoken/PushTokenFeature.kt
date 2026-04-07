package com.azikar24.wormaceptor.feature.pushtoken

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.PushTokenEngine
import com.azikar24.wormaceptor.feature.pushtoken.ui.PushTokenScreen
import com.azikar24.wormaceptor.feature.pushtoken.vm.PushTokenEffect
import com.azikar24.wormaceptor.feature.pushtoken.vm.PushTokenEvent
import com.azikar24.wormaceptor.feature.pushtoken.vm.PushTokenViewModel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

/** Entry point for the Push Token management feature. */
object PushTokenFeature {
    /** Creates a [PushTokenViewModelFactory] for use with viewModel(). */
    fun createViewModelFactory(engine: PushTokenEngine): PushTokenViewModelFactory {
        return PushTokenViewModelFactory(engine)
    }
}

/** Factory for creating [PushTokenViewModel] instances with the required engine. */
class PushTokenViewModelFactory(
    private val engine: PushTokenEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PushTokenViewModel::class.java)) {
            return PushTokenViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/** Composable entry point that wires the ViewModel to the Push Token screen. */
@Composable
fun PushTokenManager(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: PushTokenEngine = koinInject()
    val factory = remember { PushTokenFeature.createViewModelFactory(engine) }
    val viewModel: PushTokenViewModel = viewModel(factory = factory)

    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    val clipboardLabel = stringResource(R.string.pushtoken_clipboard_label)
    var showCopiedSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.sendEvent(PushTokenEvent.FetchToken) }

    LaunchedEffect(showCopiedSnackbar) {
        if (showCopiedSnackbar) {
            delay(SnackbarDurationMs)
            showCopiedSnackbar = false
        }
    }

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is PushTokenEffect.CopyToClipboard -> {
                    clipboardManager.setPrimaryClip(
                        ClipData.newPlainText(clipboardLabel, effect.token),
                    )
                    showCopiedSnackbar = true
                }
            }
        },
    ) { state, onEvent ->
        PushTokenScreen(
            state = state,
            onEvent = onEvent,
            onBack = onNavigateBack,
            modifier = modifier,
            showCopiedSnackbar = showCopiedSnackbar,
        )
    }
}

private const val SnackbarDurationMs = 2_000L

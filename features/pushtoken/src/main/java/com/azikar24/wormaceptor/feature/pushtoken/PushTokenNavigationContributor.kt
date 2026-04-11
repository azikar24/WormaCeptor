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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.PushTokenEngine
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.pushtoken.ui.PushTokenScreen
import com.azikar24.wormaceptor.feature.pushtoken.vm.PushTokenEffect
import com.azikar24.wormaceptor.feature.pushtoken.vm.PushTokenViewEvent
import com.azikar24.wormaceptor.feature.pushtoken.vm.PushTokenViewModel
import com.google.auto.service.AutoService
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

private const val SnackbarDurationMs = 2_000L

@AutoService(FeatureNavigationContributor::class)
class PushTokenNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.PushToken.route) {
            PushTokenDestination(onBack = onBack)
        }
    }
}

@Composable
private fun PushTokenDestination(onBack: () -> Unit) {
    val engine: PushTokenEngine = koinInject()
    val factory = remember { PushTokenViewModelFactory(engine) }
    val viewModel: PushTokenViewModel = viewModel(factory = factory)

    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    val clipboardLabel = stringResource(R.string.pushtoken_clipboard_label)
    var showCopiedSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.sendEvent(PushTokenViewEvent.FetchToken) }

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
            onBack = { onBack() },
            showCopiedSnackbar = showCopiedSnackbar,
        )
    }
}

package com.azikar24.wormaceptor.feature.deviceinfo.navigation

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.components.rememberHapticOnce
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.core.ui.util.copyToClipboard
import com.azikar24.wormaceptor.feature.deviceinfo.DeviceInfoViewModelFactory
import com.azikar24.wormaceptor.feature.deviceinfo.R
import com.azikar24.wormaceptor.feature.deviceinfo.ui.DeviceInfoScreenContent
import com.azikar24.wormaceptor.feature.deviceinfo.vm.DeviceInfoViewEffect
import com.azikar24.wormaceptor.feature.deviceinfo.vm.DeviceInfoViewModel
import kotlinx.coroutines.launch

/**
 * Adds the Device Info navigation route to the [NavGraphBuilder].
 */
fun NavGraphBuilder.deviceInfoRoute(
    context: Context,
    onNavigateBack: () -> Unit,
) {
    composable(WormaCeptorNavKeys.DeviceInfo.route) {
        DeviceInfoDestination(context = context, onNavigateBack = onNavigateBack)
    }
}

@Suppress("ViewModelForwarding")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceInfoDestination(
    context: Context,
    onNavigateBack: () -> Unit,
    viewModel: DeviceInfoViewModel = viewModel(
        factory = DeviceInfoViewModelFactory(
            context.applicationContext as Application,
        ),
    ),
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticOnce()

    LaunchedEffect(pullToRefreshState.distanceFraction) {
        if (pullToRefreshState.distanceFraction >= 1f && !haptic.isTriggered) {
            haptic.triggerHaptic()
        } else if (pullToRefreshState.distanceFraction < 1f) {
            haptic.resetHaptic()
        }
    }

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is DeviceInfoViewEffect.CopyToClipboard -> {
                    val message = copyToClipboard(context, effect.label, effect.text)
                    scope.launch { snackBarHostState.showSnackbar(message) }
                }
                is DeviceInfoViewEffect.ShareText -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, effect.text)
                        putExtra(Intent.EXTRA_SUBJECT, effect.subject)
                    }
                    context.startActivity(
                        Intent.createChooser(
                            intent,
                            context.getString(R.string.deviceinfo_share_chooser),
                        ),
                    )
                }
            }
        },
    ) { state, onEvent ->
        LaunchedEffect(state.isRefreshing) {
            if (!state.isRefreshing) {
                haptic.resetHaptic()
            }
        }

        DeviceInfoScreenContent(
            state = state,
            snackBarHostState = snackBarHostState,
            pullToRefreshState = pullToRefreshState,
            onBack = onNavigateBack,
            onEvent = onEvent,
        )
    }
}

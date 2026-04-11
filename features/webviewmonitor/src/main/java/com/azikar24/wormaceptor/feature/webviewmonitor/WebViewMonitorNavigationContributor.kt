package com.azikar24.wormaceptor.feature.webviewmonitor

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavTransitions
import com.azikar24.wormaceptor.feature.webviewmonitor.ui.WebViewRequestDetailScreen
import com.azikar24.wormaceptor.feature.webviewmonitor.ui.components.WebViewMonitorListScreen
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewEffect
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewEvent
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewModel
import com.google.auto.service.AutoService
import org.koin.compose.koinInject

private object Routes {
    const val LIST = "list"
    const val DETAIL = "detail"
}

@AutoService(FeatureNavigationContributor::class)
class WebViewMonitorNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.WebViewMonitor.route) {
            WebViewMonitorDestination(onBack = onBack)
        }
    }
}

@Composable
private fun WebViewMonitorDestination(onBack: () -> Unit) {
    val engine: WebViewMonitorEngine = koinInject()
    val factory = remember(engine) { WebViewMonitorViewModelFactory(engine) }
    val viewModel: WebViewMonitorViewModel = viewModel(factory = factory)
    val innerNavController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.sendEvent(WebViewMonitorViewEvent.EnsureEnabled)
    }

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is WebViewMonitorViewEffect.NavigateToDetail -> {
                    innerNavController.navigate(Routes.DETAIL)
                }
            }
        },
    ) { state, onEvent ->
        NavHost(
            navController = innerNavController,
            startDestination = Routes.LIST,
            enterTransition = WormaCeptorNavTransitions.enterTransition,
            exitTransition = WormaCeptorNavTransitions.exitTransition,
            popEnterTransition = WormaCeptorNavTransitions.popEnterTransition,
            popExitTransition = WormaCeptorNavTransitions.popExitTransition,
        ) {
            composable(Routes.LIST) {
                WebViewMonitorListScreen(
                    state = state,
                    onEvent = onEvent,
                    onNavigateBack = { onBack() },
                )
            }

            composable(Routes.DETAIL) {
                if (state.selectedRequest != null) {
                    WebViewRequestDetailScreen(
                        state = state,
                        onBack = {
                            onEvent(WebViewMonitorViewEvent.ClearSelection)
                            innerNavController.popBackStack()
                        },
                    )
                } else {
                    LaunchedEffect(Unit) {
                        innerNavController.popBackStack()
                    }
                }
            }
        }
    }
}

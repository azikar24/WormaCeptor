package com.azikar24.wormaceptor.feature.webviewmonitor.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavTransitions
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.domain.entities.WebViewRequestStats
import com.azikar24.wormaceptor.domain.entities.WebViewResourceType
import com.azikar24.wormaceptor.feature.webviewmonitor.WebViewMonitorFeature
import com.azikar24.wormaceptor.feature.webviewmonitor.ui.components.WebViewMonitorListScreen
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewEffect
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewEvent
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewModel
import com.azikar24.wormaceptor.feature.webviewmonitor.vm.WebViewMonitorViewState
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.koinInject

private object Routes {
    const val LIST = "list"
    const val DETAIL = "detail"
}

/** Main composable for the WebView Monitor feature. */
@Composable
fun WebViewMonitor(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: WebViewMonitorEngine = koinInject()
    WebViewMonitor(
        engine = engine,
        modifier = modifier,
        onNavigateBack = onNavigateBack,
    )
}

/** Main composable for the WebView Monitor feature with external engine. */
@Composable
fun WebViewMonitor(
    engine: WebViewMonitorEngine,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val factory = remember(engine) { WebViewMonitorFeature.createViewModelFactory(engine) }
    val viewModel: WebViewMonitorViewModel = viewModel(factory = factory)
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        viewModel.sendEvent(WebViewMonitorViewEvent.EnsureEnabled)
    }

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is WebViewMonitorViewEffect.NavigateToDetail -> {
                    navController.navigate(Routes.DETAIL)
                }
            }
        },
    ) { state, onEvent ->
        NavHost(
            navController = navController,
            startDestination = Routes.LIST,
            modifier = modifier,
            enterTransition = WormaCeptorNavTransitions.enterTransition,
            exitTransition = WormaCeptorNavTransitions.exitTransition,
            popEnterTransition = WormaCeptorNavTransitions.popEnterTransition,
            popExitTransition = WormaCeptorNavTransitions.popExitTransition,
        ) {
            composable(Routes.LIST) {
                WebViewMonitorListScreen(
                    state = state,
                    onEvent = onEvent,
                    onNavigateBack = onNavigateBack,
                )
            }

            composable(Routes.DETAIL) {
                if (state.selectedRequest != null) {
                    WebViewRequestDetailScreen(
                        state = state,
                        onBack = {
                            onEvent(WebViewMonitorViewEvent.ClearSelection)
                            navController.popBackStack()
                        },
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun WebViewMonitorListScreenPreview() {
    val fixedTimestamp = 1_700_000_000_000L

    WormaCeptorTheme {
        WebViewMonitorListScreen(
            state = WebViewMonitorViewState(
                requests = persistentListOf(
                    WebViewRequest(
                        id = "1",
                        url = "https://api.example.com/v2/users",
                        method = "GET",
                        headers = mapOf("Accept" to "application/json"),
                        timestamp = fixedTimestamp - 3_000L,
                        webViewId = "main",
                        resourceType = WebViewResourceType.XHR,
                        statusCode = 200,
                        mimeType = "application/json",
                        contentLength = 1_024L,
                        duration = 150L,
                    ),
                    WebViewRequest(
                        id = "2",
                        url = "https://cdn.example.com/style.css",
                        method = "GET",
                        headers = emptyMap(),
                        timestamp = fixedTimestamp - 5_000L,
                        webViewId = "main",
                        resourceType = WebViewResourceType.STYLESHEET,
                        statusCode = 200,
                        contentLength = 4_096L,
                        duration = 80L,
                    ),
                    WebViewRequest(
                        id = "3",
                        url = "https://api.example.com/v2/analytics",
                        method = "POST",
                        headers = emptyMap(),
                        timestamp = fixedTimestamp - 1_000L,
                        webViewId = "main",
                        resourceType = WebViewResourceType.XHR,
                    ),
                ),
                filteredRequests = persistentListOf(
                    WebViewRequest(
                        id = "1",
                        url = "https://api.example.com/v2/users",
                        method = "GET",
                        headers = mapOf("Accept" to "application/json"),
                        timestamp = fixedTimestamp - 3_000L,
                        webViewId = "main",
                        resourceType = WebViewResourceType.XHR,
                        statusCode = 200,
                        mimeType = "application/json",
                        contentLength = 1_024L,
                        duration = 150L,
                    ),
                    WebViewRequest(
                        id = "3",
                        url = "https://api.example.com/v2/analytics",
                        method = "POST",
                        headers = emptyMap(),
                        timestamp = fixedTimestamp - 1_000L,
                        webViewId = "main",
                        resourceType = WebViewResourceType.XHR,
                    ),
                ),
                stats = WebViewRequestStats(
                    totalRequests = 3,
                    successfulRequests = 2,
                    failedRequests = 0,
                    pendingRequests = 1,
                    byResourceType = mapOf(
                        WebViewResourceType.XHR to 2,
                        WebViewResourceType.STYLESHEET to 1,
                    ),
                    averageDuration = 115L,
                    totalDataTransferred = 5_120L,
                ),
            ),
            onEvent = {},
            onNavigateBack = {},
        )
    }
}

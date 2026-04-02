package com.azikar24.wormaceptor.feature.websocket.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.WebSocketMonitorEngine
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.websocket.WebSocketFeature
import com.azikar24.wormaceptor.feature.websocket.ui.WebSocketDetailScreen
import com.azikar24.wormaceptor.feature.websocket.ui.WebSocketListScreen
import com.azikar24.wormaceptor.feature.websocket.vm.WebSocketViewEvent
import com.azikar24.wormaceptor.feature.websocket.vm.WebSocketViewModel
import org.koin.compose.koinInject

/**
 * Adds the WebSocket Monitor navigation graph to the [NavGraphBuilder].
 * Scopes the [WebSocketViewModel] to the graph so it is shared across screens.
 */
fun NavGraphBuilder.webSocketGraph(
    navController: NavHostController,
    onNavigateBack: () -> Unit,
) {
    navigation(
        startDestination = WormaCeptorNavKeys.WebSocketConnections.route,
        route = WormaCeptorNavKeys.WebSocket.route,
    ) {
        composable(WormaCeptorNavKeys.WebSocketConnections.route) { backStackEntry ->
            WebSocketConnectionsDestination(backStackEntry, navController, onNavigateBack)
        }
        composable(WormaCeptorNavKeys.WebSocketMessages.route) { backStackEntry ->
            WebSocketMessagesDestination(backStackEntry, navController)
        }
    }
}

@Composable
private fun graphScopedViewModel(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
): WebSocketViewModel {
    val graphEntry = remember(backStackEntry) {
        navController.getBackStackEntry(WormaCeptorNavKeys.WebSocket.route)
    }
    val engine: WebSocketMonitorEngine = koinInject()
    val factory = remember { WebSocketFeature.createViewModelFactory(engine) }
    return viewModel(viewModelStoreOwner = graphEntry, factory = factory)
}

@Composable
private fun WebSocketConnectionsDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    onNavigateBack: () -> Unit,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController)

    BaseScreen(viewModel) { state, onEvent ->
        WebSocketListScreen(
            connections = state.connections,
            searchQuery = state.connectionSearchQuery,
            totalCount = state.totalConnectionCount,
            onSearchQueryChanged = { query -> onEvent(WebSocketViewEvent.ConnectionSearchQueryChanged(query)) },
            onConnectionClick = { connection ->
                onEvent(WebSocketViewEvent.ConnectionSelected(connection.id))
                navController.navigate(WormaCeptorNavKeys.WebSocketMessages.route)
            },
            onClearAll = { onEvent(WebSocketViewEvent.ClearAll) },
            getMessageCount = viewModel::getMessageCountForConnection,
            onBack = onNavigateBack,
        )
    }
}

@Composable
private fun WebSocketMessagesDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController)

    BaseScreen(viewModel) { state, onEvent ->
        WebSocketDetailScreen(
            connection = state.selectedConnection,
            messages = state.messages,
            searchQuery = state.messageSearchQuery,
            directionFilter = state.directionFilter,
            totalMessageCount = state.totalMessageCount,
            directionCounts = state.directionCounts,
            expandedMessageId = state.expandedMessageId,
            onSearchQueryChanged = { query -> onEvent(WebSocketViewEvent.MessageSearchQueryChanged(query)) },
            onDirectionFilterToggle = { direction -> onEvent(WebSocketViewEvent.DirectionFilterToggled(direction)) },
            onMessageClick = { messageId -> onEvent(WebSocketViewEvent.MessageExpandToggled(messageId)) },
            onClearMessages = { onEvent(WebSocketViewEvent.ClearCurrentConnectionMessages) },
            onBack = {
                onEvent(WebSocketViewEvent.ConnectionSelectionCleared)
                navController.popBackStack()
            },
        )
    }
}

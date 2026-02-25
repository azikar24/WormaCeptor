package com.azikar24.wormaceptor.feature.websocket.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.azikar24.wormaceptor.core.engine.WebSocketMonitorEngine
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.websocket.WebSocketFeature
import com.azikar24.wormaceptor.feature.websocket.ui.WebSocketDetailScreen
import com.azikar24.wormaceptor.feature.websocket.ui.WebSocketListScreen
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

    val connections by viewModel.connections.collectAsState()
    val connectionSearchQuery by viewModel.connectionSearchQuery.collectAsState()
    val totalConnectionCount by viewModel.totalConnectionCount.collectAsState()

    WebSocketListScreen(
        connections = connections,
        searchQuery = connectionSearchQuery,
        totalCount = totalConnectionCount,
        onSearchQueryChanged = viewModel::onConnectionSearchQueryChanged,
        onConnectionClick = { connection ->
            viewModel.selectConnection(connection.id)
            navController.navigate(WormaCeptorNavKeys.WebSocketMessages.route)
        },
        onClearAll = viewModel::clearAll,
        getMessageCount = viewModel::getMessageCountForConnection,
        onBack = onNavigateBack,
    )
}

@Composable
private fun WebSocketMessagesDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController)

    val selectedConnection by viewModel.selectedConnection.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val messageSearchQuery by viewModel.messageSearchQuery.collectAsState()
    val directionFilter by viewModel.directionFilter.collectAsState()
    val totalMessageCount by viewModel.totalMessageCount.collectAsState()
    val directionCounts by viewModel.directionCounts.collectAsState()
    val expandedMessageId by viewModel.expandedMessageId.collectAsState()

    WebSocketDetailScreen(
        connection = selectedConnection,
        messages = messages,
        searchQuery = messageSearchQuery,
        directionFilter = directionFilter,
        totalMessageCount = totalMessageCount,
        directionCounts = directionCounts,
        expandedMessageId = expandedMessageId,
        onSearchQueryChanged = viewModel::onMessageSearchQueryChanged,
        onDirectionFilterToggle = viewModel::toggleDirectionFilter,
        onMessageClick = viewModel::toggleMessageExpanded,
        onClearMessages = viewModel::clearCurrentConnectionMessages,
        onBack = {
            viewModel.clearConnectionSelection()
            navController.popBackStack()
        },
    )
}

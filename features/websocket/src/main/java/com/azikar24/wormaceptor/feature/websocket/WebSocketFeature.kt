package com.azikar24.wormaceptor.feature.websocket

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.core.engine.WebSocketMonitorEngine
import com.azikar24.wormaceptor.feature.websocket.ui.WebSocketDetailScreen
import com.azikar24.wormaceptor.feature.websocket.ui.WebSocketListScreen
import com.azikar24.wormaceptor.feature.websocket.vm.WebSocketViewModel

/**
 * Entry point for the WebSocket Monitoring feature.
 * Provides factory methods and composable navigation host.
 *
 * Usage:
 * ```kotlin
 * // Create the engine (typically a singleton)
 * val engine = WebSocketMonitorEngine()
 *
 * // In your composable
 * WebSocketMonitor(
 *     engine = engine,
 *     onNavigateBack = { navController.popBackStack() },
 * )
 *
 * // When creating WebSocket connections, wrap the listener
 * val listener = engine.wrap(yourWebSocketListener, url)
 * okHttpClient.newWebSocket(request, listener)
 * ```
 */
object WebSocketFeature {

    /**
     * Creates a WebSocketMonitorEngine instance.
     * Use this in your dependency injection setup or as a singleton.
     *
     * @param maxMessages Maximum number of messages to store (default 500)
     */
    fun createEngine(maxMessages: Int = WebSocketMonitorEngine.DEFAULT_MAX_MESSAGES): WebSocketMonitorEngine {
        return WebSocketMonitorEngine(maxMessages)
    }

    /**
     * Creates a WebSocketViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(engine: WebSocketMonitorEngine): WebSocketViewModelFactory {
        return WebSocketViewModelFactory(engine)
    }
}

/**
 * Factory for creating WebSocketViewModel instances.
 */
class WebSocketViewModelFactory(
    private val engine: WebSocketMonitorEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WebSocketViewModel::class.java)) {
            return WebSocketViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the WebSocket Monitor feature.
 * Handles navigation between connection list and message detail screens.
 *
 * @param engine The WebSocketMonitorEngine instance to use
 * @param modifier Modifier for the root composable
 * @param onNavigateBack Callback when back navigation is requested from the list screen
 */
@Composable
fun WebSocketMonitor(
    engine: WebSocketMonitorEngine,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val factory = remember { WebSocketFeature.createViewModelFactory(engine) }
    val viewModel: WebSocketViewModel = viewModel(factory = factory)
    val navController = rememberNavController()

    // Collect state
    val connections by viewModel.connections.collectAsState()
    val connectionSearchQuery by viewModel.connectionSearchQuery.collectAsState()
    val totalConnectionCount by viewModel.totalConnectionCount.collectAsState()

    val selectedConnection by viewModel.selectedConnection.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val messageSearchQuery by viewModel.messageSearchQuery.collectAsState()
    val directionFilter by viewModel.directionFilter.collectAsState()
    val totalMessageCount by viewModel.totalMessageCount.collectAsState()
    val directionCounts by viewModel.directionCounts.collectAsState()
    val expandedMessageId by viewModel.expandedMessageId.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "connections",
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300),
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300),
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300),
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300),
            ) + fadeOut(animationSpec = tween(300))
        },
    ) {
        composable("connections") {
            WebSocketListScreen(
                connections = connections,
                searchQuery = connectionSearchQuery,
                totalCount = totalConnectionCount,
                onSearchQueryChanged = viewModel::onConnectionSearchQueryChanged,
                onConnectionClick = { connection ->
                    viewModel.selectConnection(connection.id)
                    navController.navigate("messages")
                },
                onClearAll = viewModel::clearAll,
                getMessageCount = viewModel::getMessageCountForConnection,
                onBack = onNavigateBack,
            )
        }

        composable("messages") {
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
    }
}

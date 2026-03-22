package com.azikar24.wormaceptor.feature.websocket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.WebSocketMonitorEngine
import com.azikar24.wormaceptor.feature.websocket.vm.WebSocketViewModel

/**
 * Entry point for the WebSocket Monitoring feature.
 * Provides factory methods for creating engines and ViewModel factories.
 *
 * Navigation is handled by [com.azikar24.wormaceptor.feature.websocket.navigation.webSocketGraph].
 *
 * Usage:
 * ```kotlin
 * // Create the engine (typically a singleton)
 * val engine = WebSocketMonitorEngine()
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

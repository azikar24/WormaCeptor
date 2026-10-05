package com.azikar24.wormaceptor.feature.websocket.vm

import com.azikar24.wormaceptor.domain.entities.WebSocketConnection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessage
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

data class WebSocketViewState(
    val connectionSearchQuery: String = "",
    val connections: ImmutableList<WebSocketConnection> = persistentListOf(),
    val totalConnectionCount: Int = 0,
    /** Number of captured messages per connection id, kept live from the engine's message stream. */
    val messageCountsByConnection: ImmutableMap<Long, Int> = persistentMapOf(),
    val isConnectionsLoading: Boolean = true,
    val selectedConnection: WebSocketConnection? = null,
    val messageSearchQuery: String = "",
    val directionFilter: WebSocketMessageDirection? = null,
    val expandedMessageId: Long? = null,
    val messages: ImmutableList<WebSocketMessage> = persistentListOf(),
    val totalMessageCount: Int = 0,
    val isMessagesLoading: Boolean = false,
    /** Message counts per direction for the selected connection. */
    val directionCounts: ImmutableMap<WebSocketMessageDirection, Int> = persistentMapOf(),
    val showClearAllConfirmation: Boolean = false,
    val showClearMessagesConfirmation: Boolean = false,
)

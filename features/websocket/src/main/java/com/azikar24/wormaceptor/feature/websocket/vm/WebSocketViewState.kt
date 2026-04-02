package com.azikar24.wormaceptor.feature.websocket.vm

import com.azikar24.wormaceptor.domain.entities.WebSocketConnection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessage
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Consolidated UI state for the WebSocket Monitoring feature.
 *
 * @property connectionSearchQuery Current search filter for the connection list.
 * @property connections Filtered connections based on search query.
 * @property totalConnectionCount Total number of connections (unfiltered).
 * @property selectedConnection The currently selected connection, or null.
 * @property messageSearchQuery Current search filter for messages.
 * @property directionFilter Active message direction filter, or null to show all.
 * @property expandedMessageId ID of the message whose payload is expanded, or null.
 * @property messages Filtered messages for the selected connection.
 * @property totalMessageCount Total message count for the selected connection (unfiltered).
 * @property directionCounts Message counts by direction for the selected connection.
 */
data class WebSocketViewState(
    val connectionSearchQuery: String = "",
    val connections: ImmutableList<WebSocketConnection> = persistentListOf(),
    val totalConnectionCount: Int = 0,
    val selectedConnection: WebSocketConnection? = null,
    val messageSearchQuery: String = "",
    val directionFilter: WebSocketMessageDirection? = null,
    val expandedMessageId: Long? = null,
    val messages: ImmutableList<WebSocketMessage> = persistentListOf(),
    val totalMessageCount: Int = 0,
    val directionCounts: Map<WebSocketMessageDirection, Int> = emptyMap(),
)

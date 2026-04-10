package com.azikar24.wormaceptor.feature.websocket.vm

import com.azikar24.wormaceptor.domain.entities.WebSocketConnection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessage
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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
    val showClearAllConfirmation: Boolean = false,
    val showClearMessagesConfirmation: Boolean = false,
)

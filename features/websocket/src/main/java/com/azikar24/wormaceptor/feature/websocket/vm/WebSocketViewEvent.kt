package com.azikar24.wormaceptor.feature.websocket.vm

import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection

sealed class WebSocketViewEvent {
    data class ConnectionSearchQueryChanged(val query: String) : WebSocketViewEvent()
    data class ConnectionSelected(val connectionId: Long) : WebSocketViewEvent()
    data object ConnectionSelectionCleared : WebSocketViewEvent()
    data class MessageSearchQueryChanged(val query: String) : WebSocketViewEvent()
    data class DirectionFilterToggled(val direction: WebSocketMessageDirection) : WebSocketViewEvent()
    data class MessageExpandToggled(val messageId: Long) : WebSocketViewEvent()
    data object ClearAllRequested : WebSocketViewEvent()
    data object ClearAllConfirmed : WebSocketViewEvent()
    data object ClearAllDismissed : WebSocketViewEvent()
    data object ClearMessagesRequested : WebSocketViewEvent()
    data object ClearMessagesConfirmed : WebSocketViewEvent()
    data object ClearMessagesDismissed : WebSocketViewEvent()
}

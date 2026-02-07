package com.azikar24.wormaceptor.feature.websocket.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageType
import com.azikar24.wormaceptor.domain.entities.WebSocketState

/**
 * WebSocket-specific colors for consistent visual representation.
 * Uses centralized colors from WormaCeptorColors.WebSocket.
 */
@Immutable
data class WebSocketColors(
    val connecting: Color,
    val open: Color,
    val closing: Color,
    val closed: Color,
    val sent: Color,
    val received: Color,
    val textMessage: Color,
    val binaryMessage: Color,
    val pingPong: Color,
    val sentBackground: Color,
    val receivedBackground: Color,
    val connectingBackground: Color,
    val openBackground: Color,
    val closingBackground: Color,
    val closedBackground: Color,
) {
    fun forState(state: WebSocketState): Color = when (state) {
        WebSocketState.CONNECTING -> connecting
        WebSocketState.OPEN -> open
        WebSocketState.CLOSING -> closing
        WebSocketState.CLOSED -> closed
    }

    fun backgroundForState(state: WebSocketState): Color = when (state) {
        WebSocketState.CONNECTING -> connectingBackground
        WebSocketState.OPEN -> openBackground
        WebSocketState.CLOSING -> closingBackground
        WebSocketState.CLOSED -> closedBackground
    }

    fun forDirection(direction: WebSocketMessageDirection): Color = when (direction) {
        WebSocketMessageDirection.SENT -> sent
        WebSocketMessageDirection.RECEIVED -> received
    }

    fun backgroundForDirection(direction: WebSocketMessageDirection): Color = when (direction) {
        WebSocketMessageDirection.SENT -> sentBackground
        WebSocketMessageDirection.RECEIVED -> receivedBackground
    }

    fun forMessageType(type: WebSocketMessageType): Color = when (type) {
        WebSocketMessageType.TEXT -> textMessage
        WebSocketMessageType.BINARY -> binaryMessage
        WebSocketMessageType.PING -> pingPong
        WebSocketMessageType.PONG -> pingPong
    }
}

/**
 * Returns the appropriate WebSocket colors based on the current theme.
 */
@Composable
fun webSocketColors(darkTheme: Boolean = isSystemInDarkTheme()): WebSocketColors {
    val alpha = WormaCeptorDesignSystem.Alpha

    return if (darkTheme) {
        WebSocketColors(
            connecting = WormaCeptorColors.WebSocket.Connecting,
            open = WormaCeptorColors.WebSocket.Open,
            closing = WormaCeptorColors.WebSocket.Closing,
            closed = WormaCeptorColors.WebSocket.Closed,
            sent = WormaCeptorColors.WebSocket.Sent,
            received = WormaCeptorColors.WebSocket.Received,
            textMessage = Color(0xFFE0E0E0),
            binaryMessage = WormaCeptorColors.WebSocket.BinaryMessage,
            pingPong = WormaCeptorColors.WebSocket.PingPong,
            sentBackground = WormaCeptorColors.WebSocket.Sent.copy(alpha = alpha.medium),
            receivedBackground = WormaCeptorColors.WebSocket.Received.copy(alpha = alpha.medium),
            connectingBackground = WormaCeptorColors.WebSocket.Connecting.copy(alpha = alpha.medium),
            openBackground = WormaCeptorColors.WebSocket.Open.copy(alpha = alpha.medium),
            closingBackground = WormaCeptorColors.WebSocket.Closing.copy(alpha = alpha.medium),
            closedBackground = Color(0xFF212121),
        )
    } else {
        WebSocketColors(
            connecting = WormaCeptorColors.WebSocket.Connecting,
            open = WormaCeptorColors.WebSocket.Open,
            closing = WormaCeptorColors.WebSocket.Closing,
            closed = WormaCeptorColors.WebSocket.Closed,
            sent = WormaCeptorColors.WebSocket.Sent,
            received = WormaCeptorColors.WebSocket.Received,
            textMessage = WormaCeptorColors.WebSocket.TextMessage,
            binaryMessage = WormaCeptorColors.WebSocket.BinaryMessage,
            pingPong = WormaCeptorColors.WebSocket.PingPong,
            sentBackground = WormaCeptorColors.WebSocket.Sent.copy(alpha = alpha.subtle),
            receivedBackground = WormaCeptorColors.WebSocket.Received.copy(alpha = alpha.subtle),
            connectingBackground = WormaCeptorColors.WebSocket.Connecting.copy(alpha = alpha.subtle),
            openBackground = WormaCeptorColors.WebSocket.Open.copy(alpha = alpha.subtle),
            closingBackground = WormaCeptorColors.WebSocket.Closing.copy(alpha = alpha.subtle),
            closedBackground = Color(0xFFF5F5F5),
        )
    }
}

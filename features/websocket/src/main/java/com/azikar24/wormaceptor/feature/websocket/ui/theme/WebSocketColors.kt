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
 *
 * @property connecting Foreground color for the CONNECTING state.
 * @property open Foreground color for the OPEN state.
 * @property closing Foreground color for the CLOSING state.
 * @property closed Foreground color for the CLOSED state.
 * @property sent Foreground color for sent messages.
 * @property received Foreground color for received messages.
 * @property textMessage Color for text-type message indicators.
 * @property binaryMessage Color for binary-type message indicators.
 * @property pingPong Color for ping/pong control frame indicators.
 * @property sentBackground Background color for sent message rows.
 * @property receivedBackground Background color for received message rows.
 * @property connectingBackground Background color for CONNECTING state badges.
 * @property openBackground Background color for OPEN state badges.
 * @property closingBackground Background color for CLOSING state badges.
 * @property closedBackground Background color for CLOSED state badges.
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
    /** Returns the foreground color for the given connection state. */
    fun forState(state: WebSocketState): Color = when (state) {
        WebSocketState.CONNECTING -> connecting
        WebSocketState.OPEN -> open
        WebSocketState.CLOSING -> closing
        WebSocketState.CLOSED -> closed
    }

    /** Returns the background color for the given connection state. */
    fun backgroundForState(state: WebSocketState): Color = when (state) {
        WebSocketState.CONNECTING -> connectingBackground
        WebSocketState.OPEN -> openBackground
        WebSocketState.CLOSING -> closingBackground
        WebSocketState.CLOSED -> closedBackground
    }

    /** Returns the foreground color for the given message direction (sent or received). */
    fun forDirection(direction: WebSocketMessageDirection): Color = when (direction) {
        WebSocketMessageDirection.SENT -> sent
        WebSocketMessageDirection.RECEIVED -> received
    }

    /** Returns the background color for the given message direction. */
    fun backgroundForDirection(direction: WebSocketMessageDirection): Color = when (direction) {
        WebSocketMessageDirection.SENT -> sentBackground
        WebSocketMessageDirection.RECEIVED -> receivedBackground
    }

    /** Returns the color associated with the given message type (text, binary, ping/pong). */
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
            sentBackground = WormaCeptorColors.WebSocket.Sent.copy(alpha = alpha.MEDIUM),
            receivedBackground = WormaCeptorColors.WebSocket.Received.copy(alpha = alpha.MEDIUM),
            connectingBackground = WormaCeptorColors.WebSocket.Connecting.copy(alpha = alpha.MEDIUM),
            openBackground = WormaCeptorColors.WebSocket.Open.copy(alpha = alpha.MEDIUM),
            closingBackground = WormaCeptorColors.WebSocket.Closing.copy(alpha = alpha.MEDIUM),
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
            sentBackground = WormaCeptorColors.WebSocket.Sent.copy(alpha = alpha.SUBTLE),
            receivedBackground = WormaCeptorColors.WebSocket.Received.copy(alpha = alpha.SUBTLE),
            connectingBackground = WormaCeptorColors.WebSocket.Connecting.copy(alpha = alpha.SUBTLE),
            openBackground = WormaCeptorColors.WebSocket.Open.copy(alpha = alpha.SUBTLE),
            closingBackground = WormaCeptorColors.WebSocket.Closing.copy(alpha = alpha.SUBTLE),
            closedBackground = Color(0xFFF5F5F5),
        )
    }
}

package com.azikar24.wormaceptor.feature.websocket.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.badge.WormaCeptorStatusBadge
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAlpha
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.WebSocketMessage
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageType
import com.azikar24.wormaceptor.feature.websocket.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val PayloadPreviewChars = 150
private const val PayloadCollapsedLines = 3

@Composable
internal fun MessageList(
    messages: ImmutableList<WebSocketMessage>,
    expandedMessageId: Long?,
    onMessageClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            top = WormaCeptorTokens.Spacing.xs,
            bottom = WormaCeptorTokens.Spacing.sm +
                WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding(),
        ),
    ) {
        items(
            items = messages,
            key = { it.id },
        ) { message ->
            MessageItem(
                message = message,
                isExpanded = message.id == expandedMessageId,
                onClick = { onMessageClick(message.id) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MessageItem(
    message: WebSocketMessage,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = messagePalette(message)
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }
    val formattedTime = remember(message.timestamp) {
        timeFormat.format(Date(message.timestamp))
    }

    WormaCeptorCard(
        modifier = modifier
            .padding(
                horizontal = WormaCeptorTokens.Spacing.sm,
                vertical = WormaCeptorTokens.Spacing.xxs,
            )
            .animateContentSize(),
        onClick = onClick,
        style = CardStyle.Outlined,
        backgroundColor = palette.direction.copy(alpha = TokenAlpha.SUBTLE),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorTokens.Spacing.md,
                    vertical = WormaCeptorTokens.Spacing.sm,
                ),
        ) {
            MessageHeader(
                message = message,
                palette = palette,
                formattedTime = formattedTime,
                isExpanded = isExpanded,
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

            Text(
                text = if (isExpanded) message.payload else message.payloadPreview(PayloadPreviewChars),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = WormaCeptorTokens.Typography.codeMedium.lineHeight,
                maxLines = if (isExpanded) Int.MAX_VALUE else PayloadCollapsedLines,
                overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MessageHeader(
    message: WebSocketMessage,
    palette: MessagePalette,
    formattedTime: String,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DirectionIndicator(
            direction = message.direction,
            color = palette.direction,
        )

        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

        WormaCeptorStatusBadge(
            text = message.type.name,
            containerColor = palette.type.copy(alpha = WormaCeptorTokens.Alpha.SOFT),
            contentColor = palette.type,
        )

        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

        Text(
            text = formatBytes(message.size),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = WormaCeptorTokens.Alpha.HEAVY,
            ),
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = formattedTime,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = WormaCeptorTokens.Alpha.HEAVY,
            ),
        )

        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.xs))

        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = stringResource(
                if (isExpanded) R.string.websocket_collapse else R.string.websocket_expand,
            ),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = WormaCeptorTokens.Alpha.BOLD,
            ),
            modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
        )
    }
}

@Composable
private fun DirectionIndicator(
    direction: WebSocketMessageDirection,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val icon: ImageVector = when (direction) {
        WebSocketMessageDirection.SENT -> Icons.AutoMirrored.Filled.CallMade
        WebSocketMessageDirection.RECEIVED -> Icons.AutoMirrored.Filled.CallReceived
    }
    val label = stringResource(
        when (direction) {
            WebSocketMessageDirection.SENT -> R.string.websocket_direction_sent
            WebSocketMessageDirection.RECEIVED -> R.string.websocket_direction_received
        },
    )
    Row(
        modifier = modifier.padding(end = WormaCeptorTokens.Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = direction.name,
            tint = color,
            modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
        )
        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.xxs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

private data class MessagePalette(val direction: Color, val type: Color)

@Composable
private fun messagePalette(message: WebSocketMessage): MessagePalette {
    val ws = WormaCeptorTokens.Colors.WebSocket
    val direction = when (message.direction) {
        WebSocketMessageDirection.SENT -> ws.sent
        WebSocketMessageDirection.RECEIVED -> ws.received
    }
    val type = when (message.type) {
        WebSocketMessageType.TEXT -> ws.textMessage
        WebSocketMessageType.BINARY -> ws.binaryMessage
        WebSocketMessageType.PING -> ws.pingPong
        WebSocketMessageType.PONG -> ws.pingPong
    }
    return MessagePalette(direction = direction, type = type)
}

@Preview(showBackground = true)
@Composable
private fun MessageListPreview() {
    WormaCeptorTheme {
        MessageList(
            messages = persistentListOf(
                WebSocketMessage(
                    id = 1L,
                    connectionId = 1L,
                    type = WebSocketMessageType.TEXT,
                    direction = WebSocketMessageDirection.SENT,
                    payload = "{\"type\":\"ping\",\"timestamp\":1234567890}",
                    timestamp = System.currentTimeMillis() - 5_000L,
                    size = 42L,
                ),
                WebSocketMessage(
                    id = 2L,
                    connectionId = 1L,
                    type = WebSocketMessageType.TEXT,
                    direction = WebSocketMessageDirection.RECEIVED,
                    payload = "{\"type\":\"pong\",\"timestamp\":1234567891}",
                    timestamp = System.currentTimeMillis() - 4_000L,
                    size = 43L,
                ),
                WebSocketMessage(
                    id = 3L,
                    connectionId = 1L,
                    type = WebSocketMessageType.BINARY,
                    direction = WebSocketMessageDirection.SENT,
                    payload = "0x48656C6C6F20576F726C64",
                    timestamp = System.currentTimeMillis() - 3_000L,
                    size = 11L,
                ),
            ),
            expandedMessageId = 2L,
            onMessageClick = {},
        )
    }
}

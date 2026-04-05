package com.azikar24.wormaceptor.feature.websocket.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.WebSocketMessage
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageType
import com.azikar24.wormaceptor.feature.websocket.R
import kotlinx.collections.immutable.ImmutableList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            top = WormaCeptorTokens.Spacing.sm,
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
internal fun MessageItem(
    message: WebSocketMessage,
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ws = WormaCeptorTokens.Colors.WebSocket
    val directionColor = when (message.direction) {
        WebSocketMessageDirection.SENT -> ws.sent
        WebSocketMessageDirection.RECEIVED -> ws.received
    }
    val backgroundColor = when (message.direction) {
        WebSocketMessageDirection.SENT -> ws.sent.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE)
        WebSocketMessageDirection.RECEIVED -> ws.received.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE)
    }
    val typeColor = when (message.type) {
        WebSocketMessageType.TEXT -> ws.textMessage
        WebSocketMessageType.BINARY -> ws.binaryMessage
        WebSocketMessageType.PING -> ws.pingPong
        WebSocketMessageType.PONG -> ws.pingPong
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }
    val formattedTime = remember(message.timestamp) {
        timeFormat.format(Date(message.timestamp))
    }

    val directionIcon = when (message.direction) {
        WebSocketMessageDirection.SENT -> Icons.AutoMirrored.Filled.CallMade
        WebSocketMessageDirection.RECEIVED -> Icons.AutoMirrored.Filled.CallReceived
    }

    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .animateContentSize(),
        color = backgroundColor.copy(
            alpha = WormaCeptorTokens.Alpha.MODERATE,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorTokens.Spacing.md,
                    vertical = WormaCeptorTokens.Spacing.sm,
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Direction icon
                Icon(
                    imageVector = directionIcon,
                    contentDescription = message.direction.name,
                    tint = directionColor,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                )

                Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

                // Type badge
                Surface(
                    shape = RoundedCornerShape(WormaCeptorTokens.Radius.xs),
                    color = typeColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
                ) {
                    Text(
                        text = message.type.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = typeColor,
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorTokens.Spacing.xs,
                            vertical = WormaCeptorTokens.Spacing.xxs,
                        ),
                    )
                }

                Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

                // Size
                Text(
                    text = formatBytes(message.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.weight(1f))

                // Timestamp
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = WormaCeptorTokens.Alpha.HEAVY,
                    ),
                )

                Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.xs))

                // Expand indicator
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

            Spacer(
                modifier = Modifier.height(
                    WormaCeptorTokens.Spacing.xs,
                ),
            )

            // Payload preview or full content
            Text(
                text = if (isExpanded) message.payload else message.payloadPreview(150),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = WormaCeptorTokens.Typography.codeMedium.lineHeight,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis,
            )
        }
    }
}

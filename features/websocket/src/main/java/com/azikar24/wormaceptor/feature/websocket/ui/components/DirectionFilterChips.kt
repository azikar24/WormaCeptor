package com.azikar24.wormaceptor.feature.websocket.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection

@Composable
internal fun DirectionFilterChips(
    selectedDirection: WebSocketMessageDirection?,
    directionCounts: Map<WebSocketMessageDirection, Int>,
    onDirectionToggle: (WebSocketMessageDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ws = WormaCeptorTokens.Colors.WebSocket
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = WormaCeptorTokens.Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        WebSocketMessageDirection.entries.forEach { direction ->
            val isSelected = selectedDirection == direction
            val count = directionCounts[direction] ?: 0
            val directionColor = when (direction) {
                WebSocketMessageDirection.SENT -> ws.sent
                WebSocketMessageDirection.RECEIVED -> ws.received
            }
            val icon = when (direction) {
                WebSocketMessageDirection.SENT -> Icons.AutoMirrored.Filled.CallMade
                WebSocketMessageDirection.RECEIVED -> Icons.AutoMirrored.Filled.CallReceived
            }

            FilterChip(
                selected = isSelected,
                onClick = { onDirectionToggle(direction) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(WormaCeptorTokens.Spacing.lg),
                        )
                        Text(
                            text = direction.name,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        if (count > 0) {
                            Text(
                                text = if (count > 999) "999+" else count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) {
                                    val alpha = WormaCeptorTokens.Alpha.INTENSE +
                                        WormaCeptorTokens.Alpha.SUBTLE
                                    WormaCeptorTokens.semantic().accent.copy(
                                        alpha = alpha,
                                    )
                                } else {
                                    val alpha = WormaCeptorTokens.Alpha.INTENSE +
                                        WormaCeptorTokens.Alpha.SUBTLE
                                    WormaCeptorTokens.semantic().textSecondary.copy(
                                        alpha = alpha,
                                    )
                                },
                            )
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = directionColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
                    selectedLabelColor = directionColor,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = directionColor.copy(
                        alpha = WormaCeptorTokens.Alpha.MODERATE,
                    ),
                    selectedBorderColor = directionColor.copy(
                        alpha = WormaCeptorTokens.Alpha.BOLD,
                    ),
                    enabled = true,
                    selected = isSelected,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DirectionFilterChipsNoneSelectedPreview() {
    WormaCeptorTheme {
        DirectionFilterChips(
            selectedDirection = null,
            directionCounts = mapOf(
                WebSocketMessageDirection.SENT to 12,
                WebSocketMessageDirection.RECEIVED to 8,
            ),
            onDirectionToggle = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DirectionFilterChipsSentSelectedPreview() {
    WormaCeptorTheme {
        DirectionFilterChips(
            selectedDirection = WebSocketMessageDirection.SENT,
            directionCounts = mapOf(
                WebSocketMessageDirection.SENT to 12,
                WebSocketMessageDirection.RECEIVED to 8,
            ),
            onDirectionToggle = {},
        )
    }
}

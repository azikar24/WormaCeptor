package com.azikar24.wormaceptor.feature.pushtoken.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatDateShort
import com.azikar24.wormaceptor.domain.entities.TokenHistory

private const val TokenPreviewLength = 20

/** Displays a single token history entry with event icon, truncated token, and timestamp. */
@Composable
fun PushTokenHistoryItem(
    entry: TokenHistory,
    modifier: Modifier = Modifier,
) {
    val (icon, color) = when (entry.event) {
        TokenHistory.TokenEvent.CREATED ->
            Icons.Default.Add to WormaCeptorTokens.Colors.Status.green

        TokenHistory.TokenEvent.REFRESHED ->
            Icons.Default.Autorenew to WormaCeptorTokens.Colors.Status.blue

        TokenHistory.TokenEvent.INVALIDATED ->
            Icons.Default.Warning to WormaCeptorTokens.Colors.Status.amber

        TokenHistory.TokenEvent.DELETED ->
            Icons.Default.Delete to WormaCeptorTokens.Colors.Status.red
    }

    WormaCeptorCard(
        modifier = modifier,
        style = CardStyle.Outlined,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(WormaCeptorTokens.IconSize.xl)
                    .clip(WormaCeptorTokens.Shapes.card)
                    .background(color.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)),
                Alignment.Center,
            ) {
                Icon(
                    icon,
                    null,
                    tint = color,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                )
            }
            Spacer(Modifier.width(WormaCeptorTokens.Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    entry.event.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    entry.token.take(TokenPreviewLength) + "...",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = WormaCeptorTokens.semantic().textSecondary,
                )
            }
            Text(
                formatDateShort(entry.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = WormaCeptorTokens.semantic().textSecondary.copy(
                    alpha = WormaCeptorTokens.Alpha.HEAVY,
                ),
            )
        }
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(showBackground = true)
@Composable
private fun HistoryItemCreatedPreview() {
    WormaCeptorTheme {
        PushTokenHistoryItem(
            entry = TokenHistory(
                token = "dGhpcyBpcyBhIHNhbXBsZSBGQ00gcHVzaCB0b2tlbg==:APA91bHPRgkT",
                timestamp = 1_712_000_000_000L,
                event = TokenHistory.TokenEvent.CREATED,
            ),
        )
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(showBackground = true)
@Composable
private fun HistoryItemRefreshedPreview() {
    WormaCeptorTheme {
        PushTokenHistoryItem(
            entry = TokenHistory(
                token = "dGhpcyBpcyBhIHNhbXBsZSBGQ00gcHVzaCB0b2tlbg==:APA91bHPRgkT",
                timestamp = 1_712_300_000_000L,
                event = TokenHistory.TokenEvent.REFRESHED,
            ),
        )
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(showBackground = true)
@Composable
private fun HistoryItemDeletedPreview() {
    WormaCeptorTheme {
        PushTokenHistoryItem(
            entry = TokenHistory(
                token = "dGhpcyBpcyBhIHNhbXBsZSBGQ00gcHVzaCB0b2tlbg==:APA91bHPRgkT",
                timestamp = 1_712_600_000_000L,
                event = TokenHistory.TokenEvent.DELETED,
            ),
        )
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(showBackground = true)
@Composable
private fun HistoryItemInvalidatedPreview() {
    WormaCeptorTheme {
        PushTokenHistoryItem(
            entry = TokenHistory(
                token = "dGhpcyBpcyBhIHNhbXBsZSBGQ00gcHVzaCB0b2tlbg==:APA91bHPRgkT",
                timestamp = 1_712_400_000_000L,
                event = TokenHistory.TokenEvent.INVALIDATED,
            ),
        )
    }
}

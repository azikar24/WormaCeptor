package com.azikar24.wormaceptor.feature.pushtoken.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatDateShort
import com.azikar24.wormaceptor.domain.entities.PushTokenInfo
import com.azikar24.wormaceptor.feature.pushtoken.R
import com.azikar24.wormaceptor.feature.pushtoken.vm.PushTokenViewEvent

/** Displays the current push token with copy/delete actions, or an empty state. */
@Composable
fun PushTokenCurrentTokenCard(
    token: PushTokenInfo?,
    isLoading: Boolean,
    onEvent: (PushTokenViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    WormaCeptorCard(
        shape = WormaCeptorTokens.Shapes.cardLarge,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
            Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Text(
                stringResource(R.string.pushtoken_current_token),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (token != null) {
                TokenValueSurface(tokenValue = token.token)
                TokenProviderRow(token = token)
                TokenActionButtons(isLoading = isLoading, onEvent = onEvent)
            } else {
                TokenEmptyState(isLoading = isLoading)
            }
        }
    }
}

@Composable
private fun TokenValueSurface(tokenValue: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = WormaCeptorTokens.Shapes.card,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Text(
            tokenValue,
            Modifier.padding(WormaCeptorTokens.Spacing.md),
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TokenProviderRow(token: PushTokenInfo) {
    Row(
        Modifier.fillMaxWidth(),
        Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        Surface(
            shape = WormaCeptorTokens.Shapes.chip,
            color = WormaCeptorTokens.Colors.Status.green.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
        ) {
            Text(
                token.provider.name,
                Modifier.padding(
                    horizontal = WormaCeptorTokens.Spacing.sm,
                    vertical = WormaCeptorTokens.Spacing.xs,
                ),
                color = WormaCeptorTokens.Colors.Status.green,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Text(
            stringResource(R.string.pushtoken_refreshed, formatDateShort(token.lastRefreshed)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TokenActionButtons(
    isLoading: Boolean,
    onEvent: (PushTokenViewEvent) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        WormaCeptorButton(
            text = stringResource(R.string.pushtoken_copy),
            onClick = { onEvent(PushTokenViewEvent.CopyToken) },
            modifier = Modifier.weight(1f),
            variant = ButtonVariant.Primary,
            leadingIcon = {
                Icon(Icons.Default.ContentCopy, null, Modifier.size(WormaCeptorTokens.IconSize.sm))
            },
        )
        WormaCeptorButton(
            text = stringResource(R.string.pushtoken_delete),
            onClick = { onEvent(PushTokenViewEvent.DeleteToken) },
            variant = ButtonVariant.Outlined,
            enabled = !isLoading,
            containerColor = WormaCeptorTokens.Colors.Status.red,
            leadingIcon = {
                Icon(Icons.Default.Delete, null, Modifier.size(WormaCeptorTokens.IconSize.sm))
            },
        )
    }
}

@Composable
private fun TokenEmptyState(isLoading: Boolean) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(80.dp),
        Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.NotificationsOff,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.xl),
                )
                Text(
                    stringResource(R.string.pushtoken_no_token),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(showBackground = true)
@Composable
private fun CurrentTokenCardWithTokenPreview() {
    WormaCeptorTheme {
        PushTokenCurrentTokenCard(
            token = PushTokenInfo(
                token = "dGhpcyBpcyBhIHNhbXBsZSBGQ00gcHVzaCB0b2tlbg==:APA91bHPRgkT",
                provider = PushTokenInfo.PushProvider.FCM,
                createdAt = 1_712_000_000_000L,
                lastRefreshed = 1_712_300_000_000L,
                isValid = true,
                associatedUserId = null,
                metadata = emptyMap(),
            ),
            isLoading = false,
            onEvent = {},
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun CurrentTokenCardEmptyPreview() {
    WormaCeptorTheme {
        PushTokenCurrentTokenCard(
            token = null,
            isLoading = false,
            onEvent = {},
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun CurrentTokenCardLoadingPreview() {
    WormaCeptorTheme {
        PushTokenCurrentTokenCard(
            token = null,
            isLoading = true,
            onEvent = {},
        )
    }
}

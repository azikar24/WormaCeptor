package com.azikar24.wormaceptor.feature.webviewmonitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorMethodBadge
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAlpha
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatDuration
import com.azikar24.wormaceptor.core.ui.util.formatTimestamp
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import com.azikar24.wormaceptor.feature.webviewmonitor.ui.getStatusColor

@Composable
internal fun WebViewRequestItem(
    request: WebViewRequest,
    onClick: () -> Unit,
) {
    val statusColor = getStatusColor(request)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = WormaCeptorTokens.Spacing.sm,
                vertical = WormaCeptorTokens.Spacing.xs,
            )
            .clip(WormaCeptorTokens.Shapes.card)
            .border(
                width = WormaCeptorTokens.BorderWidth.regular,
                color = MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = WormaCeptorTokens.Alpha.MEDIUM,
                ),
                shape = WormaCeptorTokens.Shapes.card,
            )
            .background(
                color = statusColor.copy(alpha = TokenAlpha.SUBTLE),
                shape = WormaCeptorTokens.Shapes.card,
            )
            .clickable(onClick = onClick)
            .padding(WormaCeptorTokens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RequestItemContent(request, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.md))
        RequestStatusBadge(request, statusColor)
    }
}

@Composable
private fun RequestItemContent(
    request: WebViewRequest,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            WormaCeptorMethodBadge(request.method)
            Text(
                text = request.path,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            RequestHostChip(request.host)
            Text(
                text = request.resourceType.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))
        RequestMetaRow(request)
    }
}

@Composable
private fun RequestHostChip(host: String) {
    Text(
        text = host,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = WormaCeptorTokens.Alpha.PROMINENT,
                ),
                RoundedCornerShape(WormaCeptorTokens.Radius.pill),
            )
            .padding(
                horizontal = WormaCeptorTokens.Spacing.sm,
                vertical = WormaCeptorTokens.Spacing.xxs,
            ),
    )
}

@Composable
private fun RequestStatusBadge(
    request: WebViewRequest,
    statusColor: Color,
) {
    Column(horizontalAlignment = Alignment.End) {
        val statusText = when {
            request.isPending -> "..."
            request.statusCode != null -> request.statusCode.toString()
            request.isFailed -> "ERR"
            else -> "?"
        }
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = statusColor,
            modifier = Modifier
                .background(
                    statusColor.copy(alpha = TokenAlpha.SUBTLE),
                    RoundedCornerShape(WormaCeptorTokens.Radius.xs),
                )
                .padding(
                    horizontal = WormaCeptorTokens.Spacing.sm,
                    vertical = WormaCeptorTokens.Spacing.xxs,
                ),
        )
        request.duration?.let { duration ->
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xxs))
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = WormaCeptorTokens.Alpha.HEAVY,
                ),
            )
        }
    }
}

@Composable
private fun RequestMetaRow(request: WebViewRequest) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatTimestamp(request.timestamp),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = WormaCeptorTokens.Alpha.HEAVY,
            ),
        )
        request.contentLength?.let { length ->
            Text(
                text = formatBytes(length),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = WormaCeptorTokens.Alpha.HEAVY,
                ),
            )
        }
    }
}

package com.azikar24.wormaceptor.feature.leakdetection.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatTimestamp
import com.azikar24.wormaceptor.domain.entities.LeakInfo

@Composable
internal fun LeakCard(
    leak: LeakInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val severityColor = when (leak.severity) {
        LeakInfo.LeakSeverity.CRITICAL -> WormaCeptorTokens.Colors.LeakDetection.critical
        LeakInfo.LeakSeverity.HIGH -> WormaCeptorTokens.Colors.LeakDetection.high
        LeakInfo.LeakSeverity.MEDIUM -> WormaCeptorTokens.Colors.LeakDetection.medium
        LeakInfo.LeakSeverity.LOW -> WormaCeptorTokens.Colors.LeakDetection.low
    }
    val severityBackground = when (leak.severity) {
        LeakInfo.LeakSeverity.CRITICAL -> WormaCeptorTokens.Colors.LeakDetection.critical.copy(
            alpha = WormaCeptorTokens.Alpha.SUBTLE,
        )
        LeakInfo.LeakSeverity.HIGH -> WormaCeptorTokens.Colors.LeakDetection.high.copy(
            alpha = WormaCeptorTokens.Alpha.SUBTLE,
        )
        LeakInfo.LeakSeverity.MEDIUM -> WormaCeptorTokens.Colors.LeakDetection.medium.copy(
            alpha = WormaCeptorTokens.Alpha.SUBTLE,
        )
        LeakInfo.LeakSeverity.LOW -> WormaCeptorTokens.Colors.LeakDetection.low.copy(
            alpha = WormaCeptorTokens.Alpha.SUBTLE,
        )
    }

    WormaCeptorCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = WormaCeptorTokens.Shapes.cardLarge,
        backgroundColor = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Severity indicator
            Box(
                modifier = Modifier
                    .size(WormaCeptorTokens.TouchTarget.minimum)
                    .clip(RoundedCornerShape(WormaCeptorTokens.Radius.md))
                    .background(severityBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = leak.severity.name,
                    tint = severityColor,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                )
            }

            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = leak.objectClass.substringAfterLast('.'),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = leak.leakDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatTimestamp(leak.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatBytes(leak.retainedSize),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = severityColor,
                    )
                }
            }

            // Severity badge
            Surface(
                shape = RoundedCornerShape(WormaCeptorTokens.Radius.xs),
                color = severityBackground,
            ) {
                Text(
                    text = leak.severity.name.take(1),
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorTokens.Spacing.sm,
                        vertical = WormaCeptorTokens.Spacing.xs,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = severityColor,
                )
            }
        }
    }
}

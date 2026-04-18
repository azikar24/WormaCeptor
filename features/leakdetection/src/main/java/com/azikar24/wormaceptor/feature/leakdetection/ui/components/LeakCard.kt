package com.azikar24.wormaceptor.feature.leakdetection.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.badge.WormaCeptorStatusBadge
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatTimestamp
import com.azikar24.wormaceptor.domain.entities.LeakInfo

@Composable
internal fun LeakCard(
    leak: LeakInfo,
    onClick: () -> Unit,
) {
    val severityColor = severityColor(leak.severity)
    val severityBackground = severityColor.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE)

    WormaCeptorCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = WormaCeptorTokens.Shapes.cardLarge,
        backgroundColor = WormaCeptorTokens.semantic().surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            SeverityIndicator(
                severityName = leak.severity.name,
                severityColor = severityColor,
                severityBackground = severityBackground,
            )

            LeakCardDetails(
                leak = leak,
                severityColor = severityColor,
                modifier = Modifier.weight(1f),
            )

            WormaCeptorStatusBadge(
                text = leak.severity.name.take(1),
                containerColor = severityBackground,
                contentColor = severityColor,
                modifier = Modifier.semantics { contentDescription = "Severity: ${leak.severity.name}" },
            )
        }
    }
}

@Composable
private fun SeverityIndicator(
    severityName: String,
    severityColor: Color,
    severityBackground: Color,
) {
    Box(
        modifier = Modifier
            .size(WormaCeptorTokens.TouchTarget.minimum)
            .clip(WormaCeptorTokens.Shapes.card)
            .background(severityBackground),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = severityName,
            tint = severityColor,
            modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
        )
    }
}

@Composable
private fun LeakCardDetails(
    leak: LeakInfo,
    severityColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = leak.objectClass.substringAfterLast('.'),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = WormaCeptorTokens.semantic().textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = leak.leakDescription,
            style = MaterialTheme.typography.bodySmall,
            color = WormaCeptorTokens.semantic().textSecondary,
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
                color = WormaCeptorTokens.semantic().textSecondary,
            )
            Text(
                text = formatBytes(leak.retainedSize),
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = severityColor,
            )
        }
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(name = "LeakCard - Light")
@Composable
private fun LeakCardPreview() {
    WormaCeptorTheme {
        Surface {
            LeakCard(
                leak = LeakInfo(
                    timestamp = System.currentTimeMillis(),
                    objectClass = "com.example.app.ui.HomeActivity",
                    leakDescription = "Activity retained after onDestroy",
                    retainedSize = 2 * 1_048_576L,
                    referencePath = listOf(
                        "GC Root -> static field",
                        "AppManager.instance -> activity",
                    ),
                    severity = LeakInfo.LeakSeverity.CRITICAL,
                ),
                onClick = {},
            )
        }
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(name = "LeakCard - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LeakCardDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            LeakCard(
                leak = LeakInfo(
                    timestamp = System.currentTimeMillis(),
                    objectClass = "com.example.app.data.CacheManager",
                    leakDescription = "Cache not cleared on low memory",
                    retainedSize = 512 * 1024L,
                    referencePath = emptyList(),
                    severity = LeakInfo.LeakSeverity.HIGH,
                ),
                onClick = {},
            )
        }
    }
}

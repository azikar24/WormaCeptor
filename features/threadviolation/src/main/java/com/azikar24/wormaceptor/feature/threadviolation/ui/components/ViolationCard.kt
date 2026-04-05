package com.azikar24.wormaceptor.feature.threadviolation.ui.components

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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
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
import com.azikar24.wormaceptor.core.ui.util.formatTimestamp
import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.domain.entities.ThreadViolation.ViolationType

@Composable
internal fun ViolationCard(
    violation: ThreadViolation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typeColor = when (violation.violationType) {
        ViolationType.DISK_READ -> WormaCeptorTokens.Colors.ThreadViolation.diskRead
        ViolationType.DISK_WRITE -> WormaCeptorTokens.Colors.ThreadViolation.diskWrite
        ViolationType.NETWORK -> WormaCeptorTokens.Colors.ThreadViolation.network
        ViolationType.SLOW_CALL -> WormaCeptorTokens.Colors.ThreadViolation.slowCall
        ViolationType.CUSTOM_SLOW_CODE -> WormaCeptorTokens.Colors.ThreadViolation.customSlowCode
    }
    val icon = when (violation.violationType) {
        ViolationType.DISK_READ -> Icons.Default.SaveAlt
        ViolationType.DISK_WRITE -> Icons.Default.Storage
        ViolationType.NETWORK -> Icons.Default.Cloud
        ViolationType.SLOW_CALL -> Icons.Default.SlowMotionVideo
        ViolationType.CUSTOM_SLOW_CODE -> Icons.Default.Speed
    }

    WormaCeptorCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(WormaCeptorTokens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(WormaCeptorTokens.TouchTarget.minimum)
                    .clip(RoundedCornerShape(WormaCeptorTokens.Radius.md))
                    .background(typeColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = violation.violationType.name.replace("_", " "),
                    tint = typeColor,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                )
            }
            Spacer(Modifier.width(WormaCeptorTokens.Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = violation.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm)) {
                    Text(
                        formatTimestamp(violation.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    violation.durationMs?.let {
                        Text(
                            "${it}ms",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = typeColor,
                        )
                    }
                }
            }
            Surface(
                shape = WormaCeptorTokens.Shapes.chip,
                color = typeColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
            ) {
                Text(
                    text = violation.violationType.name.take(1),
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorTokens.Spacing.sm,
                        vertical = WormaCeptorTokens.Spacing.xs,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = typeColor,
                )
            }
        }
    }
}

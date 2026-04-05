package com.azikar24.wormaceptor.feature.leakdetection.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatTimestampFull
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.feature.leakdetection.R

@Composable
internal fun LeakDetailContent(
    leak: LeakInfo,
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

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
    ) {
        // Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
            ) {
                Box(
                    modifier = Modifier
                        .size(WormaCeptorTokens.Spacing.xxxl)
                        .clip(RoundedCornerShape(WormaCeptorTokens.Radius.lg))
                        .background(severityBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = stringResource(R.string.leakdetection_title),
                        tint = severityColor,
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
                    )
                }
                Column {
                    Text(
                        text = leak.objectClass.substringAfterLast('.'),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(WormaCeptorTokens.Radius.xs),
                            color = severityBackground,
                        ) {
                            Text(
                                text = leak.severity.name,
                                modifier = Modifier.padding(
                                    horizontal = WormaCeptorTokens.Spacing.sm,
                                    vertical = WormaCeptorTokens.Spacing.xxs,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = severityColor,
                            )
                        }
                        Text(
                            text = formatBytes(leak.retainedSize),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = severityColor,
                        )
                    }
                }
            }
        }

        // Details
        item {
            DetailSection(
                title = stringResource(R.string.leakdetection_section_details),
                items = listOf(
                    stringResource(R.string.leakdetection_detail_class) to leak.objectClass,
                    stringResource(R.string.leakdetection_detail_description) to leak.leakDescription,
                    stringResource(R.string.leakdetection_detail_retained_size) to formatBytes(leak.retainedSize),
                    stringResource(R.string.leakdetection_detail_detected) to formatTimestampFull(leak.timestamp),
                ),
            )
        }

        // Reference path
        if (leak.referencePath.isNotEmpty()) {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                ) {
                    Text(
                        text = stringResource(R.string.leakdetection_section_reference_path),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { heading() },
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(WormaCeptorTokens.Radius.md),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Column(
                            modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
                            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
                        ) {
                            leak.referencePath.forEach { step ->
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = WormaCeptorTokens.Alpha.PROMINENT,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { heading() },
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(WormaCeptorTokens.Radius.md),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Column(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                items.forEach { (label, value) ->
                    Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = WormaCeptorTokens.Alpha.PROMINENT),
                        )
                    }
                }
            }
        }
    }
}

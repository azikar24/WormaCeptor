package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Status bar with monitoring indicator, label, and sample count.
 * Composes [WormaCeptorMonitoringIndicator] with descriptive text.
 *
 * @param isMonitoring Whether monitoring is currently active
 * @param sampleCount Number of collected samples
 * @param modifier Modifier for the root composable
 * @param activeLabel Label shown when monitoring is active
 * @param pausedLabel Label shown when monitoring is paused
 * @param countSuffix Suffix appended to the sample count
 */
@Composable
fun WormaCeptorMonitoringStatusBar(
    isMonitoring: Boolean,
    sampleCount: Int,
    modifier: Modifier = Modifier,
    activeLabel: String = "Monitoring",
    pausedLabel: String = "Paused",
    countSuffix: String = "samples",
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            WormaCeptorMonitoringIndicator(isActive = isMonitoring)

            Text(
                text = if (isMonitoring) activeLabel else pausedLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = "$sampleCount $countSuffix",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
        )
    }
}

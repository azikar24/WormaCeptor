package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.R
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

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
    activeLabel: String = stringResource(R.string.monitoring_status_active),
    pausedLabel: String = stringResource(R.string.monitoring_status_paused),
    countSuffix: String = stringResource(R.string.monitoring_status_samples_suffix),
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
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

@Preview(name = "MonitoringStatusBar")
@Composable
private fun MonitoringStatusBarPreview() {
    WormaCeptorTheme {
        Surface {
            Column(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
            ) {
                WormaCeptorMonitoringStatusBar(
                    isMonitoring = true,
                    sampleCount = 42,
                    activeLabel = "Monitoring",
                    pausedLabel = "Paused",
                    countSuffix = "samples",
                )
                WormaCeptorMonitoringStatusBar(
                    isMonitoring = false,
                    sampleCount = 0,
                    activeLabel = "Monitoring",
                    pausedLabel = "Paused",
                    countSuffix = "samples",
                )
            }
        }
    }
}

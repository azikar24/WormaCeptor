package com.azikar24.wormaceptor.feature.ratelimit.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatDuration
import com.azikar24.wormaceptor.domain.entities.ThrottleStats
import com.azikar24.wormaceptor.feature.ratelimit.R

@Suppress("LongMethod")
@Composable
internal fun StatisticsCard(
    stats: ThrottleStats,
    colors: ToolColors.RateLimit.Scheme,
    modifier: Modifier = Modifier,
) {
    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.cardBackground,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.ratelimit_stats_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.labelPrimary,
                    modifier = Modifier.semantics { heading() },
                )
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = stringResource(R.string.ratelimit_stats_title),
                    tint = colors.primary,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = stringResource(R.string.ratelimit_stats_requests_throttled),
                    value = stats.requestsThrottled.toString(),
                    color = colors.primary,
                    colors = colors,
                )
                StatItem(
                    label = stringResource(R.string.ratelimit_stats_packets_dropped),
                    value = stats.packetsDropped.toString(),
                    color = colors.packetLoss,
                    colors = colors,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = stringResource(R.string.ratelimit_stats_total_delay),
                    value = formatDuration(stats.totalDelayMs),
                    color = colors.latency,
                    colors = colors,
                )
                StatItem(
                    label = stringResource(R.string.ratelimit_stats_bytes_throttled),
                    value = formatBytes(stats.bytesThrottled),
                    color = colors.download,
                    colors = colors,
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color,
    colors: ToolColors.RateLimit.Scheme,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(WormaCeptorTokens.Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.labelSecondary,
        )
    }
}

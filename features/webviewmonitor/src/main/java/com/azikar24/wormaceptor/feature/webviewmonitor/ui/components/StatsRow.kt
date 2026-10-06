package com.azikar24.wormaceptor.feature.webviewmonitor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.WebViewRequestStats
import com.azikar24.wormaceptor.feature.webviewmonitor.R

@Composable
internal fun StatsRow(
    stats: WebViewRequestStats,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        WormaCeptorSummaryCard(
            count = stats.totalRequests.toString(),
            label = stringResource(R.string.webviewmonitor_stats_total),
            color = WormaCeptorTokens.semantic().textPrimary,
            modifier = Modifier.weight(1f),
        )
        WormaCeptorSummaryCard(
            count = stats.successfulRequests.toString(),
            label = stringResource(R.string.webviewmonitor_stats_success),
            color = countColor(stats.successfulRequests, WormaCeptorTokens.Colors.Status.green),
            modifier = Modifier.weight(1f),
        )
        WormaCeptorSummaryCard(
            count = stats.failedRequests.toString(),
            label = stringResource(R.string.webviewmonitor_stats_failed),
            color = countColor(stats.failedRequests, WormaCeptorTokens.Colors.Status.red),
            modifier = Modifier.weight(1f),
        )
        WormaCeptorSummaryCard(
            count = stats.pendingRequests.toString(),
            label = stringResource(R.string.webviewmonitor_stats_pending),
            color = countColor(stats.pendingRequests, WormaCeptorTokens.Colors.Status.amber),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun countColor(
    count: Int,
    stateColor: Color,
): Color = if (count > 0) stateColor else WormaCeptorTokens.semantic().textPrimary

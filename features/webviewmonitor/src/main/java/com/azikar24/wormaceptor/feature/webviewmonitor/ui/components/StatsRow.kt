package com.azikar24.wormaceptor.feature.webviewmonitor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            color = WormaCeptorTokens.Colors.Status.blue,
            modifier = Modifier.weight(1f),
        )
        WormaCeptorSummaryCard(
            count = stats.successfulRequests.toString(),
            label = stringResource(R.string.webviewmonitor_stats_success),
            color = WormaCeptorTokens.Colors.Status.green,
            modifier = Modifier.weight(1f),
        )
        WormaCeptorSummaryCard(
            count = stats.failedRequests.toString(),
            label = stringResource(R.string.webviewmonitor_stats_failed),
            color = WormaCeptorTokens.Colors.Status.red,
            modifier = Modifier.weight(1f),
        )
        WormaCeptorSummaryCard(
            count = stats.pendingRequests.toString(),
            label = stringResource(R.string.webviewmonitor_stats_pending),
            color = WormaCeptorTokens.Colors.Status.amber,
            modifier = Modifier.weight(1f),
        )
    }
}

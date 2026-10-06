package com.azikar24.wormaceptor.feature.threadviolation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.ViolationStats
import com.azikar24.wormaceptor.feature.threadviolation.R

@Composable
internal fun ViolationSummarySection(
    stats: ViolationStats,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        WormaCeptorSummaryCard(
            count = stats.diskReadCount.toString(),
            label = stringResource(R.string.threadviolation_summary_disk_read),
            color = countColor(stats.diskReadCount),
            modifier = Modifier.weight(1f),
            backgroundColor = WormaCeptorTokens.semantic().surface,
            labelColor = WormaCeptorTokens.semantic().textSecondary,
        )
        WormaCeptorSummaryCard(
            count = stats.diskWriteCount.toString(),
            label = stringResource(R.string.threadviolation_summary_disk_write),
            color = countColor(stats.diskWriteCount),
            modifier = Modifier.weight(1f),
            backgroundColor = WormaCeptorTokens.semantic().surface,
            labelColor = WormaCeptorTokens.semantic().textSecondary,
        )
        WormaCeptorSummaryCard(
            count = stats.networkCount.toString(),
            label = stringResource(R.string.threadviolation_summary_network),
            color = countColor(stats.networkCount),
            modifier = Modifier.weight(1f),
            backgroundColor = WormaCeptorTokens.semantic().surface,
            labelColor = WormaCeptorTokens.semantic().textSecondary,
        )
        WormaCeptorSummaryCard(
            count = (stats.slowCallCount + stats.customSlowCodeCount).toString(),
            label = stringResource(R.string.threadviolation_summary_slow),
            color = countColor(stats.slowCallCount + stats.customSlowCodeCount),
            modifier = Modifier.weight(1f),
            backgroundColor = WormaCeptorTokens.semantic().surface,
            labelColor = WormaCeptorTokens.semantic().textSecondary,
        )
    }
}

@Composable
private fun countColor(count: Int): Color = if (count > 0) {
    WormaCeptorTokens.semantic().textPrimary
} else {
    WormaCeptorTokens.semantic().textSecondary
}

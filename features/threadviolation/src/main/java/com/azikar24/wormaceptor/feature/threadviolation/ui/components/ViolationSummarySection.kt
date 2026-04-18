package com.azikar24.wormaceptor.feature.threadviolation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
            color = WormaCeptorTokens.Colors.ThreadViolation.diskRead,
            modifier = Modifier.weight(1f),
            backgroundColor = WormaCeptorTokens.semantic().surface,
            labelColor = WormaCeptorTokens.semantic().textSecondary,
        )
        WormaCeptorSummaryCard(
            count = stats.diskWriteCount.toString(),
            label = stringResource(R.string.threadviolation_summary_disk_write),
            color = WormaCeptorTokens.Colors.ThreadViolation.diskWrite,
            modifier = Modifier.weight(1f),
            backgroundColor = WormaCeptorTokens.semantic().surface,
            labelColor = WormaCeptorTokens.semantic().textSecondary,
        )
        WormaCeptorSummaryCard(
            count = stats.networkCount.toString(),
            label = stringResource(R.string.threadviolation_summary_network),
            color = WormaCeptorTokens.Colors.ThreadViolation.network,
            modifier = Modifier.weight(1f),
            backgroundColor = WormaCeptorTokens.semantic().surface,
            labelColor = WormaCeptorTokens.semantic().textSecondary,
        )
        WormaCeptorSummaryCard(
            count = (stats.slowCallCount + stats.customSlowCodeCount).toString(),
            label = stringResource(R.string.threadviolation_summary_slow),
            color = WormaCeptorTokens.Colors.ThreadViolation.slowCall,
            modifier = Modifier.weight(1f),
            backgroundColor = WormaCeptorTokens.semantic().surface,
            labelColor = WormaCeptorTokens.semantic().textSecondary,
        )
    }
}

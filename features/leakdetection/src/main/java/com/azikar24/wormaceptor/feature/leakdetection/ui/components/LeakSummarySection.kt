package com.azikar24.wormaceptor.feature.leakdetection.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.LeakSummary
import com.azikar24.wormaceptor.feature.leakdetection.R

@Composable
internal fun LeakSummarySection(
    summary: LeakSummary,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        val leakColors = WormaCeptorTokens.Colors.LeakDetection
        WormaCeptorSummaryCard(
            count = summary.criticalCount.toString(),
            label = stringResource(R.string.leakdetection_severity_critical),
            color = leakColors.critical,
            modifier = Modifier.weight(1f),
            backgroundColor = leakColors.critical.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE),
            labelColor = leakColors.critical.copy(alpha = WormaCeptorTokens.Alpha.PROMINENT),
        )
        WormaCeptorSummaryCard(
            count = summary.highCount.toString(),
            label = stringResource(R.string.leakdetection_severity_high),
            color = leakColors.high,
            modifier = Modifier.weight(1f),
            backgroundColor = leakColors.high.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE),
            labelColor = leakColors.high.copy(alpha = WormaCeptorTokens.Alpha.PROMINENT),
        )
        WormaCeptorSummaryCard(
            count = summary.mediumCount.toString(),
            label = stringResource(R.string.leakdetection_severity_medium),
            color = leakColors.medium,
            modifier = Modifier.weight(1f),
            backgroundColor = leakColors.medium.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE),
            labelColor = leakColors.medium.copy(alpha = WormaCeptorTokens.Alpha.PROMINENT),
        )
        WormaCeptorSummaryCard(
            count = summary.lowCount.toString(),
            label = stringResource(R.string.leakdetection_severity_low),
            color = leakColors.low,
            modifier = Modifier.weight(1f),
            backgroundColor = leakColors.low.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE),
            labelColor = leakColors.low.copy(alpha = WormaCeptorTokens.Alpha.PROMINENT),
        )
    }
}

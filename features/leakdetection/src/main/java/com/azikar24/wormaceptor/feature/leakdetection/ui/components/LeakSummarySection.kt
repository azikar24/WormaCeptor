package com.azikar24.wormaceptor.feature.leakdetection.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.LeakInfo
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
        SeveritySummaryCard(
            count = summary.criticalCount,
            label = stringResource(R.string.leakdetection_severity_critical),
            severity = LeakInfo.LeakSeverity.CRITICAL,
            modifier = Modifier.weight(1f),
        )
        SeveritySummaryCard(
            count = summary.highCount,
            label = stringResource(R.string.leakdetection_severity_high),
            severity = LeakInfo.LeakSeverity.HIGH,
            modifier = Modifier.weight(1f),
        )
        SeveritySummaryCard(
            count = summary.mediumCount,
            label = stringResource(R.string.leakdetection_severity_medium),
            severity = LeakInfo.LeakSeverity.MEDIUM,
            modifier = Modifier.weight(1f),
        )
        SeveritySummaryCard(
            count = summary.lowCount,
            label = stringResource(R.string.leakdetection_severity_low),
            severity = LeakInfo.LeakSeverity.LOW,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SeveritySummaryCard(
    count: Int,
    label: String,
    severity: LeakInfo.LeakSeverity,
    modifier: Modifier = Modifier,
) {
    WormaCeptorSummaryCard(
        count = count.toString(),
        label = label,
        color = if (count > 0) severityColor(severity) else WormaCeptorTokens.semantic().textPrimary,
        modifier = modifier,
    )
}

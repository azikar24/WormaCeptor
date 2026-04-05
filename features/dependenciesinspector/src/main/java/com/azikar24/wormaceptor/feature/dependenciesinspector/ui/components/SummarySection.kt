package com.azikar24.wormaceptor.feature.dependenciesinspector.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.DependencySummary
import com.azikar24.wormaceptor.feature.dependenciesinspector.R

@Composable
internal fun SummarySection(
    summary: DependencySummary,
    colors: ToolColors.DependenciesInspector.Scheme,
    modifier: Modifier = Modifier,
) {
    Row(modifier, Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm)) {
        WormaCeptorSummaryCard(
            count = summary.totalDetected.toString(),
            label = stringResource(R.string.dependenciesinspector_summary_detected),
            color = colors.primary,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
        WormaCeptorSummaryCard(
            count = summary.withVersion.toString(),
            label = stringResource(R.string.dependenciesinspector_summary_versioned),
            color = colors.versionDetected,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
        WormaCeptorSummaryCard(
            count = summary.withoutVersion.toString(),
            label = stringResource(R.string.dependenciesinspector_summary_unknown),
            color = colors.versionUnknown,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
    }
}

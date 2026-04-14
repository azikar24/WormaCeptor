package com.azikar24.wormaceptor.feature.loadedlibraries.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.LibrarySummary
import com.azikar24.wormaceptor.feature.loadedlibraries.R

@Composable
internal fun SummarySection(
    summary: LibrarySummary,
    colors: ToolColors.LoadedLibraries.Scheme,
    modifier: Modifier = Modifier,
) {
    Row(modifier, Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm)) {
        WormaCeptorSummaryCard(
            count = summary.nativeSoCount.toString(),
            label = stringResource(R.string.loadedlibraries_summary_native),
            color = colors.nativeSo,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
        WormaCeptorSummaryCard(
            count = summary.dexCount.toString(),
            label = stringResource(R.string.loadedlibraries_summary_dex),
            color = colors.dex,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
        WormaCeptorSummaryCard(
            count = summary.jarCount.toString(),
            label = stringResource(R.string.loadedlibraries_summary_jar),
            color = colors.jar,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
        WormaCeptorSummaryCard(
            count = summary.totalLibraries.toString(),
            label = stringResource(R.string.loadedlibraries_summary_total),
            color = colors.primary,
            modifier = Modifier.weight(1f),
            backgroundColor = colors.cardBackground,
            labelColor = colors.labelSecondary,
        )
    }
}

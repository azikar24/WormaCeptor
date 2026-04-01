package com.azikar24.wormaceptor.feature.recomposition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.recomposition.R

@Composable
internal fun SummaryRow(
    sessionDurationMs: Long,
    totalRecompositions: Long,
    modifier: Modifier = Modifier,
) {
    val totalSeconds = sessionDurationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val durationText = stringResource(R.string.recomposition_duration_format, minutes, seconds)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
    ) {
        WormaCeptorSummaryCard(
            count = durationText,
            label = stringResource(R.string.recomposition_session_duration),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )

        WormaCeptorSummaryCard(
            count = formatCount(totalRecompositions),
            label = stringResource(R.string.recomposition_total_recompositions),
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f),
        )
    }
}

internal fun formatCount(count: Long): String = when {
    count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
    count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
    else -> count.toString()
}

package com.azikar24.wormaceptor.feature.recomposition.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.recomposition.R

@Composable
internal fun SummaryRow(
    formattedDuration: String,
    formattedTotalRecompositions: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
    ) {
        WormaCeptorSummaryCard(
            count = formattedDuration,
            label = stringResource(R.string.recomposition_session_duration),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )

        WormaCeptorSummaryCard(
            count = formattedTotalRecompositions,
            label = stringResource(R.string.recomposition_total_recompositions),
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(name = "SummaryRow", showBackground = true)
@Composable
private fun SummaryRowPreview() {
    WormaCeptorTheme {
        SummaryRow(
            formattedDuration = "02:15",
            formattedTotalRecompositions = "1.2K",
        )
    }
}

package com.azikar24.wormaceptor.feature.websocket.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.feature.websocket.R

@Composable
internal fun StatsBar(
    label: String,
    totalCount: Int,
    filteredCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = if (filteredCount != totalCount) {
                stringResource(R.string.websocket_stats_filtered, filteredCount, totalCount)
            } else {
                stringResource(R.string.websocket_stats_total, totalCount)
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsBarFilteredPreview() {
    WormaCeptorTheme {
        StatsBar(
            modifier = Modifier.fillMaxWidth(),
            label = "Connections",
            totalCount = 10,
            filteredCount = 3,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsBarTotalPreview() {
    WormaCeptorTheme {
        StatsBar(
            modifier = Modifier.fillMaxWidth(),
            label = "Messages",
            totalCount = 42,
            filteredCount = 42,
        )
    }
}

package com.azikar24.wormaceptor.feature.webviewmonitor.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.feature.webviewmonitor.R

@Composable
internal fun CountText(
    filteredCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = if (filteredCount == totalCount) {
            stringResource(R.string.webviewmonitor_request_count, totalCount)
        } else {
            stringResource(R.string.webviewmonitor_request_count_filtered, filteredCount, totalCount)
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

package com.azikar24.wormaceptor.feature.viewer.ui.components.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R
import kotlinx.collections.immutable.ImmutableMap

@Composable
internal fun StatusFilterBars(
    statusCounts: ImmutableMap<IntRange, Int>,
    selectedRanges: Set<IntRange>,
    onStatusToggled: (IntRange) -> Unit,
) {
    val statusFilters = listOf(
        Triple("2xx", 200..299, WormaCeptorTokens.Colors.Status.green),
        Triple("3xx", 300..399, WormaCeptorTokens.Colors.Status.blue),
        Triple("4xx", 400..499, WormaCeptorTokens.Colors.Status.amber),
        Triple("5xx", 500..599, WormaCeptorTokens.Colors.Status.red),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        statusFilters.chunked(2).forEach { rowFilters ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                rowFilters.forEach { (label, range, color) ->
                    val count = statusCounts[range] ?: 0
                    val isSelected = range in selectedRanges
                    val sublabelText = when (label) {
                        "2xx" -> stringResource(R.string.viewer_filter_success)
                        "3xx" -> stringResource(R.string.viewer_filter_redirect)
                        "4xx" -> stringResource(R.string.viewer_filter_client_error)
                        "5xx" -> stringResource(R.string.viewer_filter_server_error)
                        else -> null
                    }

                    GridFilterCard(
                        label = label,
                        sublabel = sublabelText,
                        count = count,
                        color = color,
                        isSelected = isSelected,
                        onClick = { onStatusToggled(range) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

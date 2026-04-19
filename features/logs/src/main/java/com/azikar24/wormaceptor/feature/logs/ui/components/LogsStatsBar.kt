package com.azikar24.wormaceptor.feature.logs.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.azikar24.wormaceptor.core.ui.components.status.WormaCeptorStatusDot
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.logs.R

@Composable
internal fun StatsBar(
    totalCount: Int,
    filteredCount: Int,
    isCapturing: Boolean,
    pid: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Capture indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
            ) {
                WormaCeptorStatusDot(
                    color = if (isCapturing) {
                        WormaCeptorTokens.semantic().accent
                    } else {
                        WormaCeptorTokens.semantic().textTertiary
                    },
                )
                Text(
                    text = if (isCapturing) {
                        stringResource(
                            R.string.logs_status_capturing,
                        )
                    } else {
                        stringResource(R.string.logs_status_paused)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = WormaCeptorTokens.semantic().textSecondary,
                )
            }

            // PID
            Text(
                text = stringResource(R.string.logs_pid, pid),
                style = MaterialTheme.typography.labelSmall,
                color = WormaCeptorTokens.semantic().textSecondary,
                fontFamily = FontFamily.Monospace,
            )
        }

        // Counts
        Text(
            text = if (filteredCount != totalCount) {
                stringResource(R.string.logs_entries_filtered, filteredCount, totalCount)
            } else {
                stringResource(R.string.logs_entries_total, totalCount)
            },
            style = MaterialTheme.typography.labelSmall,
            color = WormaCeptorTokens.semantic().textSecondary,
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun StatsBarPreview(@PreviewParameter(StatsBarPreviewProvider::class) params: StatsBarPreviewData) {
    WormaCeptorTheme {
        StatsBar(
            totalCount = params.totalCount,
            filteredCount = params.filteredCount,
            isCapturing = params.isCapturing,
            pid = params.pid,
        )
    }
}

private data class StatsBarPreviewData(
    val totalCount: Int,
    val filteredCount: Int,
    val isCapturing: Boolean,
    val pid: Int,
)

@Suppress("MagicNumber")
private class StatsBarPreviewProvider : PreviewParameterProvider<StatsBarPreviewData> {
    override val values: Sequence<StatsBarPreviewData> = sequenceOf(
        StatsBarPreviewData(totalCount = 42, filteredCount = 42, isCapturing = true, pid = 12_345),
        StatsBarPreviewData(totalCount = 42, filteredCount = 15, isCapturing = true, pid = 12_345),
        StatsBarPreviewData(totalCount = 10, filteredCount = 10, isCapturing = false, pid = 12_345),
    )
}

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
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorStatusDot
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
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // PID
            Text(
                text = stringResource(R.string.logs_pid, pid),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

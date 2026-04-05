package com.azikar24.wormaceptor.feature.viewer.ui.components.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R

@Composable
internal fun FilterHeader(
    filteredCount: Int,
    totalCount: Int,
    filtersActive: Boolean,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = WormaCeptorTokens.Spacing.lg,
                vertical = WormaCeptorTokens.Spacing.md,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stringResource(R.string.viewer_filter_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (filtersActive) {
                Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xxs))
                Text(
                    text = stringResource(R.string.viewer_filter_results_count, filteredCount, totalCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(WormaCeptorTokens.Radius.pill),
            color = if (filtersActive) {
                MaterialTheme.colorScheme.primary.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.BOLD)
            },
            modifier = Modifier.semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = context.getString(
                    R.string.viewer_filter_results_description,
                    filteredCount,
                    totalCount,
                )
            },
        ) {
            Text(
                text = filteredCount.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (filtersActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(
                    horizontal = WormaCeptorTokens.Spacing.lg,
                    vertical = WormaCeptorTokens.Spacing.sm,
                ),
            )
        }
    }
}

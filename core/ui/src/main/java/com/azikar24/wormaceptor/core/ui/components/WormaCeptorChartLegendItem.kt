package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Colored dot + label for chart legends.
 *
 * @param label Legend label text
 * @param color Color of the legend dot
 * @param modifier Modifier for the root composable
 */
@Composable
fun WormaCeptorChartLegendItem(label: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(WormaCeptorDesignSystem.Spacing.md)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

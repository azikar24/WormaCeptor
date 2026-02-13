package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Count + label stats card with color-coded background.
 * Used across monitoring screens for summary statistics.
 *
 * @param count The metric value to display
 * @param label Descriptive label text
 * @param color Color for the count text
 * @param modifier Modifier for the root composable
 * @param backgroundColor Card background color
 * @param labelColor Color for the label text
 */
@Composable
fun WormaCeptorSummaryCard(
    count: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(
        alpha = WormaCeptorDesignSystem.Alpha.bold,
    ),
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
            )
        }
    }
}

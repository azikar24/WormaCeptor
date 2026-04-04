package com.azikar24.wormaceptor.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

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
        alpha = WormaCeptorTokens.Alpha.BOLD,
    ),
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    WormaCeptorCard(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorTokens.Radius.lg),
        backgroundColor = backgroundColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md),
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

// region Previews

@Preview(name = "SummaryCard - Light")
@Composable
private fun SummaryCardLightPreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorSummaryCard(
                count = "1,247",
                label = "Total Requests",
                color = WormaCeptorTokens.Colors.Status.blue,
                modifier = Modifier.width(160.dp),
            )
        }
    }
}

@Preview(name = "SummaryCard - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SummaryCardDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            WormaCeptorSummaryCard(
                count = "38",
                label = "Errors",
                color = WormaCeptorTokens.Colors.Status.red,
                modifier = Modifier.width(160.dp),
            )
        }
    }
}

// endregion

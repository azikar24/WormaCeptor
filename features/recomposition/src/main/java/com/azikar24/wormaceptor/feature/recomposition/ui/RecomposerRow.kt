package com.azikar24.wormaceptor.feature.recomposition.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.recomposition.R
import com.azikar24.wormaceptor.feature.recomposition.vm.RecompositionItem

@Composable
internal fun RecomposerRow(
    index: Int,
    item: RecompositionItem,
    modifier: Modifier = Modifier,
) {
    val color = rateColor(item.ratePerSecond)
    val statusText = rateLabel(item.ratePerSecond)

    Surface(
        modifier = modifier,
        shape = WormaCeptorTokens.Shapes.cardLarge,
        color = WormaCeptorTokens.semantic().surfaceVariant.copy(
            alpha = WormaCeptorTokens.Alpha.BOLD,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "#$index",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = WormaCeptorTokens.semantic().textSecondary,
                modifier = Modifier.width(WormaCeptorTokens.IconSize.xl),
            )

            Box(
                modifier = Modifier
                    .size(WormaCeptorTokens.IconSize.xxs)
                    .clip(CircleShape)
                    .background(color),
            )

            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = WormaCeptorTokens.semantic().textPrimary,
                )
                Text(
                    text = stringResource(R.string.recomposition_count_label, item.count),
                    style = MaterialTheme.typography.labelSmall,
                    color = WormaCeptorTokens.semantic().textSecondary,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.recomposition_rate_per_second, item.ratePerSecond),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = color,
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                )
            }
        }
    }
}

@Suppress("MagicNumber")
@Preview(name = "RecomposerRow - Normal", showBackground = true)
@Composable
private fun RecomposerRowNormalPreview() {
    WormaCeptorTheme {
        RecomposerRow(
            index = 1,
            item = RecompositionItem("ProductCard", 12L, 0.4f),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Suppress("MagicNumber")
@Preview(name = "RecomposerRow - Critical", showBackground = true)
@Composable
private fun RecomposerRowCriticalPreview() {
    WormaCeptorTheme {
        RecomposerRow(
            index = 2,
            item = RecompositionItem("SearchBar", 342L, 11.4f),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

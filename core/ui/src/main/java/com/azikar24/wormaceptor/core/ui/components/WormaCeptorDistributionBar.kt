package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import kotlin.math.roundToInt

private const val BarAnimationDurationMs = 600
private const val PercentMultiplier = 100f

/**
 * Labeled progress bar with count and percentage.
 * Used in metrics cards for response time and status code distributions.
 *
 * @param label Distribution label text
 * @param count Number of items in this category
 * @param total Total number of items
 * @param color Bar and indicator dot color
 * @param modifier Modifier for the root composable
 */
@Composable
fun WormaCeptorDistributionBar(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val percentage = if (total > 0) count.toFloat() / total.toFloat() * PercentMultiplier else 0f
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = BarAnimationDurationMs),
        label = "distribution_bar_animation",
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = "$count (${animatedPercentage.roundToInt()}%)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(WormaCeptorTokens.Radius.xs))
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercentage / PercentMultiplier)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(WormaCeptorTokens.Radius.xs))
                    .background(color),
            )
        }
    }
}

// region Previews

@Preview(name = "DistributionBar - Light")
@Composable
private fun DistributionBarLightPreview() {
    WormaCeptorTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                WormaCeptorDistributionBar(
                    label = "2xx Success",
                    count = 842,
                    total = 1000,
                    color = WormaCeptorTokens.Colors.Chart.success2xx,
                )
                WormaCeptorDistributionBar(
                    label = "4xx Client Error",
                    count = 120,
                    total = 1000,
                    color = WormaCeptorTokens.Colors.Chart.clientError4xx,
                )
                WormaCeptorDistributionBar(
                    label = "5xx Server Error",
                    count = 38,
                    total = 1000,
                    color = WormaCeptorTokens.Colors.Chart.serverError5xx,
                )
            }
        }
    }
}

// endregion

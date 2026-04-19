package com.azikar24.wormaceptor.core.ui.components.state

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.rememberReduceMotion
import com.azikar24.wormaceptor.core.ui.theme.tokens.scaled

private const val ShimmerDurationMs = 1200
private const val ShimmerStartOffset = -2f
private const val ShimmerEndOffset = 2f

/** Width in px of the synthetic shimmer gradient band. Large enough to cover typical list rows. */
private const val ShimmerGradientWidthPx = 1000f

/** Fraction of full width used for the last line in [WormaCeptorSkeletonList] to echo paragraph ragging. */
private const val LastLineWidthFraction = 0.6f

/**
 * Animated shimmer placeholder for loading states. Honors system reduce-motion
 * and falls back to a flat surface tint when animations are disabled.
 *
 * @param modifier Modifier for the root surface.
 * @param shape Shape of the shimmer block (defaults to [WormaCeptorTokens.Shapes.card]).
 */
@Composable
fun WormaCeptorSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = WormaCeptorTokens.Shapes.card,
) {
    val reduceMotion = rememberReduceMotion()
    val base = MaterialTheme.colorScheme.surfaceVariant.copy(
        alpha = WormaCeptorTokens.Alpha.LIGHT,
    )
    val highlight = MaterialTheme.colorScheme.surfaceVariant.copy(
        alpha = WormaCeptorTokens.Alpha.MODERATE,
    )

    if (reduceMotion) {
        Surface(modifier = modifier, shape = shape, color = base) {}
        return
    }

    val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val progress by transition.animateFloat(
        initialValue = ShimmerStartOffset,
        targetValue = ShimmerEndOffset,
        animationSpec = infiniteRepeatable(
            animation = tween(ShimmerDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeleton_shimmer_progress",
    )

    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = androidx.compose.ui.geometry.Offset(progress * ShimmerGradientWidthPx, 0f),
        end = androidx.compose.ui.geometry.Offset((progress + 1f) * ShimmerGradientWidthPx, 0f),
    )

    Surface(
        modifier = modifier.clip(shape),
        shape = shape,
        color = base,
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush),
        )
    }
}

/**
 * Convenience helper: a vertical stack of [lines] shimmer bars, each [lineHeight] tall.
 * Useful for list item placeholders while data loads.
 *
 * @param lines Number of shimmer bars.
 * @param modifier Modifier for the root column.
 * @param lineHeight Height of each bar.
 */
@Composable
fun WormaCeptorSkeletonList(
    lines: Int,
    modifier: Modifier = Modifier,
    lineHeight: Dp = 16.dp,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm.scaled()),
    ) {
        repeat(lines) { index ->
            val widthFraction = if (index == lines - 1) LastLineWidthFraction else 1f
            WormaCeptorSkeleton(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(lineHeight),
                shape = WormaCeptorTokens.Shapes.chip,
            )
        }
    }
}

// region Previews

@Preview(name = "Skeleton - Light")
@Composable
private fun SkeletonLightPreview() {
    WormaCeptorTheme {
        Surface {
            Column(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
            ) {
                WormaCeptorSkeleton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                )
                WormaCeptorSkeletonList(lines = 4)
                WormaCeptorSkeleton(
                    modifier = Modifier
                        .width(120.dp)
                        .height(24.dp),
                    shape = WormaCeptorTokens.Shapes.pill,
                )
            }
        }
    }
}

@Preview(name = "Skeleton - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SkeletonDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
            ) {
                WormaCeptorSkeleton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                )
                WormaCeptorSkeletonList(lines = 4)
            }
        }
    }
}

// endregion

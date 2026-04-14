@file:Suppress("MatchingDeclarationName")

package com.azikar24.wormaceptor.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * Visual style for [WormaCeptorBadge].
 *
 * `Filled` -- solid container with contrasting content (emphasized).
 * `Soft`   -- low-alpha container tint with strong content color (readable yet subtle).
 * `Tonal`  -- caller-provided container + content colors (fully custom).
 */
sealed interface BadgeVariant {
    /** Solid fill using [color] as container with a readable content tint. */
    data class Filled(val color: Color) : BadgeVariant

    /** Soft tint using [color] at [WormaCeptorTokens.Alpha.SOFT] with [color] as content. */
    data class Soft(val color: Color) : BadgeVariant

    /** Explicit container + content colors, for palette-driven callers. */
    data class Tonal(
        /** Surface (background) color of the badge. */
        val containerColor: Color,
        /** Text (content) color of the badge. */
        val contentColor: Color,
    ) : BadgeVariant
}

/**
 * Unified badge primitive. All WormaCeptor badges (method, status, category)
 * share a single rendering path so spacing, shape, and typography stay in lock-step.
 *
 * @param text Badge label (short, typically 1-4 chars or an uppercase method name).
 * @param variant Visual variant -- filled, soft, or tonal.
 * @param modifier Modifier for the root surface.
 * @param shape Badge shape (defaults to [WormaCeptorTokens.Shapes.badge]).
 * @param textStyle Typography override (defaults to [MaterialTheme.typography.labelSmall]).
 * @param horizontalPadding Inner horizontal padding.
 * @param verticalPadding Inner vertical padding.
 */
@Composable
fun WormaCeptorBadge(
    text: String,
    variant: BadgeVariant,
    modifier: Modifier = Modifier,
    shape: Shape = WormaCeptorTokens.Shapes.badge,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    horizontalPadding: Dp = WormaCeptorTokens.Spacing.sm,
    verticalPadding: Dp = WormaCeptorTokens.Spacing.xs,
) {
    val (containerColor, contentColor) = when (variant) {
        is BadgeVariant.Filled -> variant.color to contentColorFor(variant.color)
        is BadgeVariant.Soft -> variant.color.copy(alpha = WormaCeptorTokens.Alpha.SOFT) to variant.color
        is BadgeVariant.Tonal -> variant.containerColor to variant.contentColor
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = text,
            style = textStyle,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = horizontalPadding,
                vertical = verticalPadding,
            ),
        )
    }
}

// Relative luminance weights from ITU-R BT.601 (perceived brightness).
private const val LuminanceWeightRed = 0.299
private const val LuminanceWeightGreen = 0.587
private const val LuminanceWeightBlue = 0.114
private const val LuminanceThreshold = 0.5

// Simple contrast helper for Filled variant -- dark content on light fills, light on dark.
private fun contentColorFor(background: Color): Color {
    val luminance = LuminanceWeightRed * background.red +
        LuminanceWeightGreen * background.green +
        LuminanceWeightBlue * background.blue
    return if (luminance > LuminanceThreshold) Color.Black else Color.White
}

// region Previews

@Preview(name = "Badge - Variants Light")
@Composable
private fun BadgeVariantsLightPreview() {
    WormaCeptorTheme {
        Surface {
            Row(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorBadge(
                    text = "FILLED",
                    variant = BadgeVariant.Filled(WormaCeptorTokens.Colors.Status.green),
                )
                WormaCeptorBadge(
                    text = "SOFT",
                    variant = BadgeVariant.Soft(WormaCeptorTokens.Colors.HttpMethod.post),
                )
                WormaCeptorBadge(
                    text = "TONAL",
                    variant = BadgeVariant.Tonal(
                        containerColor = WormaCeptorTokens.Colors.Status.red.copy(
                            alpha = WormaCeptorTokens.Alpha.LIGHT,
                        ),
                        contentColor = WormaCeptorTokens.Colors.Status.red,
                    ),
                )
            }
        }
    }
}

@Preview(name = "Badge - Variants Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BadgeVariantsDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            Row(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorBadge(
                    text = "FILLED",
                    variant = BadgeVariant.Filled(WormaCeptorTokens.Colors.Status.green),
                )
                WormaCeptorBadge(
                    text = "SOFT",
                    variant = BadgeVariant.Soft(WormaCeptorTokens.Colors.HttpMethod.post),
                )
                WormaCeptorBadge(
                    text = "TONAL",
                    variant = BadgeVariant.Tonal(
                        containerColor = WormaCeptorTokens.Colors.Status.red.copy(
                            alpha = WormaCeptorTokens.Alpha.LIGHT,
                        ),
                        contentColor = WormaCeptorTokens.Colors.Status.red,
                    ),
                )
            }
        }
    }
}

// endregion

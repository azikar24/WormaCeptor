package com.azikar24.wormaceptor.core.ui.components.badge

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.contentColorFor

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

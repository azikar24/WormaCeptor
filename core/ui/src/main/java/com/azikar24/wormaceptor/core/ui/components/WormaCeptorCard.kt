package com.azikar24.wormaceptor.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme

/**
 * Visual style for [WormaCeptorCard].
 */
enum class CardStyle {
    /** Surface card with subtle elevation-based background. Default for most cards. */
    Filled,

    /** Card with a thin border and subtle fill. For selectable or expandable content. */
    Outlined,

    /** Card with visible elevation shadow. For prominent/floating cards. */
    Elevated,
}

/**
 * Unified card component for WormaCeptor.
 *
 * Provides consistent shape, colors, and border across all feature modules.
 * Replaces raw [Card] calls with standardized styling.
 *
 * @param modifier Modifier for the card
 * @param style Visual style variant
 * @param shape Card shape (defaults to [WormaCeptorDesignSystem.Shapes.card])
 * @param backgroundColor Optional override for background color (e.g., feature-specific cardBackground)
 * @param borderColor Optional override for border color (Outlined style only)
 * @param content Card body content
 */
@Composable
fun WormaCeptorCard(
    modifier: Modifier = Modifier,
    style: CardStyle = CardStyle.Filled,
    shape: Shape = WormaCeptorDesignSystem.Shapes.card,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val containerColor = backgroundColor ?: when (style) {
        CardStyle.Filled -> MaterialTheme.colorScheme.surfaceColorAtElevation(
            WormaCeptorDesignSystem.Elevation.sm,
        )
        CardStyle.Outlined -> MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = WormaCeptorDesignSystem.Alpha.SUBTLE,
        )
        CardStyle.Elevated -> MaterialTheme.colorScheme.surfaceColorAtElevation(
            WormaCeptorDesignSystem.Elevation.md,
        )
    }

    val border = when (style) {
        CardStyle.Outlined -> BorderStroke(
            width = WormaCeptorDesignSystem.BorderWidth.thin,
            color = borderColor ?: MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = WormaCeptorDesignSystem.Alpha.MEDIUM,
            ),
        )
        else -> null
    }

    val elevation = when (style) {
        CardStyle.Elevated -> CardDefaults.cardElevation(
            defaultElevation = WormaCeptorDesignSystem.Elevation.md,
        )
        else -> CardDefaults.cardElevation(
            defaultElevation = WormaCeptorDesignSystem.Elevation.xs,
        )
    }

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = elevation,
        border = border,
        content = content,
    )
}

/**
 * Clickable variant of [WormaCeptorCard].
 *
 * @param onClick Callback when card is tapped
 * @param modifier Modifier for the card
 * @param style Visual style variant
 * @param shape Card shape
 * @param backgroundColor Optional override for background color
 * @param borderColor Optional override for border color
 * @param enabled Whether the card responds to clicks
 * @param content Card body content
 */
@Composable
fun WormaCeptorCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: CardStyle = CardStyle.Filled,
    shape: Shape = WormaCeptorDesignSystem.Shapes.card,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val containerColor = backgroundColor ?: when (style) {
        CardStyle.Filled -> MaterialTheme.colorScheme.surfaceColorAtElevation(
            WormaCeptorDesignSystem.Elevation.sm,
        )
        CardStyle.Outlined -> MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = WormaCeptorDesignSystem.Alpha.SUBTLE,
        )
        CardStyle.Elevated -> MaterialTheme.colorScheme.surfaceColorAtElevation(
            WormaCeptorDesignSystem.Elevation.md,
        )
    }

    val border = when (style) {
        CardStyle.Outlined -> BorderStroke(
            width = WormaCeptorDesignSystem.BorderWidth.thin,
            color = borderColor ?: MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = WormaCeptorDesignSystem.Alpha.MEDIUM,
            ),
        )
        else -> null
    }

    val elevation = when (style) {
        CardStyle.Elevated -> CardDefaults.cardElevation(
            defaultElevation = WormaCeptorDesignSystem.Elevation.md,
        )
        else -> CardDefaults.cardElevation(
            defaultElevation = WormaCeptorDesignSystem.Elevation.xs,
        )
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = elevation,
        border = border,
        content = content,
    )
}

@Preview(name = "Card - Light")
@Composable
private fun WormaCeptorCardPreview() {
    WormaCeptorTheme {
        Surface {
            Column(
                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                WormaCeptorCard {
                    Text("Filled card", modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg))
                }
                WormaCeptorCard(style = CardStyle.Outlined) {
                    Text("Outlined card", modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg))
                }
                WormaCeptorCard(style = CardStyle.Elevated) {
                    Text("Elevated card", modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg))
                }
            }
        }
    }
}

@Preview(name = "Card - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WormaCeptorCardDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            Column(
                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                WormaCeptorCard {
                    Text("Filled card", modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg))
                }
                WormaCeptorCard(style = CardStyle.Outlined) {
                    Text("Outlined card", modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg))
                }
                WormaCeptorCard(style = CardStyle.Elevated) {
                    Text("Elevated card", modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg))
                }
            }
        }
    }
}

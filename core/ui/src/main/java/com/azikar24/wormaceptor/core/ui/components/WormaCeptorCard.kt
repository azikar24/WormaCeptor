package com.azikar24.wormaceptor.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

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
 * When [onClick] is provided, the card becomes interactive with optional long-press support.
 * Haptic feedback is provided automatically on long press.
 *
 * @param modifier Modifier for the card
 * @param onClick Optional tap callback (card is static when null)
 * @param onLongClick Optional long-press callback
 * @param style Visual style variant
 * @param shape Card shape (defaults to [WormaCeptorTokens.Shapes.card])
 * @param backgroundColor Optional override for background color
 * @param borderColor Optional override for border color (Outlined style only)
 * @param enabled Whether the card responds to interactions
 * @param content Card body content
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WormaCeptorCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    style: CardStyle = CardStyle.Filled,
    shape: Shape = WormaCeptorTokens.Shapes.card,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    val containerColor = backgroundColor ?: when (style) {
        CardStyle.Filled -> MaterialTheme.colorScheme.surfaceColorAtElevation(
            WormaCeptorTokens.Elevation.sm,
        )
        CardStyle.Outlined -> MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = WormaCeptorTokens.Alpha.SUBTLE,
        )
        CardStyle.Elevated -> MaterialTheme.colorScheme.surfaceColorAtElevation(
            WormaCeptorTokens.Elevation.md,
        )
    }

    val border = when (style) {
        CardStyle.Outlined -> BorderStroke(
            width = WormaCeptorTokens.BorderWidth.thin,
            color = borderColor ?: MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = WormaCeptorTokens.Alpha.MEDIUM,
            ),
        )
        else -> null
    }

    val elevation = when (style) {
        CardStyle.Elevated -> CardDefaults.cardElevation(
            defaultElevation = WormaCeptorTokens.Elevation.md,
        )
        CardStyle.Outlined -> CardDefaults.cardElevation()
        CardStyle.Filled -> CardDefaults.cardElevation(
            defaultElevation = WormaCeptorTokens.Elevation.xs,
        )
    }

    val clickModifier = if (onClick != null) {
        Modifier
            .clip(shape)
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick?.let {
                    {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        it()
                    }
                },
            )
    } else {
        Modifier
    }

    Card(
        modifier = modifier.then(clickModifier),
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
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorCard {
                    Text("Filled card", modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg))
                }
                WormaCeptorCard(style = CardStyle.Outlined) {
                    Text("Outlined card", modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg))
                }
                WormaCeptorCard(style = CardStyle.Elevated) {
                    Text("Elevated card", modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg))
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
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorCard {
                    Text("Filled card", modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg))
                }
                WormaCeptorCard(style = CardStyle.Outlined) {
                    Text("Outlined card", modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg))
                }
                WormaCeptorCard(style = CardStyle.Elevated) {
                    Text("Elevated card", modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg))
                }
            }
        }
    }
}

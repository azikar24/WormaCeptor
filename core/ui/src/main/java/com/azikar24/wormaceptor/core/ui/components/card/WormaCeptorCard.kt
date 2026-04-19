package com.azikar24.wormaceptor.core.ui.components.card

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.modifier.wormaceptorFocusRing
import com.azikar24.wormaceptor.core.ui.modifier.wormaceptorPressScale
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/** Default width of the accent stripe drawn on the left edge when set. */
private val AccentStripeWidth: Dp = 4.dp

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
 * @param shape Card shape (defaults to [WormaCeptorTokens.Shapes.card], or the
 *              asymmetric [WormaCeptorTokens.Shapes.accent] when style is Accent)
 * @param backgroundColor Optional override for background color
 * @param borderColor Optional override for border color (Outlined style only)
 * @param accentStripe Optional left-edge color stripe -- used to convey context
 *                     (HTTP method, severity, category) without adding UI chrome.
 *                     Follows the card's rounded corners.
 * @param enabled Whether the card responds to interactions
 * @param content Card body content
 */
@Suppress("LongMethod")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WormaCeptorCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    style: CardStyle = CardStyle.Filled,
    shape: Shape = if (style == CardStyle.Accent) {
        WormaCeptorTokens.Shapes.accent
    } else {
        WormaCeptorTokens.Shapes.card
    },
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    accentStripe: Color? = null,
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

        CardStyle.Accent -> MaterialTheme.colorScheme.surfaceColorAtElevation(
            WormaCeptorTokens.Elevation.sm,
        )
    }

    val border = BorderStroke(
        width = WormaCeptorTokens.BorderWidth.thin,
        color = borderColor ?: MaterialTheme.colorScheme.outlineVariant.copy(
            alpha = WormaCeptorTokens.Alpha.MEDIUM,
        ),
    ).takeIf { style == CardStyle.Outlined || style == CardStyle.Accent }

    val elevation = if (style == CardStyle.Elevated) {
        CardDefaults.cardElevation(
            defaultElevation = WormaCeptorTokens.Elevation.md,
        )
    } else {
        CardDefaults.cardElevation()
    }

    val interactionSource = remember { MutableInteractionSource() }
    val clickModifier = if (onClick != null) {
        Modifier
            .clip(shape)
            .wormaceptorPressScale(interactionSource)
            .wormaceptorFocusRing(interactionSource, shape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
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

    val stripeModifier = if (accentStripe != null) {
        Modifier.drawWithContent {
            drawContent()
            drawRect(
                color = accentStripe,
                size = Size(AccentStripeWidth.toPx(), size.height),
            )
        }
    } else {
        Modifier
    }

    Card(
        modifier = modifier.then(clickModifier).then(stripeModifier),
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
                WormaCeptorCard(
                    style = CardStyle.Accent,
                    accentStripe = WormaCeptorTokens.Colors.HttpMethod.post,
                ) {
                    Text("Accent card", modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg))
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
                WormaCeptorCard(
                    style = CardStyle.Accent,
                    accentStripe = WormaCeptorTokens.Colors.Status.red,
                ) {
                    Text("Accent card", modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg))
                }
            }
        }
    }
}

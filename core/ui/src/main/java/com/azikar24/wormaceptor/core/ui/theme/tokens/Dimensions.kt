@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicProperty")

package com.azikar24.wormaceptor.core.ui.theme.tokens

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object TokenSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}

object TokenRadius {
    val xs = 4.dp
    val sm = 6.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp

    @Suppress("MagicNumber")
    val pill = 999.dp
}

object TokenElevation {
    val xs = 1.dp
    val sm = 2.dp
    val md = 4.dp
    val lg = 6.dp
    val xl = 8.dp
    val fab = md
}

object TokenBorderWidth {
    val thin = 0.5.dp
    val regular = 1.dp
    val thick = 2.dp
    val bold = 3.dp
}

object TokenAlpha {
    const val HINT = 0.04f
    const val SUBTLE = 0.08f
    const val LIGHT = 0.12f
    const val SOFT = 0.16f
    const val MEDIUM = 0.20f
    const val MODERATE = 0.32f
    const val STRONG = 0.40f
    const val BOLD = 0.50f
    const val INTENSE = 0.60f
    const val HEAVY = 0.72f
    const val PROMINENT = 0.87f
    const val OPAQUE = 1.0f
}

object TokenAnimation {
    const val ULTRA_FAST = 100
    const val FAST = 150
    const val MEDIUM = 200
    const val NORMAL = 250
    const val SLOW = 350
    const val VERY_SLOW = 500
    const val PAGE = 300
}

object TokenAnimations {
    val expandFadeIn: EnterTransition = expandVertically(
        animationSpec = tween(TokenAnimation.FAST, easing = TokenEasing.standardAccelerate),
    ) + fadeIn(animationSpec = tween(TokenAnimation.FAST, easing = TokenEasing.standardAccelerate))

    val shrinkFadeOut: ExitTransition = shrinkVertically(
        animationSpec = tween(TokenAnimation.FAST, easing = TokenEasing.standardAccelerate),
    ) + fadeOut(animationSpec = tween(TokenAnimation.FAST, easing = TokenEasing.standardAccelerate))
}

object TokenIconSize {
    val xxs = 12.dp
    val xs = 14.dp
    val sm = 16.dp
    val md = 20.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 40.dp
    val xxxl = 48.dp
}

object TokenTouchTarget {
    val minimum = 44.dp
    val comfortable = 48.dp
    val large = 56.dp
}

object TokenComponentSize {
    val textAreaHeight = 120.dp
    val progressBarHeight = 6.dp
    val toolTileHeight = 116.dp
    val dot = 6.dp
    val dotInset = 1.dp
    val templateCardWidth = 260.dp
    val chartHeight = 200.dp
    val gaugeContainerHeight = 200.dp
    val gaugeSize = 160.dp
    val gaugeStrokeWidth = 16.dp
    val perCoreLabelWidth = 56.dp
    val perCorePercentageWidth = 36.dp
    val tableCellMinWidth = 100.dp
    val tableCellMaxWidth = 200.dp
    val treeIndent = 14.dp
    val iconButtonSmall = 36.dp
    val pullRefreshThreshold = 80.dp
    val pullRefreshIndicator = 40.dp
    val pullRefreshStroke = 2.5.dp
}

/**
 * WormaCeptor delegates hover/pressed/drag feedback to Compose's [LocalIndication]
 * (Material ripple), so only focus and disabled overlays live here. Reintroduce
 * HOVER/PRESSED/DRAGGED if a custom indication ever ships.
 */
object TokenStateLayer {
    const val FOCUS = 0.12f
    const val DISABLED_CONTAINER = 0.12f
    const val DISABLED_CONTENT = 0.38f
}

object TokenFocusIndicator {
    val width = 2.dp
    val widthBold = 3.dp
    val offset = 2.dp
}

/**
 * Density multipliers applied via `Dp.scaled()`. Override at the [WormaCeptorTheme]
 * boundary to retune a subtree without forking component APIs.
 */
enum class TokenDensity(val scale: Float) {
    @Suppress("MagicNumber")
    Compact(0.85f),
    Default(1.0f),

    @Suppress("MagicNumber")
    Expanded(1.15f),
}

@Suppress("MagicNumber")
object TokenEasing {
    val standard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val standardDecelerate: Easing = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
    val standardAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)
    val emphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
}

object TokenShapes {
    val card = RoundedCornerShape(TokenRadius.md)
    val cardLarge = RoundedCornerShape(TokenRadius.lg)
    val cardExtraLarge = RoundedCornerShape(TokenRadius.xl)
    val button = RoundedCornerShape(TokenRadius.sm)
    val chip = RoundedCornerShape(TokenRadius.xs)
    val badge = RoundedCornerShape(TokenRadius.xs)
    val textField = RoundedCornerShape(TokenRadius.sm)
    val sheet = RoundedCornerShape(topStart = TokenRadius.xl, topEnd = TokenRadius.xl)
    val fab = RoundedCornerShape(TokenRadius.lg)
    val pill = RoundedCornerShape(TokenRadius.pill)

    /** Signature asymmetric shape -- one large rounded corner (top-start), three small. */
    val accent = RoundedCornerShape(
        topStart = TokenRadius.lg,
        topEnd = TokenRadius.xs,
        bottomStart = TokenRadius.xs,
        bottomEnd = TokenRadius.xs,
    )
}

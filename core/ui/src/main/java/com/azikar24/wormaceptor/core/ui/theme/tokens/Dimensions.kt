package com.azikar24.wormaceptor.core.ui.theme.tokens

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Spacing scale -- 4px baseline grid for consistent spacing.
 * Relocated from [WormaCeptorDesignSystem.Spacing].
 */
object TokenSpacing {
    /** Extra-extra-small spacing (2dp). */
    val xxs = 2.dp

    /** Extra-small spacing (4dp). */
    val xs = 4.dp

    /** Small spacing (8dp). */
    val sm = 8.dp

    /** Medium/default spacing (12dp). */
    val md = 12.dp

    /** Large spacing (16dp). */
    val lg = 16.dp

    /** Extra-large spacing (24dp). */
    val xl = 24.dp

    /** Extra-extra-large spacing (32dp). */
    val xxl = 32.dp

    /** Extra-extra-extra-large spacing (48dp). */
    val xxxl = 48.dp
}

/**
 * Corner radius tokens -- subtle rounded corners for modern feel.
 * Relocated from [WormaCeptorDesignSystem.CornerRadius].
 */
object TokenRadius {
    /** Extra-small corner radius (4dp). */
    val xs = 4.dp

    /** Small corner radius (6dp). */
    val sm = 6.dp

    /** Medium corner radius (8dp). */
    val md = 8.dp

    /** Large corner radius (12dp). */
    val lg = 12.dp

    /** Extra-large corner radius (16dp). */
    val xl = 16.dp

    /** Fully rounded pill shape (999dp). */
    @Suppress("MagicNumber")
    val pill = 999.dp
}

/**
 * Elevation tokens -- minimal, subtle elevation with layered depth.
 * Relocated from [WormaCeptorDesignSystem.Elevation].
 */
object TokenElevation {
    /** Extra-small elevation (1dp). */
    val xs = 1.dp

    /** Small elevation (2dp). */
    val sm = 2.dp

    /** Medium elevation (4dp). */
    val md = 4.dp

    /** Large elevation (6dp). */
    val lg = 6.dp

    /** Extra-large elevation (8dp). */
    val xl = 8.dp

    /** Floating action button elevation. */
    val fab = md
}

/**
 * Border width tokens -- consistent border styling.
 * Relocated from [WormaCeptorDesignSystem.BorderWidth].
 */
object TokenBorderWidth {
    /** Thin hairline border (0.5dp). */
    val thin = 0.5.dp

    /** Regular standard border (1dp). */
    val regular = 1.dp

    /** Thick emphasized border (2dp). */
    val thick = 2.dp

    /** Bold heavy border (3dp). */
    val bold = 3.dp
}

/**
 * Opacity level constants -- for subtle backgrounds and overlays.
 * Relocated from [WormaCeptorDesignSystem.Alpha].
 */
object TokenAlpha {
    /** Near-invisible overlay (4%). */
    const val HINT = 0.04f

    /** Barely visible overlay (8%). */
    const val SUBTLE = 0.08f

    /** Light tint overlay (12%). */
    const val LIGHT = 0.12f

    /** Soft overlay (16%). */
    const val SOFT = 0.16f

    /** Medium overlay (20%). */
    const val MEDIUM = 0.20f

    /** Moderate overlay (32%). */
    const val MODERATE = 0.32f

    /** Strong overlay (40%). */
    const val STRONG = 0.40f

    /** Bold overlay (50%). */
    const val BOLD = 0.50f

    /** Intense heavy overlay (60%). */
    const val INTENSE = 0.60f

    /** Heavy emphasis overlay (72%). */
    const val HEAVY = 0.72f

    /** Near-opaque overlay (87%). */
    const val PROMINENT = 0.87f

    /** Fully opaque (100%). */
    const val OPAQUE = 1.0f
}

/**
 * Animation duration constants -- following Material Motion guidelines.
 * Relocated from [WormaCeptorDesignSystem.AnimationDuration].
 */
object TokenAnimation {
    /** Ultra-fast animation duration (100ms). */
    const val ULTRA_FAST = 100

    /** Fast animation duration (150ms). */
    const val FAST = 150

    /** Normal/default animation duration (250ms). */
    const val NORMAL = 250

    /** Slow animation duration (350ms). */
    const val SLOW = 350

    /** Very slow animation duration (500ms). */
    const val VERY_SLOW = 500

    /** Page transition animation duration (300ms). */
    const val PAGE = 300
}

/**
 * Reusable animation enter/exit transitions.
 * Relocated from [WormaCeptorDesignSystem.Animations].
 */
object TokenAnimations {
    /** Standard enter transition: expand vertically + fade in. */
    val expandFadeIn: EnterTransition
        get() = expandVertically(
            animationSpec = tween(TokenAnimation.NORMAL),
        ) + fadeIn(animationSpec = tween(TokenAnimation.FAST))

    /** Standard exit transition: shrink vertically + fade out. */
    val shrinkFadeOut: ExitTransition
        get() = shrinkVertically(
            animationSpec = tween(TokenAnimation.FAST),
        ) + fadeOut(animationSpec = tween(TokenAnimation.FAST))
}

/**
 * Icon size tokens -- consistent icon sizing.
 * Relocated from [WormaCeptorDesignSystem.IconSize].
 */
object TokenIconSize {
    /** Extra-extra-small icon (12dp). */
    val xxs = 12.dp

    /** Extra-small icon (14dp). */
    val xs = 14.dp

    /** Small icon (16dp). */
    val sm = 16.dp

    /** Medium icon (20dp). */
    val md = 20.dp

    /** Large/default icon (24dp). */
    val lg = 24.dp

    /** Extra-large icon (32dp). */
    val xl = 32.dp

    /** Extra-extra-large icon (40dp). */
    val xxl = 40.dp

    /** Extra-extra-extra-large icon (48dp). */
    val xxxl = 48.dp
}

/**
 * Touch target tokens -- minimum touch target sizes for accessibility.
 * Relocated from [WormaCeptorDesignSystem.TouchTarget].
 */
object TokenTouchTarget {
    /** Minimum accessible touch target (44dp). */
    val minimum = 44.dp

    /** Comfortable touch target (48dp). */
    val comfortable = 48.dp

    /** Large touch target (56dp). */
    val large = 56.dp
}

/**
 * Common shape tokens.
 * Relocated from [WormaCeptorDesignSystem.Shapes].
 */
object TokenShapes {
    /** Standard card shape with medium corner radius. */
    val card = RoundedCornerShape(TokenRadius.md)

    /** Large card shape with large corner radius. */
    val cardLarge = RoundedCornerShape(TokenRadius.lg)

    /** Button shape with small corner radius. */
    val button = RoundedCornerShape(TokenRadius.sm)

    /** Chip shape with extra-small corner radius. */
    val chip = RoundedCornerShape(TokenRadius.xs)

    /** Badge shape with extra-small corner radius. */
    val badge = RoundedCornerShape(TokenRadius.xs)

    /** Text field shape with small corner radius. */
    val textField = RoundedCornerShape(TokenRadius.sm)

    /** Bottom sheet shape with rounded top corners. */
    val sheet = RoundedCornerShape(topStart = TokenRadius.xl, topEnd = TokenRadius.xl)

    /** Floating action button shape with large corner radius. */
    val fab = RoundedCornerShape(TokenRadius.lg)

    /** Fully rounded pill shape. */
    val pill = RoundedCornerShape(TokenRadius.pill)
}

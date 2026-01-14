package com.azikar24.wormaceptor.feature.viewer.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modern Minimal Design System for WormaCeptor V2
 * Inspired by Linear, Notion, and Stripe - clean, spacious, high information density
 */
object WormaCeptorDesignSystem {

    // Spacing Scale - Consistent spacing throughout the app
    object Spacing {
        val xxs = 2.dp
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val xxl = 32.dp
        val xxxl = 48.dp
    }

    // Corner Radius - Subtle rounded corners
    object CornerRadius {
        val xs = 4.dp
        val sm = 6.dp
        val md = 8.dp
        val lg = 12.dp
        val xl = 16.dp
        val pill = 999.dp
    }

    // Elevation - Minimal, subtle elevation
    object Elevation {
        val none = 0.dp
        val xs = 1.dp
        val sm = 2.dp
        val md = 4.dp
        val lg = 6.dp
    }

    // Border Width
    object BorderWidth {
        val thin = 0.5.dp
        val regular = 1.dp
        val thick = 2.dp
    }

    // Opacity Levels - For subtle backgrounds and overlays
    object Alpha {
        const val subtle = 0.08f
        const val light = 0.12f
        const val medium = 0.20f
        const val strong = 0.40f
        const val intense = 0.60f
    }

    // Animation Durations
    object AnimationDuration {
        const val fast = 150
        const val normal = 250
        const val slow = 350
    }

    // Animation Specs
    object AnimationSpecs {
        val fastSpring = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )

        val normalSpring = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )

        val fastTween = tween<Float>(
            durationMillis = AnimationDuration.fast,
            easing = FastOutSlowInEasing
        )

        val normalTween = tween<Float>(
            durationMillis = AnimationDuration.normal,
            easing = FastOutSlowInEasing
        )
    }

    // Status Color Tints - Subtle background colors for status indicators
    fun Color.subtleTint(alpha: Float = Alpha.subtle): Color = this.copy(alpha = alpha)

    // Card Padding Presets
    object CardPadding {
        val compact = PaddingValues(Spacing.md)
        val regular = PaddingValues(Spacing.lg)
        val comfortable = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.xl)
    }

    // Common Shapes
    object Shapes {
        val card = RoundedCornerShape(CornerRadius.md)
        val button = RoundedCornerShape(CornerRadius.sm)
        val chip = RoundedCornerShape(CornerRadius.xs)
        val sheet = RoundedCornerShape(topStart = CornerRadius.xl, topEnd = CornerRadius.xl)
    }
}

/**
 * Extension functions for consistent styling
 */

// Apply subtle background tint with status color
fun Color.asSubtleBackground(): Color = this.copy(alpha = WormaCeptorDesignSystem.Alpha.subtle)

// Apply light background tint
fun Color.asLightBackground(): Color = this.copy(alpha = WormaCeptorDesignSystem.Alpha.light)

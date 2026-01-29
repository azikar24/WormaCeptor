/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern Minimal Design System for WormaCeptor V2
 * Inspired by Linear, Notion, and Stripe - clean, spacious, high information density
 *
 * This is the shared design system used across all WormaCeptor modules.
 */
object WormaCeptorDesignSystem {

    /**
     * Theme Colors - Shared color palette for consistent theming across host app and library
     */
    object ThemeColors {
        // Light Theme
        val LightBackground = Color(0xFFFFFFFF)
        val LightSurface = Color(0xFFFAFAFA)
        val LightSurfaceVariant = Color(0xFFF0F0F0)
        val LightTextPrimary = Color(0xFF0A0A0A)
        val LightTextSecondary = Color(0xFF6B6B6B)
        val LightTextTertiary = Color(0xFF9CA3AF)
        val LightOutline = Color(0xFFE5E5E5)
        val LightOutlineVariant = Color(0xFFD4D4D4)

        // Dark Theme
        val DarkBackground = Color(0xFF0A0A0A)
        val DarkSurface = Color(0xFF141414)
        val DarkSurfaceVariant = Color(0xFF1F1F1F)
        val DarkTextPrimary = Color(0xFFFAFAFA)
        val DarkTextSecondary = Color(0xFF8A8A8A)
        val DarkTextTertiary = Color(0xFF525252)
        val DarkOutline = Color(0xFF2A2A2A)
        val DarkOutlineVariant = Color(0xFF3A3A3A)

        // Accent Colors - Teal
        val AccentLight = Color(0xFF0D9488)
        val AccentDark = Color(0xFF2DD4BF)
        val AccentSubtleLight = Color(0x120D9488)
        val AccentSubtleDark = Color(0x152DD4BF)

        // Semantic Colors
        val Error = Color(0xFFDC2626)
        val ErrorDark = Color(0xFFF87171)
        val Success = Color(0xFF16A34A)
        val SuccessDark = Color(0xFF4ADE80)
        val Warning = Color(0xFFD97706)
        val WarningDark = Color(0xFFFBBF24)
        val Info = Color(0xFF2563EB)
        val InfoDark = Color(0xFF60A5FA)
    }

    /**
     * Spacing Scale - 4px baseline grid for consistent spacing
     */
    object Spacing {
        val xxs = 2.dp
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val xxl = 32.dp
        val xxxl = 48.dp

        /** Safe area padding for edge-to-edge layouts */
        val safeArea = 16.dp
    }

    /**
     * Corner Radius - Subtle rounded corners for modern feel
     */
    object CornerRadius {
        val none = 0.dp
        val xs = 4.dp
        val sm = 6.dp
        val md = 8.dp
        val lg = 12.dp
        val xl = 16.dp
        val xxl = 20.dp
        val pill = 999.dp

        /** Get radius based on component size */
        fun forSize(size: Dp): Dp = when {
            size < 24.dp -> xs
            size < 48.dp -> sm
            size < 72.dp -> md
            else -> lg
        }
    }

    /**
     * Elevation - Minimal, subtle elevation with layered depth
     */
    object Elevation {
        val none = 0.dp
        val xs = 1.dp
        val sm = 2.dp
        val md = 4.dp
        val lg = 6.dp
        val xl = 8.dp
        val xxl = 12.dp

        /** Elevated surface for cards */
        val card = sm

        /** Modal/dialog elevation */
        val modal = lg

        /** Floating action button */
        val fab = md
    }

    /**
     * Border Width - Consistent border styling
     */
    object BorderWidth {
        val hairline = 0.5.dp
        val thin = 0.5.dp
        val regular = 1.dp
        val medium = 1.5.dp
        val thick = 2.dp
        val bold = 3.dp
    }

    /**
     * Opacity Levels - For subtle backgrounds and overlays
     */
    object Alpha {
        const val invisible = 0.0f
        const val hint = 0.04f
        const val subtle = 0.08f
        const val light = 0.12f
        const val soft = 0.16f
        const val medium = 0.20f
        const val moderate = 0.32f
        const val strong = 0.40f
        const val bold = 0.50f
        const val intense = 0.60f
        const val heavy = 0.72f
        const val prominent = 0.87f
        const val opaque = 1.0f

        /** Disabled state opacity */
        const val disabled = 0.38f

        /** Hover state overlay */
        const val hover = 0.08f

        /** Focus state overlay */
        const val focus = 0.12f

        /** Pressed state overlay */
        const val pressed = 0.12f

        /** Dragged state overlay */
        const val dragged = 0.16f
    }

    /**
     * Animation Durations - Following Material Motion guidelines
     */
    object AnimationDuration {
        const val instant = 0
        const val ultraFast = 100
        const val fast = 150
        const val normal = 250
        const val slow = 350
        const val verySlow = 500
        const val deliberate = 700

        /** Micro-interactions like checkboxes, toggles */
        const val micro = 150

        /** Standard transitions */
        const val standard = 250

        /** Complex animations */
        const val complex = 350

        /** Page transitions */
        const val page = 300
    }

    /**
     * Animation Easing - Smooth, natural motion curves
     */
    object Easing {
        /** Standard easing for most animations */
        val standard = FastOutSlowInEasing

        /** Deceleration for entering elements */
        val decelerate = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

        /** Acceleration for exiting elements */
        val accelerate = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)

        /** Emphasized easing for important transitions */
        val emphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

        /** Linear for continuous animations */
        val linear = CubicBezierEasing(0.0f, 0.0f, 1.0f, 1.0f)
    }

    /**
     * Animation Specs - Pre-configured animation specifications
     */
    object AnimationSpecs {
        val fastSpring = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        )

        val normalSpring = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        )

        val gentleSpring = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        )

        val snappySpring = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh,
        )

        val fastTween = tween<Float>(
            durationMillis = AnimationDuration.fast,
            easing = FastOutSlowInEasing,
        )

        val normalTween = tween<Float>(
            durationMillis = AnimationDuration.normal,
            easing = FastOutSlowInEasing,
        )

        val slowTween = tween<Float>(
            durationMillis = AnimationDuration.slow,
            easing = FastOutSlowInEasing,
        )
    }

    /**
     * Icon Sizes - Consistent icon sizing
     */
    object IconSize {
        val xxs = 12.dp
        val xs = 14.dp
        val sm = 16.dp
        val md = 20.dp
        val lg = 24.dp
        val xl = 32.dp
        val xxl = 40.dp
        val xxxl = 48.dp

        /** Inline icons in text */
        val inline = 16.dp

        /** Standard action icons */
        val action = 24.dp

        /** Navigation icons */
        val navigation = 24.dp

        /** Feature/category icons */
        val feature = 32.dp
    }

    /**
     * Touch Target - Minimum touch target sizes for accessibility
     */
    object TouchTarget {
        val minimum = 44.dp
        val comfortable = 48.dp
        val large = 56.dp
    }

    /**
     * Typography - Text styles for consistent hierarchy
     */
    object Typography {
        /** Display - Large, impactful text */
        val displayLarge = TextStyle(
            fontSize = 57.sp,
            lineHeight = 64.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = (-0.25).sp,
        )

        val displayMedium = TextStyle(
            fontSize = 45.sp,
            lineHeight = 52.sp,
            fontWeight = FontWeight.Normal,
        )

        val displaySmall = TextStyle(
            fontSize = 36.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.Normal,
        )

        /** Headline - Section headers */
        val headlineLarge = TextStyle(
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Medium,
        )

        val headlineMedium = TextStyle(
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Medium,
        )

        val headlineSmall = TextStyle(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Medium,
        )

        /** Title - Component headers */
        val titleLarge = TextStyle(
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.SemiBold,
        )

        val titleMedium = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.15.sp,
        )

        val titleSmall = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.1.sp,
        )

        /** Body - Main content text */
        val bodyLarge = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.5.sp,
        )

        val bodyMedium = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.25.sp,
        )

        val bodySmall = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.4.sp,
        )

        /** Label - UI labels and captions */
        val labelLarge = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.1.sp,
        )

        val labelMedium = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
        )

        val labelSmall = TextStyle(
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
        )

        /** Code - Monospace for code display */
        val codeLarge = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
        )

        val codeMedium = TextStyle(
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
        )

        val codeSmall = TextStyle(
            fontSize = 10.sp,
            lineHeight = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
        )

        /** Overline - Category labels */
        val overline = TextStyle(
            fontSize = 10.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )

        /** Caption - Small descriptive text */
        val caption = TextStyle(
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.4.sp,
        )
    }

    /**
     * Status Color Tints - Subtle background colors for status indicators
     */
    fun Color.subtleTint(alpha: Float = Alpha.subtle): Color = this.copy(alpha = alpha)

    /**
     * Card Padding Presets
     */
    object CardPadding {
        val tight = PaddingValues(Spacing.sm)
        val compact = PaddingValues(Spacing.md)
        val regular = PaddingValues(Spacing.lg)
        val comfortable = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.xl)
        val spacious = PaddingValues(Spacing.xl)
    }

    /**
     * Common Shapes
     */
    object Shapes {
        val card = RoundedCornerShape(CornerRadius.md)
        val cardLarge = RoundedCornerShape(CornerRadius.lg)
        val button = RoundedCornerShape(CornerRadius.sm)
        val buttonLarge = RoundedCornerShape(CornerRadius.md)
        val chip = RoundedCornerShape(CornerRadius.xs)
        val badge = RoundedCornerShape(CornerRadius.xs)
        val searchBar = RoundedCornerShape(CornerRadius.lg)
        val textField = RoundedCornerShape(CornerRadius.sm)
        val sheet = RoundedCornerShape(topStart = CornerRadius.xl, topEnd = CornerRadius.xl)
        val dialog = RoundedCornerShape(CornerRadius.xxl)
        val fab = RoundedCornerShape(CornerRadius.lg)
        val pill = RoundedCornerShape(CornerRadius.pill)
    }

    /**
     * Gradient Helpers - Create consistent gradients
     */
    object Gradients {
        /** Subtle vertical gradient for cards */
        fun cardSurface(baseColor: Color): Brush = Brush.verticalGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.97f),
                baseColor,
            ),
        )

        /** Horizontal progress gradient */
        fun progress(color: Color): Brush = Brush.horizontalGradient(
            colors = listOf(
                color.copy(alpha = 0.8f),
                color,
            ),
        )

        /** Radial glow effect */
        fun glow(color: Color): Brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.15f),
                color.copy(alpha = 0.05f),
            ),
        )

        /** Shimmer effect for loading states */
        fun shimmer(baseColor: Color): Brush = Brush.horizontalGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.6f),
                baseColor.copy(alpha = 0.9f),
                baseColor.copy(alpha = 0.6f),
            ),
        )
    }
}

/**
 * Apply subtle background tint with status color
 */
fun Color.asSubtleBackground(): Color = this.copy(alpha = WormaCeptorDesignSystem.Alpha.subtle)

/**
 * Apply light background tint
 */
fun Color.asLightBackground(): Color = this.copy(alpha = WormaCeptorDesignSystem.Alpha.light)

/**
 * Apply soft background tint
 */
fun Color.asSoftBackground(): Color = this.copy(alpha = WormaCeptorDesignSystem.Alpha.soft)

/**
 * Apply medium background tint
 */
fun Color.asMediumBackground(): Color = this.copy(alpha = WormaCeptorDesignSystem.Alpha.medium)

/**
 * Apply disabled state
 */
fun Color.asDisabled(): Color = this.copy(alpha = WormaCeptorDesignSystem.Alpha.disabled)

/**
 * Apply hover overlay
 */
fun Color.withHoverOverlay(): Color = this.copy(alpha = WormaCeptorDesignSystem.Alpha.hover)

/**
 * Apply pressed overlay
 */
fun Color.withPressedOverlay(): Color = this.copy(alpha = WormaCeptorDesignSystem.Alpha.pressed)

package com.azikar24.wormaceptor.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

        // Dark Theme
        val DarkBackground = Color(0xFF0A0A0A)
        val DarkSurface = Color(0xFF141414)
        val DarkSurfaceVariant = Color(0xFF1F1F1F)
        val DarkTextPrimary = Color(0xFFFAFAFA)
        val DarkTextSecondary = Color(0xFF8A8A8A)
        val DarkTextTertiary = Color(0xFF525252)

        // Accent Colors - Teal
        val AccentLight = Color(0xFF0D9488)
        val AccentDark = Color(0xFF2DD4BF)
        val AccentSubtleLight = Color(0x120D9488)
        val AccentSubtleDark = Color(0x152DD4BF)

        // Semantic Colors
        val Error = Color(0xFFDC2626)
        val ErrorDark = Color(0xFFF87171)
        val Success = Color(0xFF16A34A)
        val Warning = Color(0xFFD97706)
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
    }

    /**
     * Corner Radius - Subtle rounded corners for modern feel
     */
    object CornerRadius {
        val xs = 4.dp
        val sm = 6.dp
        val md = 8.dp
        val lg = 12.dp
        val xl = 16.dp
        val pill = 999.dp
    }

    /**
     * Elevation - Minimal, subtle elevation with layered depth
     */
    object Elevation {
        val xs = 1.dp
        val sm = 2.dp
        val md = 4.dp
        val lg = 6.dp
        val xl = 8.dp

        /** Floating action button */
        val fab = md
    }

    /**
     * Border Width - Consistent border styling
     */
    object BorderWidth {
        val thin = 0.5.dp
        val regular = 1.dp
        val thick = 2.dp
        val bold = 3.dp
    }

    /**
     * Opacity Levels - For subtle backgrounds and overlays
     */
    object Alpha {
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
    }

    /**
     * Animation Durations - Following Material Motion guidelines
     */
    object AnimationDuration {
        const val ultraFast = 100
        const val fast = 150
        const val normal = 250
        const val slow = 350
        const val verySlow = 500

        /** Page transitions */
        const val page = 300
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
        /** Body - Main content text */
        val bodyMedium = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.25.sp,
        )

        /** Label - UI labels and captions */
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
    }

    /**
     * Common Shapes
     */
    object Shapes {
        val card = RoundedCornerShape(CornerRadius.md)
        val cardLarge = RoundedCornerShape(CornerRadius.lg)
        val button = RoundedCornerShape(CornerRadius.sm)
        val chip = RoundedCornerShape(CornerRadius.xs)
        val badge = RoundedCornerShape(CornerRadius.xs)
        val searchBar = RoundedCornerShape(CornerRadius.lg)
        val textField = RoundedCornerShape(CornerRadius.sm)
        val sheet = RoundedCornerShape(topStart = CornerRadius.xl, topEnd = CornerRadius.xl)
        val fab = RoundedCornerShape(CornerRadius.lg)
        val pill = RoundedCornerShape(CornerRadius.pill)
    }
}

/**
 * Apply subtle background tint with status color
 */
fun Color.asSubtleBackground(): Color = this.copy(alpha = WormaCeptorDesignSystem.Alpha.subtle)

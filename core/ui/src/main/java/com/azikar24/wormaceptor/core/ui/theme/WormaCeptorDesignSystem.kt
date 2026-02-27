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
     * Theme Colors - Shared color palette for consistent theming across host app and library.
     */
    object ThemeColors {
        /** Light theme background color. */
        val LightBackground = Color(0xFFFFFFFF)

        /** Light theme surface color. */
        val LightSurface = Color(0xFFFAFAFA)

        /** Light theme surface variant color. */
        val LightSurfaceVariant = Color(0xFFF0F0F0)

        /** Light theme primary text color. */
        val LightTextPrimary = Color(0xFF0A0A0A)

        /** Light theme secondary text color. */
        val LightTextSecondary = Color(0xFF6B6B6B)

        /** Light theme tertiary text color. */
        val LightTextTertiary = Color(0xFF9CA3AF)

        /** Dark theme background color. */
        val DarkBackground = Color(0xFF0A0A0A)

        /** Dark theme surface color. */
        val DarkSurface = Color(0xFF141414)

        /** Dark theme surface variant color. */
        val DarkSurfaceVariant = Color(0xFF1F1F1F)

        /** Dark theme primary text color. */
        val DarkTextPrimary = Color(0xFFFAFAFA)

        /** Dark theme secondary text color. */
        val DarkTextSecondary = Color(0xFF8A8A8A)

        /** Dark theme tertiary text color. */
        val DarkTextTertiary = Color(0xFF525252)

        /** Teal accent color for light theme. */
        val AccentLight = Color(0xFF0D9488)

        /** Teal accent color for dark theme. */
        val AccentDark = Color(0xFF2DD4BF)

        /** Subtle teal accent background for light theme. */
        val AccentSubtleLight = Color(0x120D9488)

        /** Subtle teal accent background for dark theme. */
        val AccentSubtleDark = Color(0x152DD4BF)

        /** Error color for light theme. */
        val Error = Color(0xFFDC2626)

        /** Error color for dark theme. */
        val ErrorDark = Color(0xFFF87171)

        /** Success semantic color. */
        val Success = Color(0xFF16A34A)

        /** Warning semantic color. */
        val Warning = Color(0xFFD97706)
    }

    /**
     * Spacing Scale - 4px baseline grid for consistent spacing.
     */
    object Spacing {
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
     * Corner Radius - Subtle rounded corners for modern feel.
     */
    object CornerRadius {
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
        val pill = 999.dp
    }

    /**
     * Elevation - Minimal, subtle elevation with layered depth.
     */
    object Elevation {
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
     * Border Width - Consistent border styling.
     */
    object BorderWidth {
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
     * Opacity Levels - For subtle backgrounds and overlays.
     */
    object Alpha {
        /** Near-invisible overlay (4%). */
        const val hint = 0.04f

        /** Barely visible overlay (8%). */
        const val subtle = 0.08f

        /** Light tint overlay (12%). */
        const val light = 0.12f

        /** Soft overlay (16%). */
        const val soft = 0.16f

        /** Medium overlay (20%). */
        const val medium = 0.20f

        /** Moderate overlay (32%). */
        const val moderate = 0.32f

        /** Strong overlay (40%). */
        const val strong = 0.40f

        /** Bold overlay (50%). */
        const val bold = 0.50f

        /** Intense heavy overlay (60%). */
        const val intense = 0.60f

        /** Heavy emphasis overlay (72%). */
        const val heavy = 0.72f

        /** Near-opaque overlay (87%). */
        const val prominent = 0.87f

        /** Fully opaque (100%). */
        const val opaque = 1.0f
    }

    /**
     * Animation Durations - Following Material Motion guidelines.
     */
    object AnimationDuration {
        /** Ultra-fast animation duration (100ms). */
        const val ultraFast = 100

        /** Fast animation duration (150ms). */
        const val fast = 150

        /** Normal/default animation duration (250ms). */
        const val normal = 250

        /** Slow animation duration (350ms). */
        const val slow = 350

        /** Very slow animation duration (500ms). */
        const val verySlow = 500

        /** Page transition animation duration (300ms). */
        const val page = 300
    }

    /**
     * Icon Sizes - Consistent icon sizing.
     */
    object IconSize {
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
     * Touch Target - Minimum touch target sizes for accessibility.
     */
    object TouchTarget {
        /** Minimum accessible touch target (44dp). */
        val minimum = 44.dp

        /** Comfortable touch target (48dp). */
        val comfortable = 48.dp

        /** Large touch target (56dp). */
        val large = 56.dp
    }

    /**
     * Typography - Text styles for consistent hierarchy.
     */
    object Typography {
        /** Body - Main content text. */
        val bodyMedium = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.25.sp,
        )

        /** Label - UI labels and captions. */
        val labelMedium = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
        )

        /** Small label for compact UI elements. */
        val labelSmall = TextStyle(
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
        )

        /** Code - Monospace for code display. */
        val codeMedium = TextStyle(
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
        )

        /** Small monospace text style for compact code display. */
        val codeSmall = TextStyle(
            fontSize = 10.sp,
            lineHeight = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
        )

        /** Overline - Category labels. */
        val overline = TextStyle(
            fontSize = 10.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )
    }

    /**
     * Common Shapes.
     */
    object Shapes {
        /** Standard card shape with medium corner radius. */
        val card = RoundedCornerShape(CornerRadius.md)

        /** Large card shape with large corner radius. */
        val cardLarge = RoundedCornerShape(CornerRadius.lg)

        /** Button shape with small corner radius. */
        val button = RoundedCornerShape(CornerRadius.sm)

        /** Chip shape with extra-small corner radius. */
        val chip = RoundedCornerShape(CornerRadius.xs)

        /** Badge shape with extra-small corner radius. */
        val badge = RoundedCornerShape(CornerRadius.xs)

        /** Search bar shape with large corner radius. */
        val searchBar = RoundedCornerShape(CornerRadius.lg)

        /** Text field shape with small corner radius. */
        val textField = RoundedCornerShape(CornerRadius.sm)

        /** Bottom sheet shape with rounded top corners. */
        val sheet = RoundedCornerShape(topStart = CornerRadius.xl, topEnd = CornerRadius.xl)

        /** Floating action button shape with large corner radius. */
        val fab = RoundedCornerShape(CornerRadius.lg)

        /** Fully rounded pill shape. */
        val pill = RoundedCornerShape(CornerRadius.pill)
    }
}

/**
 * Applies a subtle background tint with this status color.
 */
fun Color.asSubtleBackground(): Color = this.copy(alpha = WormaCeptorDesignSystem.Alpha.subtle)

/*
 * Gesture-specific colors and theme extensions for WormaCeptor
 *
 * Provides consistent colors for gesture-related UI elements:
 * - Pull-to-refresh states
 * - Swipe navigation edges
 * - Zoom controls
 * - Progress indicators
 */

package com.azikar24.wormaceptor.feature.viewer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Colors specifically for gesture-related UI components.
 * Designed to work harmoniously with the WormaCeptor design system.
 */
@Immutable
data class GestureColors(
    // Pull-to-refresh states
    val refreshPulling: Color,
    val refreshThreshold: Color,
    val refreshActive: Color,
    val refreshComplete: Color,

    // Swipe navigation
    val swipeEdgeGradientStart: Color,
    val swipeEdgeGradientEnd: Color,
    val swipeIndicatorActive: Color,
    val swipeIndicatorInactive: Color,

    // Zoom controls
    val zoomControlBackground: Color,
    val zoomControlBorder: Color,
    val zoomLevelNormal: Color,
    val zoomLevelZoomed: Color,

    // Position indicator
    val positionBackground: Color,
    val positionText: Color,
    val positionHighlight: Color,

    // Swipe-back
    val swipeBackShadow: Color,
    val swipeBackIndicator: Color,
    val swipeBackThreshold: Color,

    // Haptic feedback visual cues
    val hapticPulse: Color,
)

/**
 * Light theme gesture colors.
 * Uses WormaCeptorColors for consistency with the design system.
 */
val LightGestureColors = GestureColors(
    // Pull-to-refresh - using primary blue and status green
    refreshPulling = md_theme_light_primary,
    refreshThreshold = WormaCeptorColors.StatusGreen,
    refreshActive = md_theme_light_primary,
    refreshComplete = WormaCeptorColors.StatusGreen,

    // Swipe navigation - subtle edge hints using primary
    swipeEdgeGradientStart = md_theme_light_primary.copy(alpha = 0.10f),
    swipeEdgeGradientEnd = Color.Transparent,
    swipeIndicatorActive = md_theme_light_primary,
    swipeIndicatorInactive = md_theme_light_primary.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),

    // Zoom controls - using surface colors
    zoomControlBackground = md_theme_light_surface,
    zoomControlBorder = md_theme_light_outline.copy(alpha = 0.10f),
    zoomLevelNormal = md_theme_light_onSurfaceVariant,
    zoomLevelZoomed = md_theme_light_primary,

    // Position indicator
    positionBackground = md_theme_light_surface,
    positionText = md_theme_light_onSurfaceVariant,
    positionHighlight = md_theme_light_primary,

    // Swipe-back
    swipeBackShadow = md_theme_light_scrim.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
    swipeBackIndicator = md_theme_light_surface,
    swipeBackThreshold = md_theme_light_primary,

    // Haptic feedback
    hapticPulse = md_theme_light_primary.copy(alpha = 0.10f),
)

/**
 * Dark theme gesture colors.
 * Uses WormaCeptorColors for consistency with the design system.
 */
val DarkGestureColors = GestureColors(
    // Pull-to-refresh - using primary and status colors
    refreshPulling = md_theme_dark_primary,
    refreshThreshold = WormaCeptorColors.StatusGreen,
    refreshActive = md_theme_dark_primary,
    refreshComplete = WormaCeptorColors.StatusGreen,

    // Swipe navigation
    swipeEdgeGradientStart = md_theme_dark_primary.copy(alpha = 0.15f),
    swipeEdgeGradientEnd = Color.Transparent,
    swipeIndicatorActive = md_theme_dark_primary,
    swipeIndicatorInactive = md_theme_dark_primary.copy(alpha = 0.25f),

    // Zoom controls - using surface colors
    zoomControlBackground = md_theme_dark_surface,
    zoomControlBorder = md_theme_dark_outline.copy(alpha = 0.10f),
    zoomLevelNormal = md_theme_dark_onSurfaceVariant,
    zoomLevelZoomed = md_theme_dark_primary,

    // Position indicator
    positionBackground = md_theme_dark_surface,
    positionText = md_theme_dark_onSurfaceVariant,
    positionHighlight = md_theme_dark_primary,

    // Swipe-back
    swipeBackShadow = md_theme_dark_scrim.copy(alpha = WormaCeptorDesignSystem.Alpha.strong),
    swipeBackIndicator = md_theme_dark_surfaceVariant,
    swipeBackThreshold = md_theme_dark_primary,

    // Haptic feedback
    hapticPulse = md_theme_dark_primary.copy(alpha = 0.15f),
)

/**
 * CompositionLocal for providing gesture colors throughout the app.
 */
val LocalGestureColors = staticCompositionLocalOf { LightGestureColors }

/**
 * Returns the appropriate gesture colors based on the current theme.
 */
@Composable
fun gestureColors(darkTheme: Boolean = isSystemInDarkTheme()): GestureColors {
    return if (darkTheme) DarkGestureColors else LightGestureColors
}

/**
 * Extension to get gesture colors from MaterialTheme context.
 * Usage: MaterialTheme.gestureColors.refreshPulling
 */
val MaterialTheme.gestureColors: GestureColors
    @Composable
    get() = gestureColors()

// =============================================================================
// ADDITIONAL COLOR UTILITIES
// =============================================================================

/**
 * Creates a gradient for swipe edge effects.
 */
@Composable
fun swipeEdgeGradient(isLeftEdge: Boolean): List<Color> {
    val colors = gestureColors()
    return if (isLeftEdge) {
        listOf(colors.swipeEdgeGradientStart, colors.swipeEdgeGradientEnd)
    } else {
        listOf(colors.swipeEdgeGradientEnd, colors.swipeEdgeGradientStart)
    }
}

/**
 * Creates appropriate alpha values for different gesture states.
 */
object GestureAlpha {
    const val IDLE = 0f
    const val SUBTLE = 0.1f
    const val HINT = 0.3f
    const val ACTIVE = 0.6f
    const val FULL = 1f
}

/**
 * Durations specifically tuned for gesture animations.
 * Based on WormaCeptorDesignSystem.AnimationDuration with gesture-specific variants.
 * Gestures need slightly faster feedback for responsive feel.
 */
object GestureAnimationDuration {
    const val INSTANT = 50
    const val SNAP = 100
    const val QUICK = WormaCeptorDesignSystem.AnimationDuration.fast // 150ms
    const val NORMAL = 200
    const val SMOOTH = WormaCeptorDesignSystem.AnimationDuration.slow // 350ms
}

/**
 * Spring configurations for gesture animations.
 */
object GestureSpring {
    // For snappy interactions like tap responses
    val snappy = androidx.compose.animation.core.spring<Float>(
        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
        stiffness = androidx.compose.animation.core.Spring.StiffnessHigh,
    )

    // For smooth drag release animations
    val smooth = androidx.compose.animation.core.spring<Float>(
        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
        stiffness = androidx.compose.animation.core.Spring.StiffnessMedium,
    )

    // For bouncy feedback animations
    val bouncy = androidx.compose.animation.core.spring<Float>(
        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
        stiffness = androidx.compose.animation.core.Spring.StiffnessLow,
    )

    // For quick snapping animations
    val snap = androidx.compose.animation.core.spring<Float>(
        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
        stiffness = androidx.compose.animation.core.Spring.StiffnessHigh,
    )
}

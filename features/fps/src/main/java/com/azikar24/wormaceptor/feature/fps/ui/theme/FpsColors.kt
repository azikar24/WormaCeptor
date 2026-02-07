package com.azikar24.wormaceptor.feature.fps.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * FPS status colors for visual feedback based on frame rate.
 *
 * Color scheme:
 * - Good (>= 55 FPS): Green - excellent performance
 * - Warning (30-54 FPS): Yellow/Amber - moderate performance
 * - Critical (< 30 FPS): Red - poor performance
 */
@Immutable
data class FpsColors(
    val good: Color,
    val warning: Color,
    val critical: Color,
    val goodBackground: Color,
    val warningBackground: Color,
    val criticalBackground: Color,
    val chartLine: Color,
    val chartFill: Color,
    val chartGrid: Color,
    val jankIndicator: Color,
) {
    /**
     * Returns the appropriate color based on FPS value.
     */
    fun forFps(fps: Float): Color = when {
        fps >= FPS_GOOD_THRESHOLD -> good
        fps >= FPS_WARNING_THRESHOLD -> warning
        else -> critical
    }

    /**
     * Returns the appropriate background color based on FPS value.
     */
    fun backgroundForFps(fps: Float): Color = when {
        fps >= FPS_GOOD_THRESHOLD -> goodBackground
        fps >= FPS_WARNING_THRESHOLD -> warningBackground
        else -> criticalBackground
    }

    companion object {
        const val FPS_GOOD_THRESHOLD = 55f
        const val FPS_WARNING_THRESHOLD = 30f
    }
}

/**
 * Light theme FPS colors.
 */
val LightFpsColors = FpsColors(
    good = Color(0xFF388E3C), // Green 700
    warning = Color(0xFFF57C00), // Orange 700
    critical = Color(0xFFD32F2F), // Red 700
    goodBackground = Color(0xFFE8F5E9), // Green 50
    warningBackground = Color(0xFFFFF3E0), // Orange 50
    criticalBackground = Color(0xFFFFEBEE), // Red 50
    chartLine = Color(0xFF1976D2), // Blue 700
    chartFill = Color(0xFF1976D2).copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
    chartGrid = Color(0xFFE0E0E0), // Gray 300
    jankIndicator = Color(0xFFE91E63), // Pink 500
)

/**
 * Dark theme FPS colors.
 */
val DarkFpsColors = FpsColors(
    good = Color(0xFF81C784), // Green 300
    warning = Color(0xFFFFB74D), // Orange 300
    critical = Color(0xFFE57373), // Red 300
    goodBackground = Color(0xFF1B5E20).copy(alpha = WormaCeptorDesignSystem.Alpha.medium), // Green with alpha
    warningBackground = Color(0xFFE65100).copy(alpha = WormaCeptorDesignSystem.Alpha.medium), // Orange with alpha
    criticalBackground = Color(0xFFB71C1C).copy(alpha = WormaCeptorDesignSystem.Alpha.medium), // Red with alpha
    chartLine = Color(0xFF64B5F6), // Blue 300
    chartFill = Color(0xFF64B5F6).copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
    chartGrid = Color(0xFF424242), // Gray 800
    jankIndicator = Color(0xFFF06292), // Pink 300
)

/**
 * Returns the appropriate FPS colors based on the current theme.
 */
@Composable
fun fpsColors(darkTheme: Boolean = isSystemInDarkTheme()): FpsColors {
    return if (darkTheme) DarkFpsColors else LightFpsColors
}

package com.azikar24.wormaceptor.feature.fps.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
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
    fun forFps(fps: Float): Color = when {
        fps >= FPS_GOOD_THRESHOLD -> good
        fps >= FPS_WARNING_THRESHOLD -> warning
        else -> critical
    }

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
 * Returns the appropriate FPS colors based on the current theme.
 * Delegates status colors to WormaCeptorColors and uses Material theme for surfaces.
 */
@Suppress("MagicNumber") // Chart-specific hex colors are domain-specific
@Composable
fun fpsColors(darkTheme: Boolean = isSystemInDarkTheme()): FpsColors {
    val outline = MaterialTheme.colorScheme.outline

    return FpsColors(
        good = WormaCeptorColors.StatusGreen,
        warning = WormaCeptorColors.StatusAmber,
        critical = WormaCeptorColors.StatusRed,
        goodBackground = WormaCeptorColors.StatusGreen.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
        warningBackground = WormaCeptorColors.StatusAmber.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
        criticalBackground = WormaCeptorColors.StatusRed.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
        chartLine = if (darkTheme) Color(0xFF64B5F6) else Color(0xFF1976D2),
        chartFill = (if (darkTheme) Color(0xFF64B5F6) else Color(0xFF1976D2))
            .copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
        chartGrid = outline.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
        jankIndicator = if (darkTheme) Color(0xFFF06292) else Color(0xFFE91E63),
    )
}

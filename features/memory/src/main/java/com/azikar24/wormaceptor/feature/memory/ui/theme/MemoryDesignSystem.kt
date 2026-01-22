/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.memory.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colors for the Memory Monitoring feature.
 *
 * Uses a consistent color scheme to represent different memory states:
 * - Normal: Green tones for healthy memory usage
 * - Warning: Amber/Orange tones for elevated usage
 * - Critical: Red tones for high usage (>80%)
 * - Native: Blue tones to distinguish from Java heap
 */
@Immutable
data class MemoryColors(
    // Chart colors
    val heapUsed: Color,
    val heapFree: Color,
    val heapTotal: Color,
    val nativeHeap: Color,

    // Status colors
    val normal: Color,
    val warning: Color,
    val critical: Color,

    // Background colors
    val cardBackground: Color,
    val chartBackground: Color,
    val gridLines: Color,

    // Text colors
    val labelPrimary: Color,
    val labelSecondary: Color,
    val valuePrimary: Color,
) {
    /**
     * Returns the appropriate status color based on heap usage percentage.
     */
    fun statusColorForUsage(usagePercent: Float): Color = when {
        usagePercent >= 80f -> critical
        usagePercent >= 60f -> warning
        else -> normal
    }
}

/**
 * Light theme memory colors.
 */
val LightMemoryColors = MemoryColors(
    // Chart colors - saturated for visibility
    heapUsed = Color(0xFF4CAF50), // Green 500
    heapFree = Color(0xFFC8E6C9), // Green 100
    heapTotal = Color(0xFF81C784), // Green 300
    nativeHeap = Color(0xFF2196F3), // Blue 500

    // Status colors
    normal = Color(0xFF4CAF50), // Green 500
    warning = Color(0xFFFF9800), // Orange 500
    critical = Color(0xFFF44336), // Red 500

    // Background colors
    cardBackground = Color(0xFFFAFAFA),
    chartBackground = Color(0xFFF5F5F5),
    gridLines = Color(0xFFE0E0E0),

    // Text colors
    labelPrimary = Color(0xFF212121),
    labelSecondary = Color(0xFF757575),
    valuePrimary = Color(0xFF424242),
)

/**
 * Dark theme memory colors.
 */
val DarkMemoryColors = MemoryColors(
    // Chart colors - slightly muted for dark backgrounds
    heapUsed = Color(0xFF66BB6A), // Green 400
    heapFree = Color(0xFF2E7D32), // Green 800
    heapTotal = Color(0xFF81C784), // Green 300
    nativeHeap = Color(0xFF42A5F5), // Blue 400

    // Status colors
    normal = Color(0xFF66BB6A), // Green 400
    warning = Color(0xFFFFB74D), // Orange 300
    critical = Color(0xFFEF5350), // Red 400

    // Background colors
    cardBackground = Color(0xFF1E1E1E),
    chartBackground = Color(0xFF2D2D2D),
    gridLines = Color(0xFF424242),

    // Text colors
    labelPrimary = Color(0xFFE0E0E0),
    labelSecondary = Color(0xFF9E9E9E),
    valuePrimary = Color(0xFFBDBDBD),
)

/**
 * Returns the appropriate memory colors based on the current theme.
 */
@Composable
fun memoryColors(darkTheme: Boolean = isSystemInDarkTheme()): MemoryColors {
    return if (darkTheme) DarkMemoryColors else LightMemoryColors
}

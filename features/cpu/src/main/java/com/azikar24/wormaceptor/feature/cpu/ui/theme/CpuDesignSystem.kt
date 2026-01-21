/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.cpu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colors for the CPU Monitoring feature.
 *
 * Uses a consistent color scheme to represent different CPU usage states:
 * - Normal (green): CPU usage < 50%
 * - Warning (amber/orange): CPU usage 50-80%
 * - Critical (red): CPU usage > 80%
 * - Per-core uses a gradient of colors for visual distinction
 */
@Immutable
data class CpuColors(
    // Chart colors
    val cpuUsage: Color,
    val cpuUsageLight: Color,
    val coreColors: List<Color>,

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

    // Gauge colors
    val gaugeBackground: Color,
    val gaugeTrack: Color,
) {
    /**
     * Returns the appropriate status color based on CPU usage percentage.
     */
    fun statusColorForUsage(usagePercent: Float): Color = when {
        usagePercent >= 80f -> critical
        usagePercent >= 50f -> warning
        else -> normal
    }

    /**
     * Returns a color for a specific core index.
     */
    fun colorForCore(index: Int): Color {
        return coreColors[index % coreColors.size]
    }
}

/**
 * Light theme CPU colors.
 */
val LightCpuColors = CpuColors(
    // Chart colors - saturated for visibility
    cpuUsage = Color(0xFF2196F3),        // Blue 500
    cpuUsageLight = Color(0xFFBBDEFB),   // Blue 100

    // Per-core colors - distinct colors for each core
    coreColors = listOf(
        Color(0xFF2196F3),  // Blue
        Color(0xFF4CAF50),  // Green
        Color(0xFFFF9800),  // Orange
        Color(0xFF9C27B0),  // Purple
        Color(0xFF00BCD4),  // Cyan
        Color(0xFFE91E63),  // Pink
        Color(0xFF607D8B),  // Blue Grey
        Color(0xFF795548),  // Brown
    ),

    // Status colors
    normal = Color(0xFF4CAF50),        // Green 500
    warning = Color(0xFFFF9800),       // Orange 500
    critical = Color(0xFFF44336),      // Red 500

    // Background colors
    cardBackground = Color(0xFFFAFAFA),
    chartBackground = Color(0xFFF5F5F5),
    gridLines = Color(0xFFE0E0E0),

    // Text colors
    labelPrimary = Color(0xFF212121),
    labelSecondary = Color(0xFF757575),
    valuePrimary = Color(0xFF424242),

    // Gauge colors
    gaugeBackground = Color(0xFFEEEEEE),
    gaugeTrack = Color(0xFFE0E0E0),
)

/**
 * Dark theme CPU colors.
 */
val DarkCpuColors = CpuColors(
    // Chart colors - slightly muted for dark backgrounds
    cpuUsage = Color(0xFF42A5F5),        // Blue 400
    cpuUsageLight = Color(0xFF1565C0),   // Blue 800

    // Per-core colors - slightly lighter for dark mode
    coreColors = listOf(
        Color(0xFF42A5F5),  // Blue
        Color(0xFF66BB6A),  // Green
        Color(0xFFFFB74D),  // Orange
        Color(0xFFAB47BC),  // Purple
        Color(0xFF26C6DA),  // Cyan
        Color(0xFFEC407A),  // Pink
        Color(0xFF78909C),  // Blue Grey
        Color(0xFF8D6E63),  // Brown
    ),

    // Status colors
    normal = Color(0xFF66BB6A),        // Green 400
    warning = Color(0xFFFFB74D),       // Orange 300
    critical = Color(0xFFEF5350),      // Red 400

    // Background colors
    cardBackground = Color(0xFF1E1E1E),
    chartBackground = Color(0xFF2D2D2D),
    gridLines = Color(0xFF424242),

    // Text colors
    labelPrimary = Color(0xFFE0E0E0),
    labelSecondary = Color(0xFF9E9E9E),
    valuePrimary = Color(0xFFBDBDBD),

    // Gauge colors
    gaugeBackground = Color(0xFF2D2D2D),
    gaugeTrack = Color(0xFF424242),
)

/**
 * Returns the appropriate CPU colors based on the current theme.
 */
@Composable
fun cpuColors(darkTheme: Boolean = isSystemInDarkTheme()): CpuColors {
    return if (darkTheme) DarkCpuColors else LightCpuColors
}

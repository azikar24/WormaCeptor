/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewborders.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Color palette for the View Borders feature.
 *
 * Standard color coding:
 * - Margin: Orange - visualizes the margin space outside the view
 * - Padding: Green - visualizes the padding space inside the view border
 * - Content: Blue - visualizes the actual content area
 */
@Immutable
data class ViewBordersColors(
    val margin: Color,
    val padding: Color,
    val content: Color,
    val marginBackground: Color,
    val paddingBackground: Color,
    val contentBackground: Color,
    val legendBorder: Color,
) {
    companion object {
        // Default colors with ~30% alpha for overlay
        val DefaultMargin = Color(0xFFFF9800) // Orange
        val DefaultPadding = Color(0xFF4CAF50) // Green
        val DefaultContent = Color(0xFF2196F3) // Blue
    }
}

/**
 * Light theme colors for View Borders.
 */
val LightViewBordersColors = ViewBordersColors(
    margin = Color(0xFFFF9800),          // Orange 500
    padding = Color(0xFF4CAF50),         // Green 500
    content = Color(0xFF2196F3),         // Blue 500
    marginBackground = Color(0xFFFFF3E0), // Orange 50
    paddingBackground = Color(0xFFE8F5E9), // Green 50
    contentBackground = Color(0xFFE3F2FD), // Blue 50
    legendBorder = Color(0xFFE0E0E0),    // Gray 300
)

/**
 * Dark theme colors for View Borders.
 */
val DarkViewBordersColors = ViewBordersColors(
    margin = Color(0xFFFFB74D),          // Orange 300
    padding = Color(0xFF81C784),         // Green 300
    content = Color(0xFF64B5F6),         // Blue 300
    marginBackground = Color(0xFFE65100).copy(alpha = 0.2f),  // Orange with alpha
    paddingBackground = Color(0xFF1B5E20).copy(alpha = 0.2f), // Green with alpha
    contentBackground = Color(0xFF0D47A1).copy(alpha = 0.2f), // Blue with alpha
    legendBorder = Color(0xFF424242),    // Gray 800
)

/**
 * Returns the appropriate View Borders colors based on the current theme.
 */
@Composable
fun viewBordersColors(darkTheme: Boolean = isSystemInDarkTheme()): ViewBordersColors {
    return if (darkTheme) DarkViewBordersColors else LightViewBordersColors
}

/**
 * Converts a Color to its ARGB Long representation.
 */
fun Color.toArgbLong(): Long {
    val alpha = (this.alpha * 255).toInt()
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return ((alpha.toLong() shl 24) or (red.toLong() shl 16) or (green.toLong() shl 8) or blue.toLong())
}

/**
 * Converts an ARGB Long value to a Compose Color.
 */
fun Long.toComposeColor(): Color {
    return Color(this.toInt())
}

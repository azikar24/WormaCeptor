/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.leakdetection.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity

/**
 * Colors for the Leak Detection feature.
 *
 * Uses a severity-based color scheme:
 * - Critical: Deep red tones for severe leaks requiring immediate attention
 * - High: Orange/amber tones for significant leaks
 * - Medium: Yellow tones for moderate leaks
 * - Low: Blue/teal tones for minor leaks
 */
@Immutable
data class LeakDetectionColors(
    // Severity colors
    val critical: Color,
    val criticalBackground: Color,
    val high: Color,
    val highBackground: Color,
    val medium: Color,
    val mediumBackground: Color,
    val low: Color,
    val lowBackground: Color,

    // Status colors
    val monitoring: Color,
    val idle: Color,

    // Background colors
    val cardBackground: Color,
    val surfaceBackground: Color,
    val detailBackground: Color,

    // Text colors
    val labelPrimary: Color,
    val labelSecondary: Color,
    val valuePrimary: Color,

    // Accent colors
    val actionButton: Color,
    val actionButtonText: Color,
    val divider: Color,
) {
    /**
     * Returns the appropriate color for a given severity level.
     */
    fun colorForSeverity(severity: LeakSeverity): Color = when (severity) {
        LeakSeverity.CRITICAL -> critical
        LeakSeverity.HIGH -> high
        LeakSeverity.MEDIUM -> medium
        LeakSeverity.LOW -> low
    }

    /**
     * Returns the appropriate background color for a given severity level.
     */
    fun backgroundForSeverity(severity: LeakSeverity): Color = when (severity) {
        LeakSeverity.CRITICAL -> criticalBackground
        LeakSeverity.HIGH -> highBackground
        LeakSeverity.MEDIUM -> mediumBackground
        LeakSeverity.LOW -> lowBackground
    }
}

/**
 * Light theme leak detection colors.
 */
val LightLeakDetectionColors = LeakDetectionColors(
    // Severity colors - vibrant for light backgrounds
    critical = Color(0xFFD32F2F), // Red 700
    criticalBackground = Color(0xFFFFEBEE), // Red 50
    high = Color(0xFFE65100), // Orange 800
    highBackground = Color(0xFFFFF3E0), // Orange 50
    medium = Color(0xFFF9A825), // Yellow 800
    mediumBackground = Color(0xFFFFFDE7), // Yellow 50
    low = Color(0xFF00838F), // Cyan 800
    lowBackground = Color(0xFFE0F7FA), // Cyan 50

    // Status colors
    monitoring = Color(0xFF4CAF50), // Green 500
    idle = Color(0xFF9E9E9E), // Grey 500

    // Background colors
    cardBackground = Color(0xFFFAFAFA), // Grey 50
    surfaceBackground = Color(0xFFFFFFFF), // White
    detailBackground = Color(0xFFF5F5F5), // Grey 100

    // Text colors
    labelPrimary = Color(0xFF212121), // Grey 900
    labelSecondary = Color(0xFF757575), // Grey 600
    valuePrimary = Color(0xFF424242), // Grey 800

    // Accent colors
    actionButton = Color(0xFF1976D2), // Blue 700
    actionButtonText = Color(0xFFFFFFFF), // White
    divider = Color(0xFFE0E0E0), // Grey 300
)

/**
 * Dark theme leak detection colors.
 */
val DarkLeakDetectionColors = LeakDetectionColors(
    // Severity colors - slightly muted for dark backgrounds
    critical = Color(0xFFEF5350), // Red 400
    criticalBackground = Color(0xFF2D1B1B), // Dark red
    high = Color(0xFFFF9800), // Orange 500
    highBackground = Color(0xFF2D2517), // Dark orange
    medium = Color(0xFFFFEB3B), // Yellow 500
    mediumBackground = Color(0xFF2D2A17), // Dark yellow
    low = Color(0xFF26C6DA), // Cyan 400
    lowBackground = Color(0xFF172D2D), // Dark cyan

    // Status colors
    monitoring = Color(0xFF66BB6A), // Green 400
    idle = Color(0xFF757575), // Grey 600

    // Background colors
    cardBackground = Color(0xFF1E1E1E), // Dark grey
    surfaceBackground = Color(0xFF121212), // Near black
    detailBackground = Color(0xFF2D2D2D), // Medium dark grey

    // Text colors
    labelPrimary = Color(0xFFE0E0E0), // Grey 300
    labelSecondary = Color(0xFF9E9E9E), // Grey 500
    valuePrimary = Color(0xFFBDBDBD), // Grey 400

    // Accent colors
    actionButton = Color(0xFF42A5F5), // Blue 400
    actionButtonText = Color(0xFF121212), // Near black
    divider = Color(0xFF424242), // Grey 800
)

/**
 * Returns the appropriate leak detection colors based on the current theme.
 */
@Composable
fun leakDetectionColors(darkTheme: Boolean = isSystemInDarkTheme()): LeakDetectionColors {
    return if (darkTheme) DarkLeakDetectionColors else LightLeakDetectionColors
}

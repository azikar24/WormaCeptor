/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.location.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Feature-specific colors for the Location Simulation feature.
 * Uses WormaCeptorDesignSystem for shared tokens (Spacing, CornerRadius, BorderWidth, Alpha).
 * Import asSubtleBackground() from com.azikar24.wormaceptor.core.ui.theme
 */
object LocationColors {
    val enabled = Color(0xFF4CAF50) // Green - mock active
    val disabled = Color(0xFF9E9E9E) // Gray - mock inactive
    val warning = Color(0xFFFF9800) // Orange - warning state
    val error = Color(0xFFF44336) // Red - error state
    val builtIn = Color(0xFF2196F3) // Blue - built-in preset
    val userPreset = Color(0xFF9C27B0) // Purple - user preset
    val coordinate = Color(0xFF00BCD4) // Cyan - coordinate display
}

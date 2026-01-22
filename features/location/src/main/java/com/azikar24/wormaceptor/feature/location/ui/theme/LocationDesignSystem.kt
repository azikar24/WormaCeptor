/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.location.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design system for the Location Simulation feature.
 * Aligned with WormaCeptorDesignSystem from viewer module.
 */
object LocationDesignSystem {

    object Spacing {
        val xxs = 2.dp
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val xxl = 32.dp
    }

    object CornerRadius {
        val xs = 4.dp
        val sm = 6.dp
        val md = 8.dp
        val lg = 12.dp
        val xl = 16.dp
        val pill = 999.dp
    }

    object BorderWidth {
        val thin = 0.5.dp
        val regular = 1.dp
        val thick = 2.dp
    }

    object Alpha {
        const val subtle = 0.08f
        const val light = 0.12f
        const val medium = 0.20f
        const val strong = 0.40f
    }

    object Shapes {
        val card = RoundedCornerShape(CornerRadius.md)
        val chip = RoundedCornerShape(CornerRadius.xs)
        val button = RoundedCornerShape(CornerRadius.sm)
        val inputField = RoundedCornerShape(CornerRadius.md)
    }

    /**
     * Colors for location-related UI elements.
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
}

fun Color.asSubtleBackground(): Color = this.copy(alpha = LocationDesignSystem.Alpha.subtle)

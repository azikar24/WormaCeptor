/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.preferences.ui.theme

import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Feature-specific design tokens for the Preferences Inspector.
 * Uses WormaCeptorDesignSystem for shared tokens (Spacing, CornerRadius, BorderWidth, Alpha).
 */
object PreferencesDesignSystem {

    /**
     * Colors for different preference value types.
     */
    object TypeColors {
        val string = Color(0xFF4CAF50) // Green
        val int = Color(0xFF2196F3) // Blue
        val long = Color(0xFF3F51B5) // Indigo
        val float = Color(0xFF9C27B0) // Purple
        val boolean = Color(0xFFFF9800) // Orange
        val stringSet = Color(0xFF00BCD4) // Cyan

        fun forTypeName(typeName: String): Color = when (typeName) {
            "String" -> string
            "Int" -> int
            "Long" -> long
            "Float" -> float
            "Boolean" -> boolean
            "StringSet" -> stringSet
            else -> Color.Gray
        }
    }
}

fun Color.asSubtleBackground(): Color = this.copy(alpha = WormaCeptorDesignSystem.Alpha.subtle)

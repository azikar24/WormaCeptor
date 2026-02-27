package com.azikar24.wormaceptor.feature.preferences.ui.theme

import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors

/**
 * Feature-specific design tokens for the Preferences Inspector.
 * Uses WormaCeptorDesignSystem for shared tokens (Spacing, CornerRadius, BorderWidth, Alpha).
 */
object PreferencesDesignSystem {

    /**
     * Colors for different preference value types.
     */
    object TypeColors {
        /** Color for String preference values. */
        val string = Color(0xFF4CAF50) // Green

        /** Color for Int preference values. */
        val int = Color(0xFF2196F3) // Blue

        /** Color for Long preference values. */
        val long = Color(0xFF3F51B5) // Indigo

        /** Color for Float preference values. */
        val float = Color(0xFF9C27B0) // Purple

        /** Color for Boolean preference values. */
        val boolean = Color(0xFFFF9800) // Orange

        /** Color for StringSet preference values. */
        val stringSet = Color(0xFF00BCD4) // Cyan

        /** Returns the color associated with the given preference value type name. */
        fun forTypeName(typeName: String): Color = when (typeName) {
            "String" -> string
            "Int" -> int
            "Long" -> long
            "Float" -> float
            "Boolean" -> boolean
            "StringSet" -> stringSet
            else -> WormaCeptorColors.StatusGrey
        }
    }
}

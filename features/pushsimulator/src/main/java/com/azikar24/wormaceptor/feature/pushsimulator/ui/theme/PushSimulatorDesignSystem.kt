package com.azikar24.wormaceptor.feature.pushsimulator.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Feature-specific design tokens for the Push Notification Simulator.
 * Common tokens (Spacing, CornerRadius, Shapes, etc.) are provided by WormaCeptorDesignSystem.
 */
object PushSimulatorDesignSystem {

    /**
     * Colors for notification priorities.
     */
    object PriorityColors {
        val low = Color(0xFF90A4AE) // Blue Grey
        val default = Color(0xFF4FC3F7) // Light Blue
        val high = Color(0xFFFFB74D) // Orange
        val max = Color(0xFFEF5350) // Red

        fun forPriority(priorityName: String): Color {
            return when (priorityName.uppercase()) {
                "LOW" -> low
                "DEFAULT" -> default
                "HIGH" -> high
                "MAX" -> max
                else -> default
            }
        }
    }

    /**
     * Colors for template types.
     */
    object TemplateColors {
        val preset = Color(0xFF81C784) // Light Green
        val user = Color(0xFF64B5F6) // Blue
        val action = Color(0xFFBA68C8) // Purple
    }
}

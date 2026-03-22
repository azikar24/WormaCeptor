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
        /** Color for low-priority notifications. */
        val low = Color(0xFF90A4AE) // Blue Grey

        /** Color for default-priority notifications. */
        val default = Color(0xFF4FC3F7) // Light Blue

        /** Color for high-priority notifications. */
        val high = Color(0xFFFFB74D) // Orange

        /** Color for max-priority notifications. */
        val max = Color(0xFFEF5350) // Red

        /** Returns the color associated with the given notification priority name. */
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
        /** Color for built-in preset templates. */
        val preset = Color(0xFF81C784) // Light Green

        /** Color for user-created custom templates. */
        val user = Color(0xFF64B5F6) // Blue

        /** Color for action-button elements in templates. */
        val action = Color(0xFFBA68C8) // Purple
    }
}

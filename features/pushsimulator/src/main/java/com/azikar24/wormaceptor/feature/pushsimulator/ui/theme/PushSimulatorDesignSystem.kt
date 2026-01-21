/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.pushsimulator.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design system for the Push Notification Simulator feature.
 */
object PushSimulatorDesignSystem {

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
    }

    object Shapes {
        val card = RoundedCornerShape(CornerRadius.md)
        val chip = RoundedCornerShape(CornerRadius.xl)
        val button = RoundedCornerShape(CornerRadius.sm)
        val textField = RoundedCornerShape(CornerRadius.sm)
    }

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

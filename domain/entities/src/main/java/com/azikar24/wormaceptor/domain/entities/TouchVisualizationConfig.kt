/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Configuration for touch visualization overlay.
 *
 * @property circleColor The color of the touch indicator circles (ARGB format)
 * @property trailEnabled Whether to show touch trails (lines following finger movement)
 * @property showCoordinates Whether to show X/Y coordinates near touch points
 * @property circleSize The size of touch indicator circles in density-independent pixels
 */
data class TouchVisualizationConfig(
    val circleColor: Long,
    val trailEnabled: Boolean,
    val showCoordinates: Boolean,
    val circleSize: Float,
) {
    companion object {
        /**
         * Default configuration for touch visualization.
         */
        val DEFAULT = TouchVisualizationConfig(
            circleColor = 0xFF2196F3, // Material Blue 500
            trailEnabled = true,
            showCoordinates = false,
            circleSize = 48f,
        )

        /**
         * Minimum circle size in dp.
         */
        const val MIN_CIRCLE_SIZE = 24f

        /**
         * Maximum circle size in dp.
         */
        const val MAX_CIRCLE_SIZE = 96f

        /**
         * Preset colors available for selection.
         */
        val PRESET_COLORS = listOf(
            0xFF2196F3, // Blue
            0xFFF44336, // Red
            0xFF4CAF50, // Green
            0xFFFF9800, // Orange
            0xFF9C27B0, // Purple
            0xFF00BCD4, // Cyan
            0xFFFFEB3B, // Yellow
            0xFFE91E63, // Pink
            0xFF607D8B, // Blue Grey
            0xFFFFFFFF, // White
        )
    }
}

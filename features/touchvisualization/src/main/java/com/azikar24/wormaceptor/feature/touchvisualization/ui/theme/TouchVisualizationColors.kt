/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.touchvisualization.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/**
 * Color definitions for touch visualization feature.
 */
data class TouchVisualizationColors(
    val previewBackground: Color,
    val previewBorder: Color,
    val rippleColor: Color,
    val trailColor: Color,
    val coordinateTextColor: Color,
    val coordinateBackgroundColor: Color,
    val settingsCardBackground: Color,
)

/**
 * Creates touch visualization colors based on the current theme.
 */
@Composable
fun touchVisualizationColors(): TouchVisualizationColors {
    val colorScheme = MaterialTheme.colorScheme

    return remember(colorScheme) {
        TouchVisualizationColors(
            previewBackground = colorScheme.surfaceVariant.copy(alpha = 0.3f),
            previewBorder = colorScheme.outline.copy(alpha = 0.5f),
            rippleColor = Color.White.copy(alpha = 0.4f),
            trailColor = Color.White.copy(alpha = 0.6f),
            coordinateTextColor = Color.White,
            coordinateBackgroundColor = Color.Black.copy(alpha = 0.6f),
            settingsCardBackground = colorScheme.surfaceVariant.copy(alpha = 0.5f),
        )
    }
}

/**
 * Converts a Long color value to Compose Color.
 */
fun Long.toComposeColor(): Color = Color(this)

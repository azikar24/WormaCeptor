/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewhierarchy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.domain.entities.ViewType

/**
 * Colors for the View Hierarchy Inspector feature.
 *
 * Uses a colorful scheme to distinguish different view types:
 * - Layouts: Purple/Violet
 * - Text: Blue
 * - Images: Green
 * - Buttons: Pink/Red
 * - Lists: Cyan
 * - Inputs: Orange
 * - Web: Yellow
 * - Surfaces: Teal
 * - Other: Gray
 */
@Immutable
data class ViewHierarchyColors(
    // Primary accent
    val primary: Color,

    // View type colors
    val layout: Color,
    val text: Color,
    val image: Color,
    val button: Color,
    val list: Color,
    val input: Color,
    val web: Color,
    val surface: Color,
    val other: Color,

    // Hierarchy visualization
    val treeLine: Color,
    val depthIndicator: Color,
    val selectedNode: Color,
    val expandIcon: Color,

    // Background colors
    val cardBackground: Color,
    val searchBackground: Color,
    val nodeBackground: Color,
    val nodeBackgroundSelected: Color,

    // Text colors
    val labelPrimary: Color,
    val labelSecondary: Color,
    val valuePrimary: Color,
    val className: Color,
    val resourceId: Color,
) {
    /**
     * Returns the color for a view type.
     */
    fun colorForViewType(viewType: ViewType): Color = when (viewType) {
        ViewType.LAYOUT -> layout
        ViewType.TEXT -> text
        ViewType.IMAGE -> image
        ViewType.BUTTON -> button
        ViewType.LIST -> list
        ViewType.INPUT -> input
        ViewType.WEB -> web
        ViewType.SURFACE -> surface
        ViewType.OTHER -> other
    }
}

/**
 * Light theme view hierarchy colors.
 */
val LightViewHierarchyColors = ViewHierarchyColors(
    // Primary accent
    primary = Color(0xFF7C4DFF),        // Deep Purple A200

    // View type colors
    layout = Color(0xFF9C27B0),         // Purple 500
    text = Color(0xFF2196F3),           // Blue 500
    image = Color(0xFF4CAF50),          // Green 500
    button = Color(0xFFE91E63),         // Pink 500
    list = Color(0xFF00BCD4),           // Cyan 500
    input = Color(0xFFFF9800),          // Orange 500
    web = Color(0xFFFFC107),            // Amber 500
    surface = Color(0xFF009688),        // Teal 500
    other = Color(0xFF9E9E9E),          // Gray 500

    // Hierarchy visualization
    treeLine = Color(0xFFBDBDBD),
    depthIndicator = Color(0xFF7C4DFF).copy(alpha = 0.3f),
    selectedNode = Color(0xFF7C4DFF).copy(alpha = 0.15f),
    expandIcon = Color(0xFF757575),

    // Background colors
    cardBackground = Color(0xFFFAFAFA),
    searchBackground = Color(0xFFF5F5F5),
    nodeBackground = Color(0xFFFFFFFF),
    nodeBackgroundSelected = Color(0xFFEDE7F6),

    // Text colors
    labelPrimary = Color(0xFF212121),
    labelSecondary = Color(0xFF757575),
    valuePrimary = Color(0xFF424242),
    className = Color(0xFF1565C0),      // Blue 800
    resourceId = Color(0xFF6A1B9A),     // Purple 800
)

/**
 * Dark theme view hierarchy colors.
 */
val DarkViewHierarchyColors = ViewHierarchyColors(
    // Primary accent
    primary = Color(0xFFB388FF),        // Deep Purple A100

    // View type colors
    layout = Color(0xFFCE93D8),         // Purple 200
    text = Color(0xFF64B5F6),           // Blue 300
    image = Color(0xFF81C784),          // Green 300
    button = Color(0xFFF48FB1),         // Pink 200
    list = Color(0xFF4DD0E1),           // Cyan 300
    input = Color(0xFFFFB74D),          // Orange 300
    web = Color(0xFFFFD54F),            // Amber 300
    surface = Color(0xFF4DB6AC),        // Teal 300
    other = Color(0xFF757575),          // Gray 600

    // Hierarchy visualization
    treeLine = Color(0xFF616161),
    depthIndicator = Color(0xFFB388FF).copy(alpha = 0.3f),
    selectedNode = Color(0xFFB388FF).copy(alpha = 0.2f),
    expandIcon = Color(0xFF9E9E9E),

    // Background colors
    cardBackground = Color(0xFF1E1E1E),
    searchBackground = Color(0xFF2D2D2D),
    nodeBackground = Color(0xFF252525),
    nodeBackgroundSelected = Color(0xFF311B92).copy(alpha = 0.3f),

    // Text colors
    labelPrimary = Color(0xFFE0E0E0),
    labelSecondary = Color(0xFF9E9E9E),
    valuePrimary = Color(0xFFBDBDBD),
    className = Color(0xFF90CAF9),      // Blue 200
    resourceId = Color(0xFFE1BEE7),     // Purple 100
)

/**
 * Returns the appropriate view hierarchy colors based on the current theme.
 */
@Composable
fun viewHierarchyColors(darkTheme: Boolean = isSystemInDarkTheme()): ViewHierarchyColors {
    return if (darkTheme) DarkViewHierarchyColors else LightViewHierarchyColors
}

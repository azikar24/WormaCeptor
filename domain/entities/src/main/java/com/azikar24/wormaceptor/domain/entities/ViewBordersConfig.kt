/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Configuration for the View Borders feature.
 *
 * This feature overlays colored borders around views to visualize
 * the layout hierarchy, similar to browser developer tools.
 *
 * Color coding follows standard conventions:
 * - Margin: Orange - space outside the view's border
 * - Padding: Green - space between border and content
 * - Content: Blue - the actual content area
 *
 * @property enabled Whether the view borders overlay is currently active
 * @property borderWidth Width of the borders in dp (1-5)
 * @property marginColor Color for margin area visualization (ARGB hex format)
 * @property paddingColor Color for padding area visualization (ARGB hex format)
 * @property contentColor Color for content area visualization (ARGB hex format)
 * @property showDimensions Whether to show view dimensions (width x height) text
 */
data class ViewBordersConfig(
    val enabled: Boolean = false,
    val borderWidth: Int = 2,
    val marginColor: Long = DEFAULT_MARGIN_COLOR,
    val paddingColor: Long = DEFAULT_PADDING_COLOR,
    val contentColor: Long = DEFAULT_CONTENT_COLOR,
    val showDimensions: Boolean = false,
) {
    companion object {
        /**
         * Default margin color: semi-transparent orange
         */
        const val DEFAULT_MARGIN_COLOR: Long = 0x4DFF9800 // Orange with ~30% alpha

        /**
         * Default padding color: semi-transparent green
         */
        const val DEFAULT_PADDING_COLOR: Long = 0x4D4CAF50 // Green with ~30% alpha

        /**
         * Default content color: semi-transparent blue
         */
        const val DEFAULT_CONTENT_COLOR: Long = 0x4D2196F3 // Blue with ~30% alpha

        /**
         * Minimum border width in dp
         */
        const val MIN_BORDER_WIDTH = 1

        /**
         * Maximum border width in dp
         */
        const val MAX_BORDER_WIDTH = 5

        /**
         * Default configuration with standard colors and disabled state
         */
        val DEFAULT = ViewBordersConfig()
    }
}

/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import androidx.compose.ui.geometry.Offset

/**
 * State for the Tool Overlay floating toolbar.
 *
 * Tracks enabled state for View Borders and Measurement tools,
 * as well as the toolbar's position on screen.
 */
data class ToolOverlayState(
    val viewBordersEnabled: Boolean = false,
    val measurementEnabled: Boolean = false,
    val positionPercent: Offset = DEFAULT_POSITION_PERCENT,
    val isDragging: Boolean = false,
) {
    /**
     * Returns true if the toolbar should be visible (any tool is enabled).
     */
    fun shouldShow(): Boolean = viewBordersEnabled || measurementEnabled

    /**
     * Returns true if the toolbar is currently in the dismiss zone (bottom of screen).
     */
    fun isInDismissZone(): Boolean = positionPercent.y >= DISMISS_ZONE_THRESHOLD

    companion object {
        val EMPTY = ToolOverlayState()

        /**
         * Default position: right side, middle of screen.
         */
        val DEFAULT_POSITION_PERCENT = Offset(0.95f, 0.5f)

        /**
         * Y position threshold for dismiss zone (bottom 15% of screen).
         */
        const val DISMISS_ZONE_THRESHOLD = 0.85f
    }
}

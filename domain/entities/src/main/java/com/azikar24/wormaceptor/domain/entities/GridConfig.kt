/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Configuration for the Grid Overlay feature.
 *
 * This feature displays a customizable grid overlay on top of the application
 * to help visualize layout alignment with Material Design guidelines.
 *
 * Features:
 * - Main grid with configurable size (4dp, 8dp, 16dp, etc.)
 * - Keylines at specific positions from screen edges
 * - Baseline grid for typography alignment
 * - Spacing visualization
 *
 * @property enabled Whether the grid overlay is currently active
 * @property gridSize Size of the main grid squares in dp (typically 4, 8, 16)
 * @property gridColor Color for the main grid lines (ARGB hex format)
 * @property gridAlpha Opacity of the grid lines (0.0 to 1.0)
 * @property showKeylines Whether to display keylines at edge margins
 * @property keylinePositions List of keyline positions in dp from screen edges
 * @property showSpacing Whether to highlight the spacing columns
 * @property baselineGridEnabled Whether to show the baseline grid for typography
 * @property baselineGridSize Size of the baseline grid in dp (typically 4 or 8)
 */
data class GridConfig(
    val enabled: Boolean = false,
    val gridSize: Int = DEFAULT_GRID_SIZE,
    val gridColor: Long = DEFAULT_GRID_COLOR,
    val gridAlpha: Float = DEFAULT_GRID_ALPHA,
    val showKeylines: Boolean = true,
    val keylinePositions: List<Int> = DEFAULT_KEYLINE_POSITIONS,
    val showSpacing: Boolean = true,
    val baselineGridEnabled: Boolean = false,
    val baselineGridSize: Int = DEFAULT_BASELINE_SIZE,
) {
    companion object {
        /**
         * Default grid size in dp - Material Design standard 8dp grid
         */
        const val DEFAULT_GRID_SIZE = 8

        /**
         * Default grid color: Blue (Material Blue 500)
         */
        const val DEFAULT_GRID_COLOR: Long = 0xFF2196F3

        /**
         * Default grid opacity: 30%
         */
        const val DEFAULT_GRID_ALPHA = 0.3f

        /**
         * Default keyline positions from edges in dp
         * 16dp: Standard margin
         * 72dp: Navigation drawer/app bar keyline
         */
        val DEFAULT_KEYLINE_POSITIONS = listOf(16, 72)

        /**
         * Default baseline grid size in dp
         */
        const val DEFAULT_BASELINE_SIZE = 4

        /**
         * Minimum grid size in dp
         */
        const val MIN_GRID_SIZE = 4

        /**
         * Maximum grid size in dp
         */
        const val MAX_GRID_SIZE = 48

        /**
         * Minimum baseline grid size in dp
         */
        const val MIN_BASELINE_SIZE = 2

        /**
         * Maximum baseline grid size in dp
         */
        const val MAX_BASELINE_SIZE = 16

        /**
         * Available grid size presets in dp
         */
        val GRID_SIZE_PRESETS = listOf(4, 8, 12, 16, 24, 32, 48)

        /**
         * Available baseline size presets in dp
         */
        val BASELINE_SIZE_PRESETS = listOf(2, 4, 8)

        /**
         * Maximum number of keylines allowed
         */
        const val MAX_KEYLINES = 10

        /**
         * Default configuration with standard Material Design values
         */
        val DEFAULT = GridConfig()
    }
}

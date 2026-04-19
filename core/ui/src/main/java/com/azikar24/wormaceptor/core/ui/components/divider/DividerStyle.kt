package com.azikar24.wormaceptor.core.ui.components.divider

/**
 * Divider style variants for consistent visual separation across WormaCeptor.
 */
enum class DividerStyle {
    /**
     * Standard list item divider using the theme's outline color.
     * Best for: list separators, menu dividers, generic content breaks.
     */
    Standard,

    /**
     * Subtle divider with reduced opacity for lightweight separation.
     * Best for: nested content, form field separators, secondary groupings.
     */
    Subtle,

    /**
     * Prominent section divider with stronger visibility.
     * Best for: major section breaks, tab content dividers, header/content separation.
     */
    Section,

    /**
     * Thick divider (2dp) for structural emphasis.
     * Best for: table header separators, data grid boundaries.
     */
    Thick,
}

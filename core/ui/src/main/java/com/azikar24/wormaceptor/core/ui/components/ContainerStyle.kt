package com.azikar24.wormaceptor.core.ui.components

/**
 * Container style variants for consistent UI across WormaCeptor.
 */
enum class ContainerStyle {
    /**
     * Filled container with no visible border.
     * Uses surfaceVariant background at medium alpha.
     * Best for: cards, tiles, content areas.
     */
    Filled,

    /**
     * Outlined container with subtle border and light background.
     * Uses outlineVariant border with subtle surfaceVariant fill.
     * Best for: list items, selectable cards, grouped content.
     */
    Outlined,
}

/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewer.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color tokens for syntax highlighting UI components.
 * These are UI-specific colors that complement the domain syntax colors.
 */
@Suppress("MagicNumber")
internal object SyntaxColorTokens {

    /**
     * Light theme UI colors for code display.
     */
    object Light {
        /** Line number text color. */
        val LineNumberText = Color(0xFF9E9E9E)

        /** Line number gutter background. */
        val LineNumberBackground = Color(0xFFF5F5F5)

        /** Code area background. */
        val CodeBackground = Color(0xFFFAFAFA)

        /** Search match highlight background (soft yellow). */
        val SearchHighlight = Color(0xFFFFF59D)

        /** Current search match highlight background (cyan). */
        val SearchHighlightCurrent = Color(0xFF4DD0E1)

        /** Text color on search highlight. */
        val SearchHighlightText: Color = Color.Black
    }

    /**
     * Dark theme UI colors for code display.
     */
    object Dark {
        /** Line number text color. */
        val LineNumberText = Color(0xFF606060)

        /** Line number gutter background. */
        val LineNumberBackground = Color(0xFF1E1E1E)

        /** Code area background. */
        val CodeBackground = Color(0xFF252526)

        /** Search match highlight background (muted orange). */
        val SearchHighlight = Color(0xFF613214)

        /** Current search match highlight background (blue). */
        val SearchHighlightCurrent = Color(0xFF264F78)

        /** Text color on search highlight. */
        val SearchHighlightText: Color = Color.White
    }
}

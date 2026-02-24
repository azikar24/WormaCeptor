package com.azikar24.wormaceptor.core.ui.theme

import androidx.compose.ui.graphics.Color

@Suppress("MagicNumber")
internal object SyntaxColorTokens {

    object Light {
        val LineNumberText = Color(0xFF9E9E9E)
        val LineNumberBackground = Color(0xFFF5F5F5)
        val CodeBackground = Color(0xFFFAFAFA)
        val SearchHighlight = Color(0xFFFFF59D)
        val SearchHighlightCurrent = Color(0xFF4DD0E1)
        val SearchHighlightText: Color = WormaCeptorDesignSystem.ThemeColors.LightTextPrimary
    }

    object Dark {
        val LineNumberText = Color(0xFF606060)
        val LineNumberBackground = Color(0xFF1E1E1E)
        val CodeBackground = Color(0xFF252526)
        val SearchHighlight = Color(0xFF613214)
        val SearchHighlightCurrent = Color(0xFF264F78)
        val SearchHighlightText: Color = WormaCeptorDesignSystem.ThemeColors.DarkTextPrimary
    }
}

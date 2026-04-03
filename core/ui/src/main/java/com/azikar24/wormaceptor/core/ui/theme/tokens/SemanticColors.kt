package com.azikar24.wormaceptor.core.ui.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Theme-aware semantic color tokens.
 * Maps high-level UI roles (background, text, accent, etc.) to concrete [Palette] entries.
 * Light and dark instances are provided below.
 */
@Immutable
data class SemanticColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentSubtle: Color,
    val error: Color,
    val errorDark: Color,
    val success: Color,
    val warning: Color,
)

/** Semantic colors for light theme, matching [WormaCeptorDesignSystem.ThemeColors] light values. */
internal val LightSemanticColors = SemanticColors(
    background = Palette.White, // 0xFFFFFFFF
    surface = Palette.Gray50, // 0xFFFAFAFA
    surfaceVariant = Palette.Gray150, // 0xFFF0F0F0
    textPrimary = Palette.Gray990, // 0xFF0A0A0A
    textSecondary = Palette.Gray650, // 0xFF6B6B6B
    textTertiary = Palette.Gray400, // 0xFF9CA3AF
    accent = Palette.Teal600, // 0xFF0D9488
    accentSubtle = Palette.AccentSubtleLight, // 0x120D9488
    error = Palette.Red600, // 0xFFDC2626
    errorDark = Palette.Red800, // 0xFFF87171
    success = Palette.Green700, // 0xFF16A34A
    warning = Palette.Amber700, // 0xFFD97706
)

/** Semantic colors for dark theme, matching [WormaCeptorDesignSystem.ThemeColors] dark values. */
internal val DarkSemanticColors = SemanticColors(
    background = Palette.Gray990, // 0xFF0A0A0A
    surface = Palette.Gray975, // 0xFF141414
    surfaceVariant = Palette.Gray925, // 0xFF1F1F1F
    textPrimary = Palette.Gray50, // 0xFFFAFAFA
    textSecondary = Palette.Gray500, // 0xFF8A8A8A
    textTertiary = Palette.Gray800, // 0xFF525252
    accent = Palette.TealBright, // 0xFF2DD4BF
    accentSubtle = Palette.AccentSubtleDark, // 0x152DD4BF
    error = Palette.Red800, // 0xFFF87171
    errorDark = Palette.Red600, // 0xFFDC2626
    success = Palette.Green700, // 0xFF16A34A
    warning = Palette.Amber700, // 0xFFD97706
)

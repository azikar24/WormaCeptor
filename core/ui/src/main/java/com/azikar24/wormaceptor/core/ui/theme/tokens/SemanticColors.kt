package com.azikar24.wormaceptor.core.ui.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Theme-aware semantic palette layered on top of M3's
 * [androidx.compose.material3.ColorScheme]. Adds WormaCeptor-specific roles
 * (success, warning, errorDark, accentSubtle, a three-step text hierarchy)
 * and is read via
 * [com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens.semantic] which
 * resolves the correct variant for the current theme.
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
    val accentSecondary: Color,
    val accentTertiary: Color,
    val accentSubtle: Color,
    val error: Color,
    val errorDark: Color,
    val success: Color,
    val warning: Color,
)

internal val LightSemanticColors = SemanticColors(
    background = Palette.White,
    surface = Palette.Gray50,
    surfaceVariant = Palette.Gray150,
    textPrimary = Palette.Gray990,
    textSecondary = Palette.Gray650,
    textTertiary = Palette.Gray400,
    accent = Palette.Teal600,
    accentSecondary = Palette.Indigo600,
    accentTertiary = Palette.Purple600,
    accentSubtle = Palette.AccentSubtleLight,
    error = Palette.Red600,
    errorDark = Palette.Red800,
    success = Palette.Green700,
    warning = Palette.Amber700,
)

internal val DarkSemanticColors = SemanticColors(
    background = Palette.Gray990,
    surface = Palette.Gray975,
    surfaceVariant = Palette.Gray925,
    textPrimary = Palette.Gray50,
    textSecondary = Palette.Gray500,
    textTertiary = Palette.Gray800,
    accent = Palette.TealBright,
    accentSecondary = Palette.Indigo300,
    accentTertiary = Palette.Purple300,
    accentSubtle = Palette.AccentSubtleDark,
    error = Palette.Red800,
    errorDark = Palette.Red600,
    success = Palette.Green700,
    warning = Palette.Amber700,
)

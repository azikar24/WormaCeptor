/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.loadedlibraries.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary

/**
 * Colors for the Loaded Libraries Inspector feature.
 *
 * Uses a consistent color scheme to represent different library types:
 * - Native (.so): Blue tones - core system functionality
 * - DEX: Purple tones - Dalvik/ART bytecode
 * - JAR: Orange tones - Java archives
 * - System: Gray tones - system libraries
 * - App: Green tones - application libraries
 */
@Immutable
data class LoadedLibrariesColors(
    // Primary accent
    val primary: Color,

    // Library type colors
    val nativeSo: Color,
    val dex: Color,
    val jar: Color,
    val aarResource: Color,

    // Source type colors
    val systemLibrary: Color,
    val appLibrary: Color,
    val systemBadge: Color,

    // Background colors
    val cardBackground: Color,
    val chipBackground: Color,
    val selectedChipBackground: Color,
    val searchBackground: Color,

    // Text colors
    val labelPrimary: Color,
    val labelSecondary: Color,
    val pathText: Color,
    val valuePrimary: Color,

    // Accent colors
    val searchHighlight: Color,
    val divider: Color,
) {
    /**
     * Returns the appropriate color for a library type.
     */
    fun colorForType(type: LoadedLibrary.LibraryType): Color = when (type) {
        LoadedLibrary.LibraryType.NATIVE_SO -> nativeSo
        LoadedLibrary.LibraryType.DEX -> dex
        LoadedLibrary.LibraryType.JAR -> jar
        LoadedLibrary.LibraryType.AAR_RESOURCE -> aarResource
    }

    /**
     * Returns the appropriate color based on library source.
     */
    fun colorForSource(isSystemLibrary: Boolean): Color {
        return if (isSystemLibrary) systemLibrary else appLibrary
    }
}

/**
 * Light theme colors for Loaded Libraries feature.
 */
val LightLoadedLibrariesColors = LoadedLibrariesColors(
    // Primary accent
    primary = Color(0xFF7C4DFF),       // Deep Purple A200

    // Library type colors - vibrant for visibility
    nativeSo = Color(0xFF2196F3),      // Blue 500 - native code
    dex = Color(0xFF9C27B0),           // Purple 500 - bytecode
    jar = Color(0xFFFF9800),           // Orange 500 - archives
    aarResource = Color(0xFF00BCD4),   // Cyan 500 - resources

    // Source type colors
    systemLibrary = Color(0xFF607D8B), // Blue Gray 500
    appLibrary = Color(0xFF4CAF50),    // Green 500
    systemBadge = Color(0xFF78909C),   // Blue Gray 400

    // Background colors
    cardBackground = Color(0xFFFAFAFA),
    chipBackground = Color(0xFFE0E0E0),
    selectedChipBackground = Color(0xFF1976D2),
    searchBackground = Color(0xFFF5F5F5),

    // Text colors
    labelPrimary = Color(0xFF212121),
    labelSecondary = Color(0xFF757575),
    pathText = Color(0xFF455A64),
    valuePrimary = Color(0xFF424242),

    // Accent colors
    searchHighlight = Color(0xFFFFEB3B),
    divider = Color(0xFFE0E0E0),
)

/**
 * Dark theme colors for Loaded Libraries feature.
 */
val DarkLoadedLibrariesColors = LoadedLibrariesColors(
    // Primary accent
    primary = Color(0xFFB388FF),       // Deep Purple A100

    // Library type colors - slightly muted for dark backgrounds
    nativeSo = Color(0xFF64B5F6),      // Blue 300
    dex = Color(0xFFBA68C8),           // Purple 300
    jar = Color(0xFFFFB74D),           // Orange 300
    aarResource = Color(0xFF4DD0E1),   // Cyan 300

    // Source type colors
    systemLibrary = Color(0xFF90A4AE), // Blue Gray 300
    appLibrary = Color(0xFF81C784),    // Green 300
    systemBadge = Color(0xFF90A4AE),   // Blue Gray 300

    // Background colors
    cardBackground = Color(0xFF1E1E1E),
    chipBackground = Color(0xFF424242),
    selectedChipBackground = Color(0xFF1565C0),
    searchBackground = Color(0xFF2D2D2D),

    // Text colors
    labelPrimary = Color(0xFFE0E0E0),
    labelSecondary = Color(0xFF9E9E9E),
    pathText = Color(0xFF78909C),
    valuePrimary = Color(0xFFBDBDBD),

    // Accent colors
    searchHighlight = Color(0xFFFDD835),
    divider = Color(0xFF424242),
)

/**
 * Returns the appropriate colors based on the current theme.
 */
@Composable
fun loadedLibrariesColors(darkTheme: Boolean = isSystemInDarkTheme()): LoadedLibrariesColors {
    return if (darkTheme) DarkLoadedLibrariesColors else LightLoadedLibrariesColors
}

/**
 * Returns a human-readable label for a library type.
 */
fun LoadedLibrary.LibraryType.displayLabel(): String = when (this) {
    LoadedLibrary.LibraryType.NATIVE_SO -> "Native (.so)"
    LoadedLibrary.LibraryType.DEX -> "DEX"
    LoadedLibrary.LibraryType.JAR -> "JAR"
    LoadedLibrary.LibraryType.AAR_RESOURCE -> "AAR"
}

/**
 * Returns a short badge label for a library type.
 */
fun LoadedLibrary.LibraryType.badgeLabel(): String = when (this) {
    LoadedLibrary.LibraryType.NATIVE_SO -> ".so"
    LoadedLibrary.LibraryType.DEX -> "dex"
    LoadedLibrary.LibraryType.JAR -> "jar"
    LoadedLibrary.LibraryType.AAR_RESOURCE -> "aar"
}

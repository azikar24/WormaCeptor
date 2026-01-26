package com.azikar24.wormaceptor.feature.viewer.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Colors
val md_theme_light_primary = Color(0xFF0061A4)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFD1E4FF)
val md_theme_light_onPrimaryContainer = Color(0xFF001D36)
val md_theme_light_secondary = Color(0xFF535F70)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFD7E3F7)
val md_theme_light_onSecondaryContainer = Color(0xFF101C2B)
val md_theme_light_tertiary = Color(0xFF6B5778)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFF2DAFF)
val md_theme_light_onTertiaryContainer = Color(0xFF251431)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFDFBFF)
val md_theme_light_onBackground = Color(0xFF001B39)
val md_theme_light_surface = Color(0xFFFDFBFF)
val md_theme_light_onSurface = Color(0xFF001B39)
val md_theme_light_surfaceVariant = Color(0xFFDFE2EB)
val md_theme_light_onSurfaceVariant = Color(0xFF43474E)
val md_theme_light_outline = Color(0xFF73777F)
val md_theme_light_inverseOnSurface = Color(0xFFECF0FF)
val md_theme_light_inverseSurface = Color(0xFF003061)
val md_theme_light_inversePrimary = Color(0xFF9ECAFF)
val md_theme_light_surfaceTint = Color(0xFF0061A4)
val md_theme_light_outlineVariant = Color(0xFFC3C7CF)
val md_theme_light_scrim = Color(0xFF000000)

// Dark Theme Colors
val md_theme_dark_primary = Color(0xFF9ECAFF)
val md_theme_dark_onPrimary = Color(0xFF003258)
val md_theme_dark_primaryContainer = Color(0xFF00497D)
val md_theme_dark_onPrimaryContainer = Color(0xFFD1E4FF)
val md_theme_dark_secondary = Color(0xFFBBC7DB)
val md_theme_dark_onSecondary = Color(0xFF253140)
val md_theme_dark_secondaryContainer = Color(0xFF3B4858)
val md_theme_dark_onSecondaryContainer = Color(0xFFD7E3F7)
val md_theme_dark_tertiary = Color(0xFFD7BEE4)
val md_theme_dark_onTertiary = Color(0xFF3B2948)
val md_theme_dark_tertiaryContainer = Color(0xFF523F5F)
val md_theme_dark_onTertiaryContainer = Color(0xFFF2DAFF)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF001B39)
val md_theme_dark_onBackground = Color(0xFFD6E3FF)
val md_theme_dark_surface = Color(0xFF001B39)
val md_theme_dark_onSurface = Color(0xFFD6E3FF)
val md_theme_dark_surfaceVariant = Color(0xFF43474E)
val md_theme_dark_onSurfaceVariant = Color(0xFFC3C7CF)
val md_theme_dark_outline = Color(0xFF8D9199)
val md_theme_dark_inverseOnSurface = Color(0xFF001B39)
val md_theme_dark_inverseSurface = Color(0xFFD6E3FF)
val md_theme_dark_inversePrimary = Color(0xFF0061A4)
val md_theme_dark_surfaceTint = Color(0xFF9ECAFF)
val md_theme_dark_outlineVariant = Color(0xFF43474E)
val md_theme_dark_scrim = Color(0xFF000000)

// Custom Status Colors (Semantic)
object WormaCeptorColors {
    val StatusGreen = Color(0xFF4CAF50)
    val StatusAmber = Color(0xFFFFC107)
    val StatusRed = Color(0xFFF44336)
    val StatusBlue = Color(0xFF2196F3)
    val StatusGrey = Color(0xFF9E9E9E)

    // Content type colors for filter chips
    @Deprecated("Use ContentTypeColors.Xml or another appropriate color")
    val ContentPurple = Color(0xFF6B5778)

    @Deprecated("Use ContentTypeColors.Image")
    val ContentCyan = Color(0xFF00838F)

    // Tool category colors (from ToolsTab)
    object CategoryColors {
        val Inspection = Color(0xFF6366F1) // Indigo
        val Performance = Color(0xFFF59E0B) // Amber
        val Network = Color(0xFF10B981) // Emerald
        val Simulation = Color(0xFF8B5CF6) // Purple
        val VisualDebug = Color(0xFFEC4899) // Pink
        val Core = Color(0xFF3B82F6) // Blue
        val Favorites = Color(0xFFF59E0B) // Amber (same as Performance)
        val Fallback = Color(0xFF6B7280) // Gray
    }

    // Content type colors (for ContentTypeChip and similar)
    object ContentTypeColors {
        val Json = Color(0xFFF59E0B) // Amber
        val Xml = Color(0xFF8B5CF6) // Purple
        val Html = Color(0xFFEC4899) // Pink
        val Protobuf = Color(0xFF10B981) // Emerald
        val FormData = Color(0xFF3B82F6) // Blue
        val Multipart = Color(0xFF6366F1) // Indigo
        val PlainText = Color(0xFF6B7280) // Gray
        val Binary = Color(0xFFEF4444) // Red
        val Pdf = Color(0xFFDC2626) // Red-600
        val Image = Color(0xFF14B8A6) // Teal
        val Unknown = Color(0xFF9CA3AF) // Gray-400
    }

    // HTTP method colors
    object HttpMethodColors {
        val Get = Color(0xFF3B82F6) // Blue
        val Post = Color(0xFF10B981) // Green
        val Put = Color(0xFFF59E0B) // Amber
        val Patch = Color(0xFF9C27B0) // Purple
        val Delete = Color(0xFFEF4444) // Red
        val Head = Color(0xFF6B7280) // Gray
        val Options = Color(0xFF8B5CF6) // Violet
    }
}

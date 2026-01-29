package com.azikar24.wormaceptor.feature.viewer.ui.theme

import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors as CoreColors

// ============================================================
// LIGHT THEME COLORS - Professional, clean appearance
// ============================================================

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
val md_theme_light_background = Color(0xFFFAFAFC) // Slightly cooler white
val md_theme_light_onBackground = Color(0xFF1A1C1E)
val md_theme_light_surface = Color(0xFFFAFAFC)
val md_theme_light_onSurface = Color(0xFF1A1C1E)
val md_theme_light_surfaceVariant = Color(0xFFE7E9ED)
val md_theme_light_onSurfaceVariant = Color(0xFF43474E)
val md_theme_light_outline = Color(0xFF73777F)
val md_theme_light_inverseOnSurface = Color(0xFFF1F3F7)
val md_theme_light_inverseSurface = Color(0xFF2F3033)
val md_theme_light_inversePrimary = Color(0xFF9ECAFF)
val md_theme_light_surfaceTint = Color(0xFF0061A4)
val md_theme_light_outlineVariant = Color(0xFFC3C7CF)
val md_theme_light_scrim = Color(0xFF000000)

// ============================================================
// DARK THEME COLORS - Easy on the eyes, reduced eye strain
// ============================================================

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
val md_theme_dark_background = Color(0xFF111318) // Deeper, more comfortable dark
val md_theme_dark_onBackground = Color(0xFFE3E3E8)
val md_theme_dark_surface = Color(0xFF111318)
val md_theme_dark_onSurface = Color(0xFFE3E3E8)
val md_theme_dark_surfaceVariant = Color(0xFF43474E)
val md_theme_dark_onSurfaceVariant = Color(0xFFC3C7CF)
val md_theme_dark_outline = Color(0xFF8D9199)
val md_theme_dark_inverseOnSurface = Color(0xFF111318)
val md_theme_dark_inverseSurface = Color(0xFFE3E3E8)
val md_theme_dark_inversePrimary = Color(0xFF0061A4)
val md_theme_dark_surfaceTint = Color(0xFF9ECAFF)
val md_theme_dark_outlineVariant = Color(0xFF43474E)
val md_theme_dark_scrim = Color(0xFF000000)

// ============================================================
// TYPE ALIAS - Use centralized colors from core module
// ============================================================

/**
 * WormaCeptorColors provides semantic colors for the viewer module.
 *
 * This is a type alias to the centralized [CoreColors] from core:ui module.
 * All color definitions are maintained in one place for consistency.
 *
 * Usage:
 * ```
 * WormaCeptorColors.StatusGreen
 * WormaCeptorColors.Category.Inspection
 * WormaCeptorColors.HttpMethod.Get
 * WormaCeptorColors.ContentType.Json
 * WormaCeptorColors.Chart.Fast
 * ```
 */
typealias WormaCeptorColors = CoreColors

/**
 * Backward compatibility aliases for existing code.
 * These map old nested object names to the new centralized structure.
 */
object ViewerColors {
    /** @deprecated Use WormaCeptorColors.Category instead */
    @Deprecated("Use WormaCeptorColors.Category", ReplaceWith("WormaCeptorColors.Category"))
    object CategoryColors {
        val Inspection = CoreColors.Category.Inspection
        val Performance = CoreColors.Category.Performance
        val Network = CoreColors.Category.Network
        val Simulation = CoreColors.Category.Simulation
        val Core = CoreColors.Category.Core
        val Favorites = CoreColors.Category.Favorites
        val Fallback = CoreColors.Category.Fallback
    }

    /** @deprecated Use WormaCeptorColors.ContentType instead */
    @Deprecated("Use WormaCeptorColors.ContentType", ReplaceWith("WormaCeptorColors.ContentType"))
    object ContentTypeColors {
        val Json = CoreColors.ContentType.Json
        val Xml = CoreColors.ContentType.Xml
        val Html = CoreColors.ContentType.Html
        val Protobuf = CoreColors.ContentType.Protobuf
        val FormData = CoreColors.ContentType.FormData
        val Multipart = CoreColors.ContentType.Multipart
        val PlainText = CoreColors.ContentType.PlainText
        val Binary = CoreColors.ContentType.Binary
        val Pdf = CoreColors.ContentType.Pdf
        val Image = CoreColors.ContentType.Image
        val Unknown = CoreColors.ContentType.Unknown
    }

    /** @deprecated Use WormaCeptorColors.HttpMethod instead */
    @Deprecated("Use WormaCeptorColors.HttpMethod", ReplaceWith("WormaCeptorColors.HttpMethod"))
    object HttpMethodColors {
        val Get = CoreColors.HttpMethod.Get
        val Post = CoreColors.HttpMethod.Post
        val Put = CoreColors.HttpMethod.Put
        val Patch = CoreColors.HttpMethod.Patch
        val Delete = CoreColors.HttpMethod.Delete
        val Head = CoreColors.HttpMethod.Head
        val Options = CoreColors.HttpMethod.Options
    }
}

package com.azikar24.wormaceptor.feature.viewer.ui.theme

import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem.ThemeColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors as CoreColors

// ============================================================
// LIGHT THEME COLORS - Using shared DesignSystem for consistency
// ============================================================

val md_theme_light_primary = ThemeColors.AccentLight
val md_theme_light_onPrimary = ThemeColors.LightBackground
val md_theme_light_primaryContainer = ThemeColors.AccentSubtleLight
val md_theme_light_onPrimaryContainer = ThemeColors.AccentLight
val md_theme_light_secondary = ThemeColors.AccentLight
val md_theme_light_onSecondary = ThemeColors.LightBackground
val md_theme_light_secondaryContainer = ThemeColors.AccentSubtleLight
val md_theme_light_onSecondaryContainer = ThemeColors.AccentLight
val md_theme_light_tertiary = ThemeColors.AccentLight
val md_theme_light_onTertiary = ThemeColors.LightBackground
val md_theme_light_tertiaryContainer = ThemeColors.AccentSubtleLight
val md_theme_light_onTertiaryContainer = ThemeColors.AccentLight
val md_theme_light_error = ThemeColors.Error
val md_theme_light_errorContainer = ThemeColors.Error.copy(alpha = 0.12f)
val md_theme_light_onError = ThemeColors.LightBackground
val md_theme_light_onErrorContainer = ThemeColors.Error
val md_theme_light_background = ThemeColors.LightBackground
val md_theme_light_onBackground = ThemeColors.LightTextPrimary
val md_theme_light_surface = ThemeColors.LightSurface
val md_theme_light_onSurface = ThemeColors.LightTextPrimary
val md_theme_light_surfaceVariant = ThemeColors.LightSurface
val md_theme_light_onSurfaceVariant = ThemeColors.LightTextSecondary
val md_theme_light_outline = ThemeColors.LightTextTertiary
val md_theme_light_inverseOnSurface = ThemeColors.LightSurface
val md_theme_light_inverseSurface = ThemeColors.DarkSurface
val md_theme_light_inversePrimary = ThemeColors.AccentDark
val md_theme_light_surfaceTint = ThemeColors.AccentLight
val md_theme_light_outlineVariant = ThemeColors.LightTextTertiary
val md_theme_light_scrim = Color(0xFF000000)

// ============================================================
// DARK THEME COLORS - Using shared DesignSystem for consistency
// ============================================================

val md_theme_dark_primary = ThemeColors.AccentDark
val md_theme_dark_onPrimary = ThemeColors.DarkBackground
val md_theme_dark_primaryContainer = ThemeColors.AccentSubtleDark
val md_theme_dark_onPrimaryContainer = ThemeColors.AccentDark
val md_theme_dark_secondary = ThemeColors.AccentDark
val md_theme_dark_onSecondary = ThemeColors.DarkBackground
val md_theme_dark_secondaryContainer = ThemeColors.AccentSubtleDark
val md_theme_dark_onSecondaryContainer = ThemeColors.AccentDark
val md_theme_dark_tertiary = ThemeColors.AccentDark
val md_theme_dark_onTertiary = ThemeColors.DarkBackground
val md_theme_dark_tertiaryContainer = ThemeColors.AccentSubtleDark
val md_theme_dark_onTertiaryContainer = ThemeColors.AccentDark
val md_theme_dark_error = ThemeColors.ErrorDark
val md_theme_dark_errorContainer = ThemeColors.Error.copy(alpha = 0.12f)
val md_theme_dark_onError = ThemeColors.DarkBackground
val md_theme_dark_onErrorContainer = ThemeColors.ErrorDark
val md_theme_dark_background = ThemeColors.DarkBackground
val md_theme_dark_onBackground = ThemeColors.DarkTextPrimary
val md_theme_dark_surface = ThemeColors.DarkSurface
val md_theme_dark_onSurface = ThemeColors.DarkTextPrimary
val md_theme_dark_surfaceVariant = ThemeColors.DarkSurface
val md_theme_dark_onSurfaceVariant = ThemeColors.DarkTextSecondary
val md_theme_dark_outline = ThemeColors.DarkTextTertiary
val md_theme_dark_inverseOnSurface = ThemeColors.DarkSurface
val md_theme_dark_inverseSurface = ThemeColors.LightSurface
val md_theme_dark_inversePrimary = ThemeColors.AccentLight
val md_theme_dark_surfaceTint = ThemeColors.AccentDark
val md_theme_dark_outlineVariant = ThemeColors.DarkTextTertiary
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

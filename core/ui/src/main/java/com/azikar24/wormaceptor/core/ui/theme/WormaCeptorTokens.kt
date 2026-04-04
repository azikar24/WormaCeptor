package com.azikar24.wormaceptor.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.azikar24.wormaceptor.core.ui.theme.tokens.ComposeSyntaxColors
import com.azikar24.wormaceptor.core.ui.theme.tokens.DarkComposeSyntaxColors
import com.azikar24.wormaceptor.core.ui.theme.tokens.DarkSemanticColors
import com.azikar24.wormaceptor.core.ui.theme.tokens.LightComposeSyntaxColors
import com.azikar24.wormaceptor.core.ui.theme.tokens.LightSemanticColors
import com.azikar24.wormaceptor.core.ui.theme.tokens.SemanticColors
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAlpha
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAnimation
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAnimations
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenBorderWidth
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenComponentSize
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenElevation
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenIconSize
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenRadius
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenShapes
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenSpacing
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenTouchTarget
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenTypography
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.core.ui.theme.tokens.Colors as FeatureColors

/**
 * Single entry-point for the entire WormaCeptor design token system.
 *
 * Usage:
 * ```
 * WormaCeptorTokens.Colors.Status.green
 * WormaCeptorTokens.Colors.Memory.heapUsed
 * WormaCeptorTokens.Spacing.md
 * WormaCeptorTokens.semantic().accent
 * WormaCeptorTokens.syntax().keyword
 * ```
 */
object WormaCeptorTokens {

    /** Unified color access. Use WormaCeptorTokens.Colors.Status.green, .Memory.heapUsed, etc. */
    object Colors {
        // Domain-agnostic groups (delegated from FeatureColors.kt)
        val Status = FeatureColors.Status
        val Chart = FeatureColors.Chart
        val Category = FeatureColors.Category
        val ContentType = FeatureColors.ContentType
        val HttpMethod = FeatureColors.HttpMethod
        val Accent = FeatureColors.Accent

        // Tool-specific groups (delegated from ToolColors.kt)
        val Memory = ToolColors.Memory
        val Cpu = ToolColors.Cpu
        val Database = ToolColors.Database
        val WebSocket = ToolColors.WebSocket
        val Location = ToolColors.Location
        val LeakDetection = ToolColors.LeakDetection
        val ThreadViolation = ToolColors.ThreadViolation
        val SecureStorage = ToolColors.SecureStorage
        val LogLevel = ToolColors.LogLevel
        val Fps = ToolColors.Fps
        val RateLimit = ToolColors.RateLimit
        val Preferences = ToolColors.Preferences
        val DependenciesInspector = ToolColors.DependenciesInspector
        val LoadedLibraries = ToolColors.LoadedLibraries
        val Recomposition = ToolColors.Recomposition
        val Crypto = ToolColors.Crypto
        val FileBrowser = ToolColors.FileBrowser
        val PushSimulator = ToolColors.PushSimulator
        val Overlay = ToolColors.Overlay
        val DismissZone = ToolColors.DismissZone
        val Viewer = ToolColors.Viewer
    }

    /** Returns theme-aware semantic colors (background, surface, accent, error, etc.). */
    @Composable
    fun semantic(darkTheme: Boolean = isSystemInDarkTheme()): SemanticColors =
        if (darkTheme) DarkSemanticColors else LightSemanticColors

    /** Returns theme-aware syntax highlighting colors for code display. */
    @Composable
    fun syntax(darkTheme: Boolean = isSystemInDarkTheme()): ComposeSyntaxColors =
        if (darkTheme) DarkComposeSyntaxColors else LightComposeSyntaxColors

    val Spacing = TokenSpacing
    val Radius = TokenRadius
    val Elevation = TokenElevation
    val BorderWidth = TokenBorderWidth
    val Alpha = TokenAlpha
    val Animation = TokenAnimation
    val Animations = TokenAnimations
    val IconSize = TokenIconSize
    val TouchTarget = TokenTouchTarget
    val Shapes = TokenShapes
    val Typography = TokenTypography
    val ComponentSize = TokenComponentSize
}

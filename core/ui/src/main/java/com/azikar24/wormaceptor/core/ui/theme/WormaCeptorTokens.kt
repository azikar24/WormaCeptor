package com.azikar24.wormaceptor.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.azikar24.wormaceptor.core.ui.theme.tokens.ComposeSyntaxColors
import com.azikar24.wormaceptor.core.ui.theme.tokens.DarkComposeSyntaxColors
import com.azikar24.wormaceptor.core.ui.theme.tokens.DarkSemanticColors
import com.azikar24.wormaceptor.core.ui.theme.tokens.LightComposeSyntaxColors
import com.azikar24.wormaceptor.core.ui.theme.tokens.LightSemanticColors
import com.azikar24.wormaceptor.core.ui.theme.tokens.LocalWormaCeptorDensity
import com.azikar24.wormaceptor.core.ui.theme.tokens.SemanticColors
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAlpha
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAnimation
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAnimations
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenBorderWidth
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenComponentSize
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenDensity
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenEasing
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenElevation
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenFocusIndicator
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenIconSize
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenRadius
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenShapes
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenSpacing
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenStateLayer
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

        /** HTTP status code colors (success, redirect, error). */
        val Status = FeatureColors.Status

        /** Chart line and area fill colors. */
        val Chart = FeatureColors.Chart

        /** Request category badge colors. */
        val Category = FeatureColors.Category

        /** MIME content-type indicator colors. */
        val ContentType = FeatureColors.ContentType

        /** HTTP method label colors (GET, POST, etc.). */
        val HttpMethod = FeatureColors.HttpMethod

        /** General accent and highlight colors. */
        val Accent = FeatureColors.Accent

        // Tool-specific groups (delegated from ToolColors.kt)

        /** Memory profiler palette (heap, native, etc.). */
        val Memory = ToolColors.Memory

        /** CPU monitor chart colors. */
        val Cpu = ToolColors.Cpu

        /** Database inspector colors. */
        val Database = ToolColors.Database

        /** WebSocket connection state colors. */
        val WebSocket = ToolColors.WebSocket

        /** Location tracking indicator colors. */
        val Location = ToolColors.Location

        /** Leak detection severity colors. */
        val LeakDetection = ToolColors.LeakDetection

        /** StrictMode thread-violation colors. */
        val ThreadViolation = ToolColors.ThreadViolation

        /** Secure storage entry colors. */
        val SecureStorage = ToolColors.SecureStorage

        /** Log-level severity colors (debug, info, warn, error). */
        val LogLevel = ToolColors.LogLevel

        /** FPS monitor chart colors. */
        val Fps = ToolColors.Fps

        /** Rate-limit indicator colors. */
        val RateLimit = ToolColors.RateLimit

        /** SharedPreferences viewer colors. */
        val Preferences = ToolColors.Preferences

        /** Dependency inspector palette. */
        val DependenciesInspector = ToolColors.DependenciesInspector

        /** Loaded-libraries list colors. */
        val LoadedLibraries = ToolColors.LoadedLibraries

        /** Compose recomposition tracker colors. */
        val Recomposition = ToolColors.Recomposition

        /** Crypto operations indicator colors. */
        val Crypto = ToolColors.Crypto

        /** File browser item-type colors. */
        val FileBrowser = ToolColors.FileBrowser

        /** Floating overlay colors. */
        val Overlay = ToolColors.Overlay

        /** Dismiss-zone indicator colors. */
        val DismissZone = ToolColors.DismissZone

        /** Content viewer colors. */
        val Viewer = ToolColors.Viewer
    }

    /** Spacing scale tokens (xs through xxxl). */
    val Spacing = TokenSpacing

    /** Corner radius tokens. */
    val Radius = TokenRadius

    /** Elevation / shadow tokens. */
    val Elevation = TokenElevation

    /** Border width tokens. */
    val BorderWidth = TokenBorderWidth

    /** Opacity alpha constants. */
    val Alpha = TokenAlpha

    /** Single animation-spec tokens (duration, easing). */
    val Animation = TokenAnimation

    /** Pre-built animation specs for common transitions. */
    val Animations = TokenAnimations

    /** Standard icon size tokens. */
    val IconSize = TokenIconSize

    /** Minimum touch-target size tokens. */
    val TouchTarget = TokenTouchTarget

    /** Shared shape tokens (card, chip, textField, etc.). */
    val Shapes = TokenShapes

    /** Typography style tokens. */
    val Typography = TokenTypography

    /** Component dimension tokens (heights, widths). */
    val ComponentSize = TokenComponentSize

    /** Material 3 state-layer opacities for hover/focus/pressed/dragged. */
    val StateLayer = TokenStateLayer

    /** Focus-ring tokens for keyboard navigation and assistive-tech focus. */
    val FocusIndicator = TokenFocusIndicator

    /**
     * Current density read from [LocalWormaCeptorDensity]. Branches like
     * `if (WormaCeptorTokens.Density == TokenDensity.Compact) ...` let
     * composables adapt their layout without threading density manually.
     * Override at the [WormaCeptorTheme] boundary to retune a subtree.
     */
    val Density: TokenDensity
        @Composable
        @ReadOnlyComposable
        get() = LocalWormaCeptorDensity.current

    /** Motion-easing curves (M3 standard / decelerate / accelerate / emphasized). */
    val Easing = TokenEasing

    /** Returns theme-aware semantic colors (background, surface, accent, error, etc.). */
    @Composable
    fun semantic(darkTheme: Boolean = isSystemInDarkTheme()): SemanticColors =
        if (darkTheme) DarkSemanticColors else LightSemanticColors

    /** Returns theme-aware syntax highlighting colors for code display. */
    @Composable
    fun syntax(darkTheme: Boolean = isSystemInDarkTheme()): ComposeSyntaxColors =
        if (darkTheme) DarkComposeSyntaxColors else LightComposeSyntaxColors
}

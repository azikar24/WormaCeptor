package com.azikar24.wormaceptor.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Compose-friendly syntax colors for code highlighting.
 * Provides both light and dark theme variants with additional UI-specific colors.
 */
@Immutable
data class ComposeSyntaxColors(
    val keyword: Color,
    val string: Color,
    val number: Color,
    val comment: Color,
    val property: Color,
    val tag: Color,
    val operator: Color,
    val punctuation: Color,
    val boolean: Color,
    val type: Color,
    val default: Color,
)

/**
 * Light theme syntax colors - clean, high-contrast for readability.
 * Inspired by VS Code Light+ and IntelliJ IDEA light theme.
 */
val LightComposeSyntaxColors = ComposeSyntaxColors(
    keyword = Color(0xFF0000FF), // Blue for keywords (if, else, class)
    string = Color(0xFF22863A), // Green for strings
    number = Color(0xFF098658), // Teal for numbers
    comment = Color(0xFF6A737D), // Gray for comments
    property = Color(0xFF6F42C1), // Purple for properties/fields
    tag = Color(0xFF22863A), // Green for XML/HTML tags
    operator = Color(0xFF000000), // Black for operators
    punctuation = Color(0xFF000000), // Black for punctuation
    boolean = Color(0xFF0000FF), // Blue for true/false/null
    type = Color(0xFF6F42C1), // Purple for types
    default = Color(0xFF24292E), // Dark gray for default text
)

/**
 * Dark theme syntax colors - easy on the eyes for extended viewing.
 * Inspired by VS Code Dark+ and IntelliJ IDEA dark theme.
 */
val DarkComposeSyntaxColors = ComposeSyntaxColors(
    keyword = Color(0xFFC678DD), // Purple for keywords
    string = Color(0xFF98C379), // Green for strings
    number = Color(0xFFD19A66), // Orange for numbers
    comment = Color(0xFF5C6370), // Gray for comments
    property = Color(0xFFE06C75), // Red for properties/fields
    tag = Color(0xFF61AFEF), // Blue for XML/HTML tags
    operator = Color(0xFFABB2BF), // Light gray for operators
    punctuation = Color(0xFFABB2BF), // Light gray for punctuation
    boolean = Color(0xFFD19A66), // Orange for true/false/null
    type = Color(0xFFE5C07B), // Yellow for types
    default = Color(0xFFABB2BF), // Light gray for default text
)

/**
 * Composition local for syntax colors, allowing components to access
 * the current theme's syntax colors without explicit passing.
 */
val LocalSyntaxColors = staticCompositionLocalOf { LightComposeSyntaxColors }

/**
 * Returns the appropriate syntax colors based on the current theme.
 */
@Composable
fun syntaxColors(darkTheme: Boolean = isSystemInDarkTheme()): ComposeSyntaxColors {
    return if (darkTheme) DarkComposeSyntaxColors else LightComposeSyntaxColors
}

/**
 * Provides syntax colors to the composition tree.
 * Wrap your content with this to make syntax colors available via LocalSyntaxColors.
 */
@Composable
fun ProvideSyntaxColors(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = syntaxColors(darkTheme)
    CompositionLocalProvider(LocalSyntaxColors provides colors) {
        content()
    }
}

/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.domain.contracts.DarkSyntaxColors
import com.azikar24.wormaceptor.domain.contracts.LightSyntaxColors
import com.azikar24.wormaceptor.domain.contracts.SyntaxColors
import com.azikar24.wormaceptor.domain.contracts.TokenType

/**
 * Compose-friendly syntax colors that integrate with the theme system.
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
    // Additional UI colors for the highlighted text component
    val lineNumberText: Color,
    val lineNumberBackground: Color,
    val codeBackground: Color,
    val searchHighlight: Color,
    val searchHighlightCurrent: Color,
    val searchHighlightText: Color,
) {
    /**
     * Returns the color for the given token type.
     */
    fun forType(type: TokenType): Color = when (type) {
        TokenType.KEYWORD -> keyword
        TokenType.STRING -> string
        TokenType.NUMBER -> number
        TokenType.COMMENT -> comment
        TokenType.PROPERTY -> property
        TokenType.TAG -> tag
        TokenType.OPERATOR -> operator
        TokenType.PUNCTUATION -> punctuation
        TokenType.BOOLEAN -> boolean
        TokenType.TYPE -> this.type
        TokenType.DEFAULT -> default
    }

    /**
     * Converts to the domain SyntaxColors interface for use with highlighters.
     */
    fun toSyntaxColors(): SyntaxColors = object : SyntaxColors {
        override val keyword: Color = this@ComposeSyntaxColors.keyword
        override val string: Color = this@ComposeSyntaxColors.string
        override val number: Color = this@ComposeSyntaxColors.number
        override val comment: Color = this@ComposeSyntaxColors.comment
        override val property: Color = this@ComposeSyntaxColors.property
        override val tag: Color = this@ComposeSyntaxColors.tag
        override val operator: Color = this@ComposeSyntaxColors.operator
        override val punctuation: Color = this@ComposeSyntaxColors.punctuation
        override val boolean: Color = this@ComposeSyntaxColors.boolean
        override val typeAnnotation: Color = this@ComposeSyntaxColors.type
        override val default: Color = this@ComposeSyntaxColors.default
    }
}

/**
 * Light theme syntax colors - clean, high-contrast for readability.
 * Inspired by VS Code Light+ and IntelliJ IDEA light theme.
 */
val LightComposeSyntaxColors = ComposeSyntaxColors(
    keyword = LightSyntaxColors.keyword,
    string = LightSyntaxColors.string,
    number = LightSyntaxColors.number,
    comment = LightSyntaxColors.comment,
    property = LightSyntaxColors.property,
    tag = LightSyntaxColors.tag,
    operator = LightSyntaxColors.operator,
    punctuation = LightSyntaxColors.punctuation,
    boolean = LightSyntaxColors.boolean,
    type = LightSyntaxColors.typeAnnotation,
    default = LightSyntaxColors.default,
    // UI-specific colors for light theme
    lineNumberText = SyntaxColorTokens.Light.LineNumberText,
    lineNumberBackground = SyntaxColorTokens.Light.LineNumberBackground,
    codeBackground = SyntaxColorTokens.Light.CodeBackground,
    searchHighlight = SyntaxColorTokens.Light.SearchHighlight,
    searchHighlightCurrent = SyntaxColorTokens.Light.SearchHighlightCurrent,
    searchHighlightText = SyntaxColorTokens.Light.SearchHighlightText,
)

/**
 * Dark theme syntax colors - easy on the eyes for extended viewing.
 * Inspired by VS Code Dark+ and IntelliJ IDEA dark theme.
 */
val DarkComposeSyntaxColors = ComposeSyntaxColors(
    keyword = DarkSyntaxColors.keyword,
    string = DarkSyntaxColors.string,
    number = DarkSyntaxColors.number,
    comment = DarkSyntaxColors.comment,
    property = DarkSyntaxColors.property,
    tag = DarkSyntaxColors.tag,
    operator = DarkSyntaxColors.operator,
    punctuation = DarkSyntaxColors.punctuation,
    boolean = DarkSyntaxColors.boolean,
    type = DarkSyntaxColors.typeAnnotation,
    default = DarkSyntaxColors.default,
    // UI-specific colors for dark theme
    lineNumberText = SyntaxColorTokens.Dark.LineNumberText,
    lineNumberBackground = SyntaxColorTokens.Dark.LineNumberBackground,
    codeBackground = SyntaxColorTokens.Dark.CodeBackground,
    searchHighlight = SyntaxColorTokens.Dark.SearchHighlight,
    searchHighlightCurrent = SyntaxColorTokens.Dark.SearchHighlightCurrent,
    searchHighlightText = SyntaxColorTokens.Dark.SearchHighlightText,
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

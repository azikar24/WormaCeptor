package com.azikar24.wormaceptor.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.azikar24.wormaceptor.domain.contracts.Token
import com.azikar24.wormaceptor.domain.contracts.TokenType

/**
 * Compose-friendly syntax colors for code highlighting.
 * Provides both syntax token colors and UI-specific colors for code display components.
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
    /** Line number text color. */
    val lineNumberText: Color,
    /** Line number gutter background. */
    val lineNumberBackground: Color,
    /** Code area background. */
    val codeBackground: Color,
    /** Search match highlight background. */
    val searchHighlight: Color,
    /** Current search match highlight background. */
    val searchHighlightCurrent: Color,
    /** Text color rendered on top of search highlight backgrounds. */
    val searchHighlightText: Color,
) {
    /** Returns the Compose [Color] for the given [type]. */
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
}

/** Light theme Compose syntax colors derived from [LightSyntaxColors]. */
val LightComposeSyntaxColors = ComposeSyntaxColors(
    keyword = Color(LightSyntaxColors.keyword.toInt()),
    string = Color(LightSyntaxColors.string.toInt()),
    number = Color(LightSyntaxColors.number.toInt()),
    comment = Color(LightSyntaxColors.comment.toInt()),
    property = Color(LightSyntaxColors.property.toInt()),
    tag = Color(LightSyntaxColors.tag.toInt()),
    operator = Color(LightSyntaxColors.operator.toInt()),
    punctuation = Color(LightSyntaxColors.punctuation.toInt()),
    boolean = Color(LightSyntaxColors.boolean.toInt()),
    type = Color(LightSyntaxColors.typeAnnotation.toInt()),
    default = Color(LightSyntaxColors.default.toInt()),
    lineNumberText = SyntaxColorTokens.Light.LineNumberText,
    lineNumberBackground = SyntaxColorTokens.Light.LineNumberBackground,
    codeBackground = SyntaxColorTokens.Light.CodeBackground,
    searchHighlight = SyntaxColorTokens.Light.SearchHighlight,
    searchHighlightCurrent = SyntaxColorTokens.Light.SearchHighlightCurrent,
    searchHighlightText = SyntaxColorTokens.Light.SearchHighlightText,
)

/** Dark theme Compose syntax colors derived from [DarkSyntaxColors]. */
val DarkComposeSyntaxColors = ComposeSyntaxColors(
    keyword = Color(DarkSyntaxColors.keyword.toInt()),
    string = Color(DarkSyntaxColors.string.toInt()),
    number = Color(DarkSyntaxColors.number.toInt()),
    comment = Color(DarkSyntaxColors.comment.toInt()),
    property = Color(DarkSyntaxColors.property.toInt()),
    tag = Color(DarkSyntaxColors.tag.toInt()),
    operator = Color(DarkSyntaxColors.operator.toInt()),
    punctuation = Color(DarkSyntaxColors.punctuation.toInt()),
    boolean = Color(DarkSyntaxColors.boolean.toInt()),
    type = Color(DarkSyntaxColors.typeAnnotation.toInt()),
    default = Color(DarkSyntaxColors.default.toInt()),
    lineNumberText = SyntaxColorTokens.Dark.LineNumberText,
    lineNumberBackground = SyntaxColorTokens.Dark.LineNumberBackground,
    codeBackground = SyntaxColorTokens.Dark.CodeBackground,
    searchHighlight = SyntaxColorTokens.Dark.SearchHighlight,
    searchHighlightCurrent = SyntaxColorTokens.Dark.SearchHighlightCurrent,
    searchHighlightText = SyntaxColorTokens.Dark.SearchHighlightText,
)

/** Returns the appropriate [ComposeSyntaxColors] based on the current theme. */
@Composable
fun syntaxColors(darkTheme: Boolean = isSystemInDarkTheme()): ComposeSyntaxColors {
    return if (darkTheme) DarkComposeSyntaxColors else LightComposeSyntaxColors
}

/** Builds an [AnnotatedString] with syntax-colored spans applied over the raw [code]. */
fun ComposeSyntaxColors.buildHighlightedString(code: String, tokens: List<Token>): AnnotatedString =
    buildAnnotatedString {
        append(code)
        tokens.forEach { token ->
            addStyle(
                SpanStyle(color = forType(token.type)),
                start = token.start,
                end = token.end,
            )
        }
    }

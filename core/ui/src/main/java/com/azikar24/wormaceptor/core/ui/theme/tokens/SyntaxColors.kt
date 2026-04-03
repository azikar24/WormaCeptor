package com.azikar24.wormaceptor.core.ui.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.azikar24.wormaceptor.domain.contracts.Token
import com.azikar24.wormaceptor.domain.contracts.TokenType

/**
 * Compose-friendly syntax colors for code highlighting.
 * Merges the former SyntaxColorScheme, SyntaxColorTokens, and ComposeSyntaxColors into
 * a single token file.
 *
 * @property keyword Language keyword color (e.g. if, fun, class).
 * @property string String literal color.
 * @property number Numeric literal color.
 * @property comment Comment text color.
 * @property property Property/field name color.
 * @property tag XML/HTML tag name color.
 * @property operator Operator symbol color (e.g. +, -, =).
 * @property punctuation Punctuation symbol color (e.g. braces, commas).
 * @property boolean Boolean literal color (true/false).
 * @property type Type annotation color.
 * @property default Default/fallback text color.
 * @property lineNumberText Line number text color.
 * @property lineNumberBackground Line number gutter background color.
 * @property codeBackground Code area background color.
 * @property searchHighlight Search match highlight background color.
 * @property searchHighlightCurrent Current search match highlight background color.
 * @property searchHighlightText Text color rendered on top of search highlight backgrounds.
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
    val lineNumberText: Color,
    val lineNumberBackground: Color,
    val codeBackground: Color,
    val searchHighlight: Color,
    val searchHighlightCurrent: Color,
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

    /** Builds an [AnnotatedString] with syntax-colored spans applied over the raw [code]. */
    fun buildHighlightedString(
        code: String,
        tokens: List<Token>,
    ): AnnotatedString = buildAnnotatedString {
        append(code)
        tokens.forEach { token ->
            addStyle(
                SpanStyle(color = forType(token.type)),
                start = token.start,
                end = token.end,
            )
        }
    }
}

/** Light theme Compose syntax colors. */
internal val LightComposeSyntaxColors = ComposeSyntaxColors(
    keyword = Palette.SyntaxLightKeyword, // 0xFF0033B3
    string = Palette.SyntaxLightString, // 0xFF067D17
    number = Palette.SyntaxLightNumber, // 0xFF1750EB
    comment = Palette.Gray525, // 0xFF8C8C8C
    property = Palette.SyntaxLightProperty, // 0xFF871094
    tag = Palette.SyntaxLightKeyword, // 0xFF0033B3
    operator = Palette.Black, // 0xFF000000
    punctuation = Palette.Black, // 0xFF000000
    boolean = Palette.SyntaxLightKeyword, // 0xFF0033B3
    type = Palette.SyntaxLightTypeAnnotation, // 0xFF008080
    default = Palette.Black, // 0xFF000000
    lineNumberText = Palette.Gray450, // 0xFF9E9E9E
    lineNumberBackground = Palette.Gray100, // 0xFFF5F5F5
    codeBackground = Palette.Gray50, // 0xFFFAFAFA
    searchHighlight = Palette.Yellow200, // 0xFFFFF59D
    searchHighlightCurrent = Palette.Cyan300, // 0xFF4DD0E1
    searchHighlightText = Palette.Gray990, // 0xFF0A0A0A (LightTextPrimary)
)

/** Dark theme Compose syntax colors. */
internal val DarkComposeSyntaxColors = ComposeSyntaxColors(
    keyword = Palette.SyntaxDarkKeyword, // 0xFFCC7832
    string = Palette.SyntaxDarkString, // 0xFF6A8759
    number = Palette.SyntaxDarkNumber, // 0xFF6897BB
    comment = Palette.Gray550, // 0xFF808080
    property = Palette.DeepPurpleA300, // 0xFF9876AA
    tag = Palette.SyntaxDarkTag, // 0xFFE8BF6A
    operator = Palette.SyntaxDarkOperator, // 0xFFA9B7C6
    punctuation = Palette.SyntaxDarkOperator, // 0xFFA9B7C6
    boolean = Palette.SyntaxDarkKeyword, // 0xFFCC7832
    type = Palette.SyntaxDarkNumber, // 0xFF6897BB
    default = Palette.SyntaxDarkOperator, // 0xFFA9B7C6
    lineNumberText = Palette.Gray750, // 0xFF606060
    lineNumberBackground = Palette.Gray950, // 0xFF1E1E1E
    codeBackground = Palette.Gray980, // 0xFF252526
    searchHighlight = Palette.DeepOrange900, // 0xFF613214
    searchHighlightCurrent = Palette.BlueDarkSearch, // 0xFF264F78
    searchHighlightText = Palette.Gray50, // 0xFFFAFAFA (DarkTextPrimary)
)

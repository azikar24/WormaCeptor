package com.azikar24.wormaceptor.feature.filebrowser.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import java.util.Locale

internal const val BytesPerLine = 16

private const val TrueLiteralLength = 4
private const val FalseLiteralLength = 5
private const val NullLiteralLength = 4
private const val HexFirstGroupSize = 8
private const val HexByteMask = 0xFF
private const val AsciiPrintableMin = 32
private const val AsciiPrintableMax = 126

// ----- JSON highlighting -----

@Composable
internal fun highlightJson(json: String): AnnotatedString {
    val colors = WormaCeptorTokens.Colors.FileBrowser.syntaxScheme()
    return buildAnnotatedString {
        var i = 0
        while (i < json.length) {
            i = when {
                json[i] == '"' -> appendJsonString(json, i, colors)
                isJsonNumberStart(json, i) -> appendJsonNumber(json, i, colors)
                json.startsWith("true", i) -> {
                    appendJsonKeyword("true", colors)
                    i + TrueLiteralLength
                }
                json.startsWith("false", i) -> {
                    appendJsonKeyword("false", colors)
                    i + FalseLiteralLength
                }
                json.startsWith("null", i) -> {
                    appendJsonKeyword("null", colors)
                    i + NullLiteralLength
                }
                json[i] in "{}[]" -> {
                    withStyle(SpanStyle(color = colors.jsonBracket, fontWeight = FontWeight.Bold)) {
                        append(json[i])
                    }
                    i + 1
                }
                else -> {
                    append(json[i])
                    i + 1
                }
            }
        }
    }
}

private fun isJsonNumberStart(
    json: String,
    i: Int,
): Boolean = json[i].isDigit() || (json[i] == '-' && i + 1 < json.length && json[i + 1].isDigit())

private fun AnnotatedString.Builder.appendJsonString(
    json: String,
    startIndex: Int,
    colors: ToolColors.FileBrowser.SyntaxScheme,
): Int {
    var i = startIndex + 1
    while (i < json.length && json[i] != '"') {
        if (json[i] == '\\' && i + 1 < json.length) i++
        i++
    }
    if (i < json.length) i++ // Include closing quote
    val str = json.substring(startIndex, i)

    var j = i
    while (j < json.length && json[j].isWhitespace()) j++
    val isKey = j < json.length && json[j] == ':'

    withStyle(SpanStyle(color = if (isKey) colors.jsonKey else colors.jsonString)) {
        append(str)
    }
    return i
}

private fun isJsonNumericChar(c: Char): Boolean =
    c.isDigit() || c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-'

private fun AnnotatedString.Builder.appendJsonNumber(
    json: String,
    startIndex: Int,
    colors: ToolColors.FileBrowser.SyntaxScheme,
): Int {
    var i = startIndex
    if (json[i] == '-') i++
    while (i < json.length && isJsonNumericChar(json[i])) {
        i++
    }
    withStyle(SpanStyle(color = colors.jsonNumber)) {
        append(json.substring(startIndex, i))
    }
    return i
}

private fun AnnotatedString.Builder.appendJsonKeyword(
    keyword: String,
    colors: ToolColors.FileBrowser.SyntaxScheme,
) {
    withStyle(SpanStyle(color = colors.jsonBoolNull, fontWeight = FontWeight.Bold)) {
        append(keyword)
    }
}

// ----- XML highlighting -----

@Composable
internal fun highlightXml(xml: String): AnnotatedString {
    val colors = WormaCeptorTokens.Colors.FileBrowser.syntaxScheme()
    return buildAnnotatedString {
        var i = 0
        while (i < xml.length) {
            i = if (xml[i] == '<') {
                appendXmlTag(xml, i, colors)
            } else {
                appendXmlTextContent(xml, i, colors)
            }
        }
    }
}

private fun AnnotatedString.Builder.appendXmlTag(
    xml: String,
    startIndex: Int,
    colors: ToolColors.FileBrowser.SyntaxScheme,
): Int {
    var i = startIndex + 1

    if (i < xml.length && xml[i] == '!') {
        return appendXmlSpecialTag(xml, startIndex, colors)
    }
    if (i < xml.length && xml[i] == '?') {
        return appendXmlProcessingInstruction(xml, startIndex, colors)
    }

    withStyle(SpanStyle(color = colors.xmlTag)) { append("<") }

    if (i < xml.length && xml[i] == '/') {
        withStyle(SpanStyle(color = colors.xmlTag)) { append("/") }
        i++
    }

    i = appendXmlTagName(xml, i, colors)
    i = appendXmlAttributes(xml, i, colors)

    if (i < xml.length && xml[i] == '>') {
        withStyle(SpanStyle(color = colors.xmlTag)) { append(">") }
        i++
    }
    return i
}

private fun AnnotatedString.Builder.appendXmlSpecialTag(
    xml: String,
    startIndex: Int,
    colors: ToolColors.FileBrowser.SyntaxScheme,
): Int {
    var i = startIndex + 1
    while (i < xml.length && xml[i] != '>') i++
    if (i < xml.length) i++
    withStyle(SpanStyle(color = colors.xmlComment)) {
        append(xml.substring(startIndex, i))
    }
    return i
}

private fun AnnotatedString.Builder.appendXmlProcessingInstruction(
    xml: String,
    startIndex: Int,
    colors: ToolColors.FileBrowser.SyntaxScheme,
): Int {
    var i = startIndex + 1
    while (i < xml.length && !(xml[i - 1] == '?' && xml[i] == '>')) i++
    if (i < xml.length) i++
    withStyle(SpanStyle(color = colors.xmlComment)) {
        append(xml.substring(startIndex, i))
    }
    return i
}

private fun isXmlTagNameTerminator(c: Char): Boolean = c.isWhitespace() || c == '>' || c == '/'

private fun AnnotatedString.Builder.appendXmlTagName(
    xml: String,
    startIndex: Int,
    colors: ToolColors.FileBrowser.SyntaxScheme,
): Int {
    var i = startIndex
    while (i < xml.length && !isXmlTagNameTerminator(xml[i])) i++
    withStyle(SpanStyle(color = colors.xmlTag, fontWeight = FontWeight.Bold)) {
        append(xml.substring(startIndex, i))
    }
    return i
}

private fun isXmlAttrNameTerminator(c: Char): Boolean = c.isWhitespace() || c == '=' || c == '>' || c == '/'

private fun AnnotatedString.Builder.appendXmlAttributes(
    xml: String,
    startIndex: Int,
    colors: ToolColors.FileBrowser.SyntaxScheme,
): Int {
    var i = startIndex
    while (i < xml.length && xml[i] != '>') {
        when {
            xml[i].isWhitespace() -> {
                append(xml[i])
                i++
            }
            xml[i] == '/' -> {
                withStyle(SpanStyle(color = colors.xmlTag)) { append("/") }
                i++
            }
            xml[i] == '=' -> {
                append("=")
                i++
            }
            xml[i] == '"' || xml[i] == '\'' -> {
                i = appendXmlAttrValue(xml, i, colors)
            }
            else -> {
                i = appendXmlAttrName(xml, i, colors)
            }
        }
    }
    return i
}

private fun AnnotatedString.Builder.appendXmlAttrValue(
    xml: String,
    startIndex: Int,
    colors: ToolColors.FileBrowser.SyntaxScheme,
): Int {
    val quote = xml[startIndex]
    var i = startIndex + 1
    while (i < xml.length && xml[i] != quote) i++
    if (i < xml.length) i++
    withStyle(SpanStyle(color = colors.xmlAttrValue)) {
        append(xml.substring(startIndex, i))
    }
    return i
}

private fun AnnotatedString.Builder.appendXmlAttrName(
    xml: String,
    startIndex: Int,
    colors: ToolColors.FileBrowser.SyntaxScheme,
): Int {
    var i = startIndex
    while (i < xml.length && !isXmlAttrNameTerminator(xml[i])) i++
    withStyle(SpanStyle(color = colors.xmlAttrName)) {
        append(xml.substring(startIndex, i))
    }
    return i
}

private fun AnnotatedString.Builder.appendXmlTextContent(
    xml: String,
    startIndex: Int,
    colors: ToolColors.FileBrowser.SyntaxScheme,
): Int {
    var i = startIndex
    while (i < xml.length && xml[i] != '<') i++
    val content = xml.substring(startIndex, i)
    if (content.isNotBlank()) {
        withStyle(SpanStyle(color = colors.xmlContent)) {
            append(content)
        }
    } else {
        append(content)
    }
    return i
}

// ----- Hex viewer -----

internal fun buildHexLine(
    bytes: ByteArray,
    lineStart: Int,
): String {
    val builder = StringBuilder()

    builder.append(String.format(Locale.US, "%08X  ", lineStart))

    for (i in 0 until HexFirstGroupSize) {
        val index = lineStart + i
        if (index < bytes.size) {
            builder.append(String.format(Locale.US, "%02X ", bytes[index].toInt() and HexByteMask))
        } else {
            builder.append("   ")
        }
    }

    builder.append(" ")

    for (i in HexFirstGroupSize until BytesPerLine) {
        val index = lineStart + i
        if (index < bytes.size) {
            builder.append(String.format(Locale.US, "%02X ", bytes[index].toInt() and HexByteMask))
        } else {
            builder.append("   ")
        }
    }

    builder.append(" ")

    for (i in 0 until BytesPerLine) {
        val index = lineStart + i
        if (index < bytes.size) {
            val byte = bytes[index].toInt() and HexByteMask
            builder.append(if (byte in AsciiPrintableMin..AsciiPrintableMax) byte.toChar() else '.')
        }
    }

    return builder.toString()
}

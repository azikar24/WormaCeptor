package com.azikar24.wormaceptor.feature.filebrowser.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import java.util.Locale

internal const val BYTES_PER_LINE = 16

@Composable
internal fun highlightJson(json: String): AnnotatedString {
    val colors = WormaCeptorTokens.Colors.FileBrowser.syntaxScheme()
    return buildAnnotatedString {
        var i = 0
        while (i < json.length) {
            when {
                // String (key or value)
                json[i] == '"' -> {
                    val start = i
                    i++
                    while (i < json.length && json[i] != '"') {
                        if (json[i] == '\\' && i + 1 < json.length) i++
                        i++
                    }
                    i++ // Include closing quote
                    val str = json.substring(start, minOf(i, json.length))

                    // Check if this is a key (followed by colon)
                    var j = i
                    while (j < json.length && json[j].isWhitespace()) j++
                    val isKey = j < json.length && json[j] == ':'

                    withStyle(SpanStyle(color = if (isKey) colors.jsonKey else colors.jsonString)) {
                        append(str)
                    }
                }
                // Number
                json[i].isDigit() || json[i] == '-' && i + 1 < json.length && json[i + 1].isDigit() -> {
                    val start = i
                    if (json[i] == '-') i++
                    while (
                        i < json.length &&
                        (
                            json[i].isDigit() || json[i] == '.' ||
                                json[i] == 'e' || json[i] == 'E' ||
                                json[i] == '+' || json[i] == '-'
                            )
                    ) {
                        i++
                    }
                    withStyle(SpanStyle(color = colors.jsonNumber)) {
                        append(json.substring(start, i))
                    }
                }
                // Boolean or null
                json.substring(i).startsWith("true") -> {
                    withStyle(SpanStyle(color = colors.jsonBoolNull, fontWeight = FontWeight.Bold)) {
                        append("true")
                    }
                    i += 4
                }
                json.substring(i).startsWith("false") -> {
                    withStyle(SpanStyle(color = colors.jsonBoolNull, fontWeight = FontWeight.Bold)) {
                        append("false")
                    }
                    i += 5
                }
                json.substring(i).startsWith("null") -> {
                    withStyle(SpanStyle(color = colors.jsonBoolNull, fontWeight = FontWeight.Bold)) {
                        append("null")
                    }
                    i += 4
                }
                // Brackets and braces
                json[i] in "{}[]" -> {
                    withStyle(SpanStyle(color = colors.jsonBracket, fontWeight = FontWeight.Bold)) {
                        append(json[i])
                    }
                    i++
                }
                else -> {
                    append(json[i])
                    i++
                }
            }
        }
    }
}

@Composable
internal fun highlightXml(xml: String): AnnotatedString {
    val colors = WormaCeptorTokens.Colors.FileBrowser.syntaxScheme()
    return buildAnnotatedString {
        var i = 0
        while (i < xml.length) {
            if (xml[i] == '<') {
                val start = i
                i++

                // Check for comment, CDATA, or processing instruction
                if (i < xml.length && xml[i] == '!') {
                    // Comment or CDATA
                    while (i < xml.length && xml[i] != '>') i++
                    i++
                    withStyle(SpanStyle(color = colors.xmlComment)) {
                        append(xml.substring(start, minOf(i, xml.length)))
                    }
                } else if (i < xml.length && xml[i] == '?') {
                    // Processing instruction
                    while (i < xml.length && !(xml[i - 1] == '?' && xml[i] == '>')) i++
                    i++
                    withStyle(SpanStyle(color = colors.xmlComment)) {
                        append(xml.substring(start, minOf(i, xml.length)))
                    }
                } else {
                    // Regular tag
                    withStyle(SpanStyle(color = colors.xmlTag)) {
                        append("<")
                    }

                    // Closing tag slash
                    if (i < xml.length && xml[i] == '/') {
                        withStyle(SpanStyle(color = colors.xmlTag)) {
                            append("/")
                        }
                        i++
                    }

                    // Tag name
                    val nameStart = i
                    while (
                        i < xml.length &&
                        !xml[i].isWhitespace() &&
                        xml[i] != '>' &&
                        xml[i] != '/'
                        ) i++
                    withStyle(SpanStyle(color = colors.xmlTag, fontWeight = FontWeight.Bold)) {
                        append(xml.substring(nameStart, i))
                    }

                    // Attributes
                    while (i < xml.length && xml[i] != '>') {
                        if (xml[i].isWhitespace()) {
                            append(xml[i])
                            i++
                        } else if (xml[i] == '/') {
                            withStyle(SpanStyle(color = colors.xmlTag)) {
                                append("/")
                            }
                            i++
                        } else if (xml[i] == '=') {
                            append("=")
                            i++
                        } else if (xml[i] == '"' || xml[i] == '\'') {
                            val quote = xml[i]
                            val attrStart = i
                            i++
                            while (i < xml.length && xml[i] != quote) i++
                            i++
                            withStyle(SpanStyle(color = colors.xmlAttrValue)) {
                                append(xml.substring(attrStart, minOf(i, xml.length)))
                            }
                        } else {
                            // Attribute name
                            val attrNameStart = i
                            while (
                                i < xml.length &&
                                !xml[i].isWhitespace() &&
                                xml[i] != '=' &&
                                xml[i] != '>' &&
                                xml[i] != '/'
                                ) i++
                            withStyle(SpanStyle(color = colors.xmlAttrName)) {
                                append(xml.substring(attrNameStart, i))
                            }
                        }
                    }

                    if (i < xml.length && xml[i] == '>') {
                        withStyle(SpanStyle(color = colors.xmlTag)) {
                            append(">")
                        }
                        i++
                    }
                }
            } else {
                // Text content
                val contentStart = i
                while (i < xml.length && xml[i] != '<') i++
                val content = xml.substring(contentStart, i)
                if (content.isNotBlank()) {
                    withStyle(SpanStyle(color = colors.xmlContent)) {
                        append(content)
                    }
                } else {
                    append(content)
                }
            }
        }
    }
}

internal fun buildHexLine(
    bytes: ByteArray,
    lineStart: Int,
): String {
    val builder = StringBuilder()

    // Address column
    builder.append(String.format(Locale.US, "%08X  ", lineStart))

    // Hex bytes - first 8 bytes
    for (i in 0 until 8) {
        val index = lineStart + i
        if (index < bytes.size) {
            builder.append(String.format(Locale.US, "%02X ", bytes[index].toInt() and 0xFF))
        } else {
            builder.append("   ")
        }
    }

    builder.append(" ")

    // Hex bytes - second 8 bytes
    for (i in 8 until BYTES_PER_LINE) {
        val index = lineStart + i
        if (index < bytes.size) {
            builder.append(String.format(Locale.US, "%02X ", bytes[index].toInt() and 0xFF))
        } else {
            builder.append("   ")
        }
    }

    builder.append(" ")

    // ASCII representation
    for (i in 0 until BYTES_PER_LINE) {
        val index = lineStart + i
        if (index < bytes.size) {
            val byte = bytes[index].toInt() and 0xFF
            builder.append(if (byte in 32..126) byte.toChar() else '.')
        }
    }

    return builder.toString()
}

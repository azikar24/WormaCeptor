package com.azikar24.wormaceptor.feature.viewer.ui.components.body

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.ComposeSyntaxColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.syntaxColors
import com.azikar24.wormaceptor.domain.contracts.XmlFormatter
import org.koin.java.KoinJavaComponent.get
import java.util.Locale

/**
 * A formatted view for XML content with proper indentation and syntax highlighting.
 * Displays XML tags with color coding for tags, attributes, and values.
 */
@Composable
fun XmlTreeView(
    xmlString: String,
    modifier: Modifier = Modifier,
    colors: ComposeSyntaxColors = syntaxColors(),
) {
    val formattedLines = remember(xmlString) {
        try {
            val formatter: XmlFormatter = get(XmlFormatter::class.java)
            formatter.format(xmlString)
        } catch (_: RuntimeException) {
            listOf(xmlString)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.codeBackground, WormaCeptorDesignSystem.Shapes.chip)
            .padding(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                formattedLines.forEachIndexed { index, line ->
                    XmlLineView(
                        line = line,
                        lineNumber = index + 1,
                        colors = colors,
                    )
                }
            }
        }
    }
}

@Composable
private fun XmlLineView(
    line: String,
    lineNumber: Int,
    colors: ComposeSyntaxColors,
) {
    Row(
        modifier = Modifier.padding(vertical = 1.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = String.format(Locale.US, "%4d", lineNumber),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
            ),
            color = colors.lineNumberText,
            modifier = Modifier.padding(end = WormaCeptorDesignSystem.Spacing.sm),
        )

        Text(
            text = highlightXmlLine(line, colors),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
            ),
        )
    }
}

/**
 * Applies syntax highlighting to an XML line.
 */
@Composable
private fun highlightXmlLine(
    line: String,
    colors: ComposeSyntaxColors,
) = buildAnnotatedString {
    var i = 0
    while (i < line.length) {
        when {
            line.startsWith("<?", i) -> {
                val end = line.indexOf("?>", i)
                if (end != -1) {
                    withStyle(SpanStyle(color = colors.comment)) {
                        append(line.substring(i, end + 2))
                    }
                    i = end + 2
                } else {
                    append(line.substring(i))
                    break
                }
            }
            line.startsWith("<!--", i) -> {
                val end = line.indexOf("-->", i)
                if (end != -1) {
                    withStyle(SpanStyle(color = colors.comment)) {
                        append(line.substring(i, end + 3))
                    }
                    i = end + 3
                } else {
                    withStyle(SpanStyle(color = colors.comment)) {
                        append(line.substring(i))
                    }
                    break
                }
            }
            line.startsWith("<![CDATA[", i) -> {
                val end = line.indexOf("]]>", i)
                if (end != -1) {
                    withStyle(SpanStyle(color = colors.tag)) {
                        append("<![CDATA[")
                    }
                    withStyle(SpanStyle(color = colors.string)) {
                        append(line.substring(i + 9, end))
                    }
                    withStyle(SpanStyle(color = colors.tag)) {
                        append("]]>")
                    }
                    i = end + 3
                } else {
                    append(line.substring(i))
                    break
                }
            }
            line[i] == '<' -> {
                val tagEnd = line.indexOf('>', i)
                if (tagEnd != -1) {
                    val tag = line.substring(i, tagEnd + 1)
                    append(highlightXmlTag(tag, colors))
                    i = tagEnd + 1
                } else {
                    append(line.substring(i))
                    break
                }
            }
            else -> {
                val nextTag = line.indexOf('<', i)
                val text = if (nextTag != -1) {
                    line.substring(i, nextTag)
                } else {
                    line.substring(i)
                }
                withStyle(SpanStyle(color = colors.default)) {
                    append(text)
                }
                i += text.length
            }
        }
    }
}

/**
 * Highlights a single XML tag with attributes.
 */
@Composable
private fun highlightXmlTag(
    tag: String,
    colors: ComposeSyntaxColors,
) = buildAnnotatedString {
    withStyle(SpanStyle(color = colors.punctuation)) {
        append("<")
    }

    val content = tag.drop(1).dropLast(1).let {
        if (it.endsWith("/")) it.dropLast(1) else it
    }
    val isSelfClosing = tag.endsWith("/>")
    val isClosing = tag.startsWith("</")

    val parts = content.split(" ", limit = 2)
    val tagName = parts[0].removePrefix("/")
    withStyle(SpanStyle(color = colors.tag)) {
        if (isClosing) append("/")
        append(tagName)
    }

    if (parts.size > 1) {
        val attributes = parts[1]
        highlightAttributes(attributes, colors).forEach { append(it) }
    }

    withStyle(SpanStyle(color = colors.punctuation)) {
        if (isSelfClosing) append("/")
        append(">")
    }
}

/**
 * Parses and highlights XML attributes.
 */
@Composable
private fun highlightAttributes(
    attributes: String,
    colors: ComposeSyntaxColors,
): List<CharSequence> {
    val result = mutableListOf<CharSequence>()
    val attrRegex = Regex("""(\s*)(\w+(?::\w+)?)\s*=\s*("[^"]*"|'[^']*')""")

    var lastEnd = 0
    attrRegex.findAll(attributes).forEach { match ->
        if (match.range.first > lastEnd) {
            result.add(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = colors.default)) {
                        append(attributes.substring(lastEnd, match.range.first))
                    }
                },
            )
        }

        val (whitespace, name, value) = match.destructured
        result.add(
            buildAnnotatedString {
                append(whitespace)
                withStyle(SpanStyle(color = colors.property)) {
                    append(name)
                }
                withStyle(SpanStyle(color = colors.punctuation)) {
                    append("=")
                }
                withStyle(SpanStyle(color = colors.string)) {
                    append(value)
                }
            },
        )

        lastEnd = match.range.last + 1
    }

    if (lastEnd < attributes.length) {
        result.add(
            buildAnnotatedString {
                withStyle(SpanStyle(color = colors.default)) {
                    append(attributes.substring(lastEnd))
                }
            },
        )
    }

    return result
}

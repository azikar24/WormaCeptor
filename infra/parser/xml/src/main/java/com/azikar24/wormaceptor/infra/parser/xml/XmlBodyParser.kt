package com.azikar24.wormaceptor.infra.parser.xml

import com.azikar24.wormaceptor.domain.contracts.BaseBodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ParsedBody
import com.azikar24.wormaceptor.domain.contracts.XmlFormatter

/**
 * Parser and formatter for XML content.
 *
 * Formats raw XML with proper indentation for display.
 * Also implements [XmlFormatter] for typed access from viewer composables.
 */
class XmlBodyParser : BaseBodyParser(), XmlFormatter {

    override val supportedContentTypes: List<String> = listOf(
        "application/xml",
        "text/xml",
        "application/xhtml+xml",
        "application/soap+xml",
        "application/rss+xml",
        "application/atom+xml",
    )

    override val priority: Int = PRIORITY

    override val defaultContentType: ContentType = ContentType.XML

    override fun canParse(
        contentType: String?,
        body: ByteArray,
    ): Boolean {
        if (contentType != null) {
            val mime = contentType.split(";").firstOrNull()?.trim()?.lowercase() ?: ""
            if (mime.contains("xml") || mime.endsWith("+xml")) {
                return true
            }
        }
        return false
    }

    override fun parseBody(body: ByteArray): ParsedBody {
        val text = String(body, Charsets.UTF_8)
        val formatted = format(text).joinToString("\n")
        return ParsedBody(
            formatted = formatted,
            contentType = ContentType.XML,
            isValid = true,
        )
    }

    @Suppress("CyclomaticComplexity", "NestedBlockDepth")
    override fun format(xml: String): List<String> {
        val result = mutableListOf<String>()
        var indent = 0
        val indentString = "  "

        var i = 0
        val builder = StringBuilder()

        while (i < xml.length) {
            val c = xml[i]

            if (c == '<') {
                val text = builder.toString().trim()
                if (text.isNotEmpty()) {
                    result.add(indentString.repeat(indent) + text)
                }
                builder.clear()

                val tagEnd = xml.indexOf('>', i)
                if (tagEnd == -1) {
                    builder.append(c)
                    i++
                    continue
                }

                val tag = xml.substring(i, tagEnd + 1)

                when {
                    tag.startsWith("<?") -> {
                        result.add(indentString.repeat(indent) + tag)
                    }
                    tag.startsWith("<!--") -> {
                        val commentEnd = xml.indexOf("-->", i)
                        if (commentEnd != -1) {
                            val comment = xml.substring(i, commentEnd + 3)
                            result.add(indentString.repeat(indent) + comment)
                            i = commentEnd + 2
                        } else {
                            result.add(indentString.repeat(indent) + tag)
                        }
                    }
                    tag.startsWith("<![CDATA[") -> {
                        val cdataEnd = xml.indexOf("]]>", i)
                        if (cdataEnd != -1) {
                            val cdata = xml.substring(i, cdataEnd + 3)
                            result.add(indentString.repeat(indent) + cdata)
                            i = cdataEnd + 2
                        } else {
                            result.add(indentString.repeat(indent) + tag)
                        }
                    }
                    tag.startsWith("</") -> {
                        indent = maxOf(0, indent - 1)
                        result.add(indentString.repeat(indent) + tag)
                    }
                    tag.endsWith("/>") -> {
                        result.add(indentString.repeat(indent) + tag)
                    }
                    tag.uppercase().startsWith("<!DOCTYPE") -> {
                        result.add(indentString.repeat(indent) + tag)
                    }
                    else -> {
                        result.add(indentString.repeat(indent) + tag)
                        indent++
                    }
                }

                i = tagEnd
            } else {
                builder.append(c)
            }
            i++
        }

        val remaining = builder.toString().trim()
        if (remaining.isNotEmpty()) {
            result.add(indentString.repeat(indent) + remaining)
        }

        return result.filter { it.isNotBlank() }
    }

    companion object {
        private const val PRIORITY = 200
    }
}

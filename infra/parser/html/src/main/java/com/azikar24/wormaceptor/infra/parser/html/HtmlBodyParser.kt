package com.azikar24.wormaceptor.infra.parser.html

import com.azikar24.wormaceptor.domain.contracts.BaseBodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ParsedBody

/**
 * Parser for HTML content.
 *
 * Features:
 * - Clean formatting with indentation
 * - Tag structure preservation
 * - Metadata extraction (title, meta tags)
 */
class HtmlBodyParser(
    private val indentString: String = "  ",
) : BaseBodyParser() {

    override val supportedContentTypes: List<String> = listOf(
        "text/html",
        "application/xhtml+xml",
    )

    override val priority: Int = 230

    override val defaultContentType: ContentType = ContentType.HTML

    // Tags that should not have their content formatted
    private val preformattedTags = setOf(
        "pre",
        "code",
        "script",
        "style",
        "textarea",
    )

    // Void elements (self-closing)
    private val voidElements = setOf(
        "area", "base", "br", "col", "embed", "hr", "img", "input",
        "link", "meta", "param", "source", "track", "wbr",
    )

    override fun canParse(
        contentType: String?,
        body: ByteArray,
    ): Boolean {
        // Check content type first
        if (contentType != null) {
            val mimeType = contentType.split(";").firstOrNull()?.trim()?.lowercase()
            if (mimeType != null && supportedContentTypes.any { it == mimeType }) {
                return true
            }
        }

        // Content inspection
        if (body.isEmpty()) return false

        val content = String(body, Charsets.UTF_8).trim().lowercase()
        return content.startsWith("<!doctype html") ||
            content.startsWith("<html") ||
            content.contains("<html") && content.contains("</html>")
    }

    override fun parseBody(body: ByteArray): ParsedBody {
        return try {
            val html = String(body, Charsets.UTF_8)
            val metadata = extractMetadata(html)
            val formatted = formatHtml(html)

            ParsedBody(
                formatted = formatted,
                contentType = ContentType.HTML,
                metadata = metadata,
                isValid = true,
            )
        } catch (e: Exception) {
            ParsedBody(
                formatted = String(body, Charsets.UTF_8),
                contentType = ContentType.HTML,
                isValid = false,
                errorMessage = "HTML parsing error: ${e.message}",
            )
        }
    }

    private fun formatHtml(html: String): String {
        val result = StringBuilder()
        var depth = 0
        var i = 0
        var inPreformatted = false
        var preformattedTag: String? = null

        while (i < html.length) {
            when {
                // Handle comments
                html.startsWith("<!--", i) -> {
                    val endComment = html.indexOf("-->", i)
                    if (endComment == -1) {
                        result.append(html.substring(i))
                        break
                    }
                    if (!inPreformatted) {
                        result.append("\n")
                        result.append(indentString.repeat(depth))
                    }
                    result.append(html.substring(i, endComment + 3))
                    i = endComment + 3
                }

                // Handle DOCTYPE
                html.substring(i).lowercase().startsWith("<!doctype") -> {
                    val endDoctype = html.indexOf('>', i)
                    if (endDoctype == -1) {
                        result.append(html.substring(i))
                        break
                    }
                    result.append(html.substring(i, endDoctype + 1))
                    result.append("\n")
                    i = endDoctype + 1
                }

                // Handle closing tags
                html.startsWith("</", i) -> {
                    val endTag = html.indexOf('>', i)
                    if (endTag == -1) {
                        result.append(html.substring(i))
                        break
                    }
                    val tagName = html.substring(i + 2, endTag).trim().lowercase()

                    // Check if exiting preformatted mode
                    if (inPreformatted && tagName == preformattedTag) {
                        inPreformatted = false
                        preformattedTag = null
                        result.append(html.substring(i, endTag + 1))
                    } else if (inPreformatted) {
                        result.append(html.substring(i, endTag + 1))
                    } else {
                        depth = maxOf(0, depth - 1)
                        if (result.isNotEmpty() && !result.endsWith("\n")) {
                            result.append("\n")
                        }
                        result.append(indentString.repeat(depth))
                        result.append(html.substring(i, endTag + 1))
                    }
                    i = endTag + 1
                }

                // Handle opening tags
                html.startsWith("<", i) -> {
                    val endTag = findTagEnd(html, i)
                    if (endTag == -1) {
                        result.append(html.substring(i))
                        break
                    }

                    val tagContent = html.substring(i + 1, endTag)
                    val tagName = extractTagName(tagContent).lowercase()
                    val isSelfClosing = tagContent.trimEnd().endsWith("/") || voidElements.contains(tagName)

                    if (!inPreformatted) {
                        if (result.isNotEmpty() && !result.endsWith("\n")) {
                            result.append("\n")
                        }
                        result.append(indentString.repeat(depth))
                    }

                    result.append("<")
                    result.append(tagContent)
                    result.append(">")

                    if (preformattedTags.contains(tagName) && !isSelfClosing) {
                        inPreformatted = true
                        preformattedTag = tagName
                    } else if (!isSelfClosing && !inPreformatted) {
                        depth++
                    }

                    i = endTag + 1
                }

                // Handle text content
                else -> {
                    val nextTag = html.indexOf('<', i)
                    val text = if (nextTag == -1) {
                        html.substring(i)
                    } else {
                        html.substring(i, nextTag)
                    }

                    if (inPreformatted) {
                        result.append(text)
                    } else {
                        val trimmed = text.trim()
                        if (trimmed.isNotEmpty()) {
                            if (result.isNotEmpty() && !result.endsWith("\n") && !result.endsWith(">")) {
                                result.append("\n")
                                result.append(indentString.repeat(depth))
                            } else if (result.endsWith(">")) {
                                // Inline text after tag
                            }
                            result.append(trimmed)
                        }
                    }

                    i = if (nextTag == -1) html.length else nextTag
                }
            }
        }

        return result.toString().trim()
    }

    private fun findTagEnd(
        html: String,
        start: Int,
    ): Int {
        var i = start + 1
        var inQuote = false
        var quoteChar = ' '

        while (i < html.length) {
            val c = html[i]
            when {
                !inQuote && (c == '"' || c == '\'') -> {
                    inQuote = true
                    quoteChar = c
                }
                inQuote && c == quoteChar -> {
                    inQuote = false
                }
                !inQuote && c == '>' -> {
                    return i
                }
            }
            i++
        }
        return -1
    }

    private fun extractTagName(tagContent: String): String {
        val trimmed = tagContent.trim()
        val spaceIndex = trimmed.indexOfFirst { it.isWhitespace() }
        return if (spaceIndex == -1) {
            trimmed.trimEnd('/')
        } else {
            trimmed.substring(0, spaceIndex)
        }
    }

    private fun extractMetadata(html: String): Map<String, String> {
        val metadata = mutableMapOf<String, String>()

        // Extract title
        val titleRegex = """<title[^>]*>([^<]*)</title>""".toRegex(RegexOption.IGNORE_CASE)
        titleRegex.find(html)?.let {
            metadata["title"] = it.groupValues[1].trim()
        }

        // Extract meta description
        val descRegex = """<meta\s+name\s*=\s*["']description["']\s+content\s*=\s*["']([^"']*)["']""".toRegex(
            RegexOption.IGNORE_CASE,
        )
        descRegex.find(html)?.let {
            metadata["description"] = it.groupValues[1]
        }

        // Also check alternate attribute order
        val descRegex2 = """<meta\s+content\s*=\s*["']([^"']*)["']\s+name\s*=\s*["']description["']""".toRegex(
            RegexOption.IGNORE_CASE,
        )
        if (!metadata.containsKey("description")) {
            descRegex2.find(html)?.let {
                metadata["description"] = it.groupValues[1]
            }
        }

        // Extract charset
        val charsetRegex = """<meta\s+charset\s*=\s*["']([^"']*)["']""".toRegex(RegexOption.IGNORE_CASE)
        charsetRegex.find(html)?.let {
            metadata["charset"] = it.groupValues[1]
        }

        // Extract viewport
        val viewportRegex = """<meta\s+name\s*=\s*["']viewport["']\s+content\s*=\s*["']([^"']*)["']""".toRegex(
            RegexOption.IGNORE_CASE,
        )
        viewportRegex.find(html)?.let {
            metadata["viewport"] = it.groupValues[1]
        }

        // Detect doctype
        val doctypeRegex = """<!DOCTYPE\s+(\w+)""".toRegex(RegexOption.IGNORE_CASE)
        doctypeRegex.find(html)?.let {
            metadata["doctype"] = it.groupValues[1]
        }

        return metadata
    }
}

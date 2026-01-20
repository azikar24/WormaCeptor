package com.azikar24.wormaceptor.infra.parser.xml

import com.azikar24.wormaceptor.domain.contracts.BodyParser
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.contracts.ParsedBody
import com.azikar24.wormaceptor.domain.contracts.emptyParsedBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.io.StringWriter

/**
 * Parser for XML content.
 *
 * Features:
 * - Pretty-print with proper indentation
 * - Namespace-aware formatting
 * - DTD/schema reference display in metadata
 */
class XmlBodyParser(
    private val indentString: String = "  ",
) : BodyParser {

    override val supportedContentTypes: List<String> = listOf(
        "application/xml",
        "text/xml",
        "application/xhtml+xml",
        "application/atom+xml",
        "application/rss+xml",
        "application/soap+xml",
    )

    override val priority: Int = 240

    override fun canParse(contentType: String?, body: ByteArray): Boolean {
        // Check content type first
        if (contentType != null) {
            val mimeType = contentType.split(";").firstOrNull()?.trim()?.lowercase()
            if (mimeType != null && supportedContentTypes.any { it == mimeType }) {
                return true
            }
            // Also check for +xml suffix
            if (mimeType?.endsWith("+xml") == true) {
                return true
            }
        }

        // Content inspection
        if (body.isEmpty()) return false

        val trimmed = String(body, Charsets.UTF_8).trimStart()
        return trimmed.startsWith("<?xml") ||
            (trimmed.startsWith("<") && !trimmed.startsWith("<!DOCTYPE html", ignoreCase = true))
    }

    override fun parse(body: ByteArray): ParsedBody {
        if (body.isEmpty()) {
            return emptyParsedBody(ContentType.XML)
        }

        return try {
            val xmlString = String(body, Charsets.UTF_8)
            val metadata = extractMetadata(xmlString)
            val formatted = formatXml(xmlString)

            ParsedBody(
                formatted = formatted,
                contentType = ContentType.XML,
                metadata = metadata,
                isValid = true,
            )
        } catch (e: XmlPullParserException) {
            ParsedBody(
                formatted = String(body, Charsets.UTF_8),
                contentType = ContentType.XML,
                isValid = false,
                errorMessage = "Invalid XML: ${e.message}",
            )
        } catch (e: Exception) {
            ParsedBody(
                formatted = String(body, Charsets.UTF_8),
                contentType = ContentType.XML,
                isValid = false,
                errorMessage = "XML parsing error: ${e.message}",
            )
        }
    }

    private fun formatXml(xml: String): String {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        val writer = StringWriter()
        var depth = 0
        var lastEvent = XmlPullParser.START_DOCUMENT
        var hasTextContent = false

        while (true) {
            val event = parser.next()
            if (event == XmlPullParser.END_DOCUMENT) break

            when (event) {
                XmlPullParser.START_TAG -> {
                    if (lastEvent != XmlPullParser.START_DOCUMENT && lastEvent != XmlPullParser.TEXT) {
                        writer.append("\n")
                    }
                    if (!hasTextContent) {
                        writer.append(indentString.repeat(depth))
                    }

                    writer.append("<")
                    if (parser.prefix != null) {
                        writer.append(parser.prefix).append(":")
                    }
                    writer.append(parser.name)

                    // Write namespace declarations
                    for (i in 0 until parser.getNamespaceCount(depth)) {
                        val prefix = parser.getNamespacePrefix(i)
                        val uri = parser.getNamespaceUri(i)
                        writer.append(" xmlns")
                        if (prefix != null) {
                            writer.append(":").append(prefix)
                        }
                        writer.append("=\"").append(escapeXml(uri)).append("\"")
                    }

                    // Write attributes
                    for (i in 0 until parser.attributeCount) {
                        writer.append(" ")
                        if (parser.getAttributePrefix(i) != null) {
                            writer.append(parser.getAttributePrefix(i)).append(":")
                        }
                        writer.append(parser.getAttributeName(i))
                        writer.append("=\"")
                        writer.append(escapeXml(parser.getAttributeValue(i)))
                        writer.append("\"")
                    }
                    writer.append(">")
                    depth++
                    hasTextContent = false
                }

                XmlPullParser.END_TAG -> {
                    depth--
                    if (lastEvent != XmlPullParser.TEXT && lastEvent != XmlPullParser.START_TAG) {
                        writer.append("\n")
                        writer.append(indentString.repeat(depth))
                    } else if (!hasTextContent && lastEvent != XmlPullParser.START_TAG) {
                        writer.append("\n")
                        writer.append(indentString.repeat(depth))
                    }

                    writer.append("</")
                    if (parser.prefix != null) {
                        writer.append(parser.prefix).append(":")
                    }
                    writer.append(parser.name)
                    writer.append(">")
                    hasTextContent = false
                }

                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim()
                    if (!text.isNullOrEmpty()) {
                        writer.append(escapeXml(text))
                        hasTextContent = true
                    }
                }

                XmlPullParser.CDSECT -> {
                    writer.append("<![CDATA[")
                    writer.append(parser.text)
                    writer.append("]]>")
                    hasTextContent = true
                }

                XmlPullParser.COMMENT -> {
                    if (lastEvent != XmlPullParser.START_DOCUMENT) {
                        writer.append("\n")
                        writer.append(indentString.repeat(depth))
                    }
                    writer.append("<!--")
                    writer.append(parser.text)
                    writer.append("-->")
                }

                XmlPullParser.PROCESSING_INSTRUCTION -> {
                    writer.append("<?")
                    writer.append(parser.text)
                    writer.append("?>")
                    writer.append("\n")
                }
            }
            lastEvent = event
        }

        return writer.toString().trimStart('\n')
    }

    private fun extractMetadata(xml: String): Map<String, String> {
        val metadata = mutableMapOf<String, String>()

        // Extract XML declaration version and encoding
        val declRegex = """<\?xml\s+version\s*=\s*["']([^"']+)["'](\s+encoding\s*=\s*["']([^"']+)["'])?""".toRegex()
        declRegex.find(xml)?.let { match ->
            metadata["version"] = match.groupValues[1]
            if (match.groupValues.size > 3 && match.groupValues[3].isNotEmpty()) {
                metadata["encoding"] = match.groupValues[3]
            }
        }

        // Extract root element
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        try {
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG) {
                    metadata["rootElement"] = parser.name
                    parser.getNamespace(parser.prefix)?.let { ns ->
                        if (ns.isNotEmpty()) {
                            metadata["rootNamespace"] = ns
                        }
                    }
                    break
                }
            }
        } catch (e: Exception) {
            // Ignore metadata extraction errors
        }

        // Extract DOCTYPE
        val doctypeRegex = """<!DOCTYPE\s+(\S+)(\s+PUBLIC\s+["']([^"']+)["'])?(\s+["']([^"']+)["'])?""".toRegex()
        doctypeRegex.find(xml)?.let { match ->
            metadata["doctype"] = match.groupValues[1]
            if (match.groupValues.size > 3 && match.groupValues[3].isNotEmpty()) {
                metadata["publicId"] = match.groupValues[3]
            }
            if (match.groupValues.size > 5 && match.groupValues[5].isNotEmpty()) {
                metadata["systemId"] = match.groupValues[5]
            }
        }

        return metadata
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}

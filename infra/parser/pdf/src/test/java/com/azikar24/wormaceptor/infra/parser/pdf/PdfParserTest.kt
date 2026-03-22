package com.azikar24.wormaceptor.infra.parser.pdf

import android.content.Context
import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.entities.PdfMetadata
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

class PdfParserTest {

    private val mockContext: Context = mockk(relaxed = true)
    private val parser = PdfParser(mockContext)

    @Nested
    inner class SupportedContentTypes {

        @Test
        fun `supportedContentTypes contains all PDF MIME types`() {
            parser.supportedContentTypes shouldBe listOf(
                "application/pdf",
                "application/x-pdf",
                "application/acrobat",
                "application/vnd.pdf",
                "text/pdf",
                "text/x-pdf",
            )
        }

        @Test
        fun `priority is 150`() {
            parser.priority shouldBe 150
        }

        @Test
        fun `defaultContentType is PDF`() {
            val result = parser.parse(byteArrayOf())
            result.contentType shouldBe ContentType.PDF
        }
    }

    @Nested
    inner class CanParse {

        @ParameterizedTest
        @ValueSource(
            strings = [
                "application/pdf",
                "application/x-pdf",
                "APPLICATION/PDF",
                "application/pdf; charset=utf-8",
                "text/pdf",
                "application/vnd.pdf",
            ],
        )
        fun `canParse returns true for content types containing pdf`(contentType: String) {
            parser.canParse(contentType, byteArrayOf()).shouldBeTrue()
        }

        @Test
        fun `canParse returns false for acrobat content type without magic bytes`() {
            // "application/acrobat" is in supportedContentTypes but canParse
            // checks contentType.contains("pdf") which doesn't match "acrobat"
            parser.canParse("application/acrobat", byteArrayOf()).shouldBeFalse()
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "application/json",
                "text/plain",
                "image/png",
                "application/octet-stream",
            ],
        )
        fun `canParse returns false for non-PDF content types without magic bytes`(contentType: String) {
            parser.canParse(contentType, byteArrayOf()).shouldBeFalse()
        }

        @Test
        fun `canParse returns true for null contentType with PDF magic bytes`() {
            // %PDF- = 0x25 0x50 0x44 0x46 0x2D
            val pdfBody = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D) + "1.4".toByteArray()
            parser.canParse(null, pdfBody).shouldBeTrue()
        }

        @Test
        fun `canParse returns false for null contentType without magic bytes`() {
            parser.canParse(null, byteArrayOf(0x01, 0x02, 0x03)).shouldBeFalse()
        }

        @Test
        fun `canParse returns false for empty body and null contentType`() {
            parser.canParse(null, byteArrayOf()).shouldBeFalse()
        }

        @Test
        fun `canParse returns true for body too short but with pdf contentType`() {
            parser.canParse("application/pdf", byteArrayOf(0x25)).shouldBeTrue()
        }

        @Test
        fun `canParse returns true for acrobat content type with magic bytes`() {
            val pdfBody = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D) + "1.4".toByteArray()
            parser.canParse("application/acrobat", pdfBody).shouldBeTrue()
        }
    }

    @Nested
    inner class ExtractPdfVersion {

        @Test
        fun `extractPdfVersion returns version from valid PDF header`() {
            val body = "%PDF-1.4 rest of file".toByteArray()
            PdfParser.extractPdfVersion(body) shouldBe "1.4"
        }

        @Test
        fun `extractPdfVersion returns version 1_7`() {
            val body = "%PDF-1.7\nmore data here...".toByteArray()
            PdfParser.extractPdfVersion(body) shouldBe "1.7"
        }

        @Test
        fun `extractPdfVersion returns version 2_0`() {
            val body = "%PDF-2.0 content".toByteArray()
            PdfParser.extractPdfVersion(body) shouldBe "2.0"
        }

        @Test
        fun `extractPdfVersion returns Unknown for data too short`() {
            val body = "%PDF-1.".toByteArray() // only 7 bytes
            PdfParser.extractPdfVersion(body) shouldBe "Unknown"
        }

        @Test
        fun `extractPdfVersion returns Unknown for non-PDF data`() {
            val body = "This is not a PDF file content".toByteArray()
            PdfParser.extractPdfVersion(body) shouldBe "Unknown"
        }

        @Test
        fun `extractPdfVersion returns Unknown for empty data`() {
            PdfParser.extractPdfVersion(byteArrayOf()) shouldBe "Unknown"
        }

        @Test
        fun `extractPdfVersion returns version 1_5`() {
            val body = "%PDF-1.5 some more content follows here".toByteArray()
            PdfParser.extractPdfVersion(body) shouldBe "1.5"
        }

        @Test
        fun `extractPdfVersion returns Unknown for exactly 8 bytes without version pattern`() {
            val body = "ABCDEFGH".toByteArray()
            PdfParser.extractPdfVersion(body) shouldBe "Unknown"
        }
    }

    @Nested
    inner class FormatFileSize {

        @Test
        fun `formatFileSize returns bytes for small sizes`() {
            PdfParser.formatFileSize(500) shouldBe "500 B"
        }

        @Test
        fun `formatFileSize returns zero bytes`() {
            PdfParser.formatFileSize(0) shouldBe "0 B"
        }

        @Test
        fun `formatFileSize returns KB for kilobyte sizes`() {
            PdfParser.formatFileSize(2048) shouldBe "2.0 KB"
        }

        @Test
        fun `formatFileSize returns MB for megabyte sizes`() {
            val twoMb = 2L * 1024 * 1024
            PdfParser.formatFileSize(twoMb) shouldBe "2.0 MB"
        }

        @Test
        fun `formatFileSize returns GB for gigabyte sizes`() {
            val threeGb = 3L * 1024 * 1024 * 1024
            PdfParser.formatFileSize(threeGb) shouldBe "3.0 GB"
        }

        @Test
        fun `formatFileSize returns fractional KB`() {
            PdfParser.formatFileSize(1536) shouldBe "1.5 KB"
        }

        @Test
        fun `formatFileSize returns 1023 B at boundary`() {
            PdfParser.formatFileSize(1023) shouldBe "1023 B"
        }

        @Test
        fun `formatFileSize returns 1_0 KB at exact 1024 boundary`() {
            PdfParser.formatFileSize(1024) shouldBe "1.0 KB"
        }

        @ParameterizedTest
        @CsvSource(
            "1, 1 B",
            "512, 512 B",
            "10240, 10.0 KB",
            "1048576, 1.0 MB",
            "5242880, 5.0 MB",
        )
        fun `formatFileSize returns correct string for various sizes`(
            bytes: Long,
            expected: String,
        ) {
            PdfParser.formatFileSize(bytes) shouldBe expected
        }
    }

    @Nested
    inner class ParseEmptyBody {

        @Test
        fun `parse returns empty body formatted for empty input`() {
            val result = parser.parse(byteArrayOf())
            result.formatted shouldBe "[Empty PDF]"
            result.isValid.shouldBeTrue()
        }
    }

    @Nested
    inner class ParseBodyWithoutMagicBytes {

        @Test
        fun `parseBody returns invalid result when magic bytes are missing`() {
            val body = "not a pdf".toByteArray()
            val result = parser.parse(body)

            result.isValid.shouldBeFalse()
            result.formatted shouldContain "Invalid PDF"
            result.formatted shouldContain "Missing magic bytes"
            result.errorMessage shouldBe "Invalid PDF: Missing %PDF- header"
        }

        @Test
        fun `parseBody returns PDF content type even for invalid data`() {
            val body = "not a pdf".toByteArray()
            val result = parser.parse(body)
            result.contentType shouldBe ContentType.PDF
        }
    }

    @Nested
    inner class ParseBodyWithMagicBytesExceptionPaths {

        @Test
        fun `parseBody with valid magic bytes falls into exception path and returns version and size`() {
            // With a mocked context, PdfRenderer will fail with a generic exception
            // because context.cacheDir returns null from relaxed mock
            val body = buildPdfWithMetadata(version = "1.4")
            val result = parser.parse(body)

            // Should fall into the generic Exception catch block
            result.contentType shouldBe ContentType.PDF
            result.formatted shouldContain "1.4"
            result.formatted shouldContain "PDF"
            result.errorMessage shouldContain "Failed to parse PDF"
        }

        @Test
        fun `parseBody exception path includes version in metadata`() {
            val body = buildPdfWithMetadata(version = "1.7")
            val result = parser.parse(body)

            result.metadata shouldContainKey "version"
            result.metadata["version"] shouldBe "1.7"
        }

        @Test
        fun `parseBody exception path includes file size in metadata`() {
            val body = buildPdfWithMetadata(version = "1.4")
            val result = parser.parse(body)

            result.metadata shouldContainKey "fileSize"
            result.metadata["fileSize"] shouldBe body.size.toString()
        }

        @Test
        fun `parseBody exception path returns invalid result`() {
            val body = buildPdfWithMetadata(version = "2.0")
            val result = parser.parse(body)

            result.isValid.shouldBeFalse()
        }

        @Test
        fun `parseBody exception path includes formatted file size in output`() {
            val body = buildPdfWithMetadata(version = "1.4")
            val result = parser.parse(body)

            result.formatted shouldContain "Size:"
        }
    }

    @Nested
    inner class ExtractDocInfoViaReflection {

        private val companionInstance = PdfParser::class.java.getDeclaredField("Companion").get(null)
        private val companionClass = companionInstance::class.java

        @Test
        fun `extractDocInfo extracts title from literal string`() {
            val content = buildPdfContent(title = "My Document")
            val result = invokeExtractDocInfo(content)

            result["Title"] shouldBe "My Document"
        }

        @Test
        fun `extractDocInfo extracts author from literal string`() {
            val content = buildPdfContent(author = "John Doe")
            val result = invokeExtractDocInfo(content)

            result["Author"] shouldBe "John Doe"
        }

        @Test
        fun `extractDocInfo extracts multiple fields`() {
            val content = buildPdfContent(
                title = "Report",
                author = "Jane",
                creator = "LibreOffice",
                producer = "PDFlib",
            )
            val result = invokeExtractDocInfo(content)

            result["Title"] shouldBe "Report"
            result["Author"] shouldBe "Jane"
            result["Creator"] shouldBe "LibreOffice"
            result["Producer"] shouldBe "PDFlib"
        }

        @Test
        fun `extractDocInfo extracts creation date`() {
            val content = buildPdfContent(creationDate = "D:20230615120000")
            val result = invokeExtractDocInfo(content)

            result["CreationDate"] shouldBe "D:20230615120000"
        }

        @Test
        fun `extractDocInfo extracts modification date`() {
            val content = buildPdfContent(modDate = "D:20240101093045")
            val result = invokeExtractDocInfo(content)

            result["ModDate"] shouldBe "D:20240101093045"
        }

        @Test
        fun `extractDocInfo extracts subject and keywords`() {
            val content = buildPdfContent(subject = "Testing", keywords = "pdf, test, parser")
            val result = invokeExtractDocInfo(content)

            result["Subject"] shouldBe "Testing"
            result["Keywords"] shouldBe "pdf, test, parser"
        }

        @Test
        fun `extractDocInfo returns empty map for content without metadata`() {
            val content = "%PDF-1.4\nsome content without metadata fields\n%%EOF"
            val result = invokeExtractDocInfo(content)

            result shouldBe emptyMap()
        }

        @Test
        fun `extractDocInfo extracts hex-encoded title`() {
            // "Test" in hex = 54657374
            val content = "%PDF-1.4\n/Title <54657374>\n%%EOF"
            val result = invokeExtractDocInfo(content)

            result["Title"] shouldBe "Test"
        }

        @Test
        fun `extractDocInfo extracts hex-encoded author`() {
            // "AB" in hex = 4142
            val content = "%PDF-1.4\n/Author <4142>\n%%EOF"
            val result = invokeExtractDocInfo(content)

            result["Author"] shouldBe "AB"
        }

        @Suppress("UNCHECKED_CAST")
        private fun invokeExtractDocInfo(content: String): Map<String, String> {
            val method = companionClass.getDeclaredMethod(
                "extractDocInfo",
                ByteArray::class.java,
            )
            method.isAccessible = true
            return method.invoke(companionInstance, content.toByteArray(Charsets.ISO_8859_1))
                as Map<String, String>
        }

        private fun buildPdfContent(
            title: String? = null,
            author: String? = null,
            creator: String? = null,
            producer: String? = null,
            creationDate: String? = null,
            modDate: String? = null,
            subject: String? = null,
            keywords: String? = null,
        ): String = buildString {
            append("%PDF-1.4\n")
            title?.let { append("/Title ($it)\n") }
            author?.let { append("/Author ($it)\n") }
            creator?.let { append("/Creator ($it)\n") }
            producer?.let { append("/Producer ($it)\n") }
            creationDate?.let { append("/CreationDate ($it)\n") }
            modDate?.let { append("/ModDate ($it)\n") }
            subject?.let { append("/Subject ($it)\n") }
            keywords?.let { append("/Keywords ($it)\n") }
            append("%%EOF")
        }
    }

    @Nested
    inner class DecodePdfLiteralStringViaReflection {

        private val companionInstance = PdfParser::class.java.getDeclaredField("Companion").get(null)
        private val companionClass = companionInstance::class.java

        @Test
        fun `decodePdfLiteralString handles newline escape`() {
            invokeDecodePdfLiteralString("Hello\\nWorld") shouldBe "Hello\nWorld"
        }

        @Test
        fun `decodePdfLiteralString handles carriage return escape`() {
            invokeDecodePdfLiteralString("Line1\\rLine2") shouldBe "Line1\rLine2"
        }

        @Test
        fun `decodePdfLiteralString handles tab escape`() {
            invokeDecodePdfLiteralString("Col1\\tCol2") shouldBe "Col1\tCol2"
        }

        @Test
        fun `decodePdfLiteralString handles escaped parentheses`() {
            invokeDecodePdfLiteralString("\\(text\\)") shouldBe "(text)"
        }

        @Test
        fun `decodePdfLiteralString handles escaped backslash`() {
            invokeDecodePdfLiteralString("path\\\\file") shouldBe "path\\file"
        }

        @Test
        fun `decodePdfLiteralString handles multiple escapes`() {
            invokeDecodePdfLiteralString("a\\nb\\rc\\td\\(e\\)f\\\\g") shouldBe "a\nb\rc\td(e)f\\g"
        }

        @Test
        fun `decodePdfLiteralString passes through plain text unchanged`() {
            invokeDecodePdfLiteralString("simple text") shouldBe "simple text"
        }

        private fun invokeDecodePdfLiteralString(input: String): String {
            val method = companionClass.getDeclaredMethod(
                "decodePdfLiteralString",
                String::class.java,
            )
            method.isAccessible = true
            return method.invoke(companionInstance, input) as String
        }
    }

    @Nested
    inner class DecodeHexStringViaReflection {

        private val companionInstance = PdfParser::class.java.getDeclaredField("Companion").get(null)
        private val companionClass = companionInstance::class.java

        @Test
        fun `decodeHexString decodes valid hex to text`() {
            // "Hello" = 48656C6C6F
            invokeDecodeHexString("48656C6C6F") shouldBe "Hello"
        }

        @Test
        fun `decodeHexString decodes single character`() {
            // "A" = 41
            invokeDecodeHexString("41") shouldBe "A"
        }

        @Test
        fun `decodeHexString handles lowercase hex`() {
            // "Hi" = 4869
            invokeDecodeHexString("4869") shouldBe "Hi"
        }

        @Test
        fun `decodeHexString returns original string for invalid hex`() {
            invokeDecodeHexString("ZZZZ") shouldBe "ZZZZ"
        }

        @Test
        fun `decodeHexString returns empty string for empty input`() {
            invokeDecodeHexString("") shouldBe ""
        }

        @Test
        fun `decodeHexString decodes space character`() {
            // Space = 20
            invokeDecodeHexString("4120422043") shouldBe "A B C"
        }

        private fun invokeDecodeHexString(input: String): String {
            val method = companionClass.getDeclaredMethod(
                "decodeHexString",
                String::class.java,
            )
            method.isAccessible = true
            return method.invoke(companionInstance, input) as String
        }
    }

    @Nested
    inner class ConvertPdfDateViaReflection {

        private val companionInstance = PdfParser::class.java.getDeclaredField("Companion").get(null)
        private val companionClass = companionInstance::class.java

        @Test
        fun `convertPdfDate converts full date with time`() {
            invokeConvertPdfDate("D:20230615120000") shouldBe "2023-06-15 12:00:00"
        }

        @Test
        fun `convertPdfDate converts date without time portion`() {
            invokeConvertPdfDate("D:20231225") shouldBe "2023-12-25"
        }

        @Test
        fun `convertPdfDate returns original string when not starting with D colon`() {
            invokeConvertPdfDate("2023-06-15") shouldBe "2023-06-15"
        }

        @Test
        fun `convertPdfDate converts date with seconds`() {
            invokeConvertPdfDate("D:20240101093045") shouldBe "2024-01-01 09:30:45"
        }

        @Test
        fun `convertPdfDate handles midnight time`() {
            invokeConvertPdfDate("D:20230101000000") shouldBe "2023-01-01 00:00:00"
        }

        @Test
        fun `convertPdfDate returns original on exception for truncated date`() {
            // Only "D:202" - too short for substring operations
            invokeConvertPdfDate("D:202") shouldBe "D:202"
        }

        @Test
        fun `convertPdfDate handles end of day time`() {
            invokeConvertPdfDate("D:20231231235959") shouldBe "2023-12-31 23:59:59"
        }

        @Test
        fun `convertPdfDate handles plain text input`() {
            invokeConvertPdfDate("not a date") shouldBe "not a date"
        }

        private fun invokeConvertPdfDate(input: String): String {
            val method = companionClass.getDeclaredMethod(
                "convertPdfDate",
                String::class.java,
            )
            method.isAccessible = true
            return method.invoke(companionInstance, input) as String
        }
    }

    @Nested
    inner class FormatPdfSummaryViaReflection {

        @Test
        fun `formatPdfSummary includes version, pages, and size`() {
            val metadata = PdfMetadata(
                pageCount = 5,
                fileSize = 2048,
                version = "1.4",
            )
            val result = invokeFormatPdfSummary(metadata)

            result shouldContain "[PDF Document]"
            result shouldContain "Version: 1.4"
            result shouldContain "Pages: 5"
            result shouldContain "Size: 2.0 KB"
        }

        @Test
        fun `formatPdfSummary includes encrypted status`() {
            val metadata = PdfMetadata(
                pageCount = 0,
                fileSize = 1024,
                version = "1.7",
                isEncrypted = true,
            )
            val result = invokeFormatPdfSummary(metadata)

            result shouldContain "Status: Password-protected"
        }

        @Test
        fun `formatPdfSummary excludes encrypted status when not encrypted`() {
            val metadata = PdfMetadata(
                pageCount = 1,
                fileSize = 512,
                version = "1.4",
                isEncrypted = false,
            )
            val result = invokeFormatPdfSummary(metadata)

            result shouldNotContain "Status:"
            result shouldNotContain "Password-protected"
        }

        @Test
        fun `formatPdfSummary includes title when present`() {
            val metadata = PdfMetadata(
                pageCount = 1,
                fileSize = 1024,
                version = "1.4",
                title = "My Document",
            )
            val result = invokeFormatPdfSummary(metadata)

            result shouldContain "Title: My Document"
        }

        @Test
        fun `formatPdfSummary includes author when present`() {
            val metadata = PdfMetadata(
                pageCount = 1,
                fileSize = 1024,
                version = "1.4",
                author = "Jane Doe",
            )
            val result = invokeFormatPdfSummary(metadata)

            result shouldContain "Author: Jane Doe"
        }

        @Test
        fun `formatPdfSummary includes subject when present`() {
            val metadata = PdfMetadata(
                pageCount = 1,
                fileSize = 1024,
                version = "1.4",
                subject = "Unit Testing",
            )
            val result = invokeFormatPdfSummary(metadata)

            result shouldContain "Subject: Unit Testing"
        }

        @Test
        fun `formatPdfSummary includes creator when present`() {
            val metadata = PdfMetadata(
                pageCount = 1,
                fileSize = 1024,
                version = "1.4",
                creator = "LibreOffice",
            )
            val result = invokeFormatPdfSummary(metadata)

            result shouldContain "Creator: LibreOffice"
        }

        @Test
        fun `formatPdfSummary includes producer when present`() {
            val metadata = PdfMetadata(
                pageCount = 1,
                fileSize = 1024,
                version = "1.4",
                producer = "PDFlib",
            )
            val result = invokeFormatPdfSummary(metadata)

            result shouldContain "Producer: PDFlib"
        }

        @Test
        fun `formatPdfSummary includes converted creation date`() {
            val metadata = PdfMetadata(
                pageCount = 1,
                fileSize = 1024,
                version = "1.4",
                creationDate = "D:20230615120000",
            )
            val result = invokeFormatPdfSummary(metadata)

            result shouldContain "Created: 2023-06-15 12:00:00"
        }

        @Test
        fun `formatPdfSummary includes converted modification date`() {
            val metadata = PdfMetadata(
                pageCount = 1,
                fileSize = 1024,
                version = "1.4",
                modificationDate = "D:20240101093045",
            )
            val result = invokeFormatPdfSummary(metadata)

            result shouldContain "Modified: 2024-01-01 09:30:45"
        }

        @Test
        fun `formatPdfSummary includes keywords when present`() {
            val metadata = PdfMetadata(
                pageCount = 1,
                fileSize = 1024,
                version = "1.4",
                keywords = "pdf, test, parser",
            )
            val result = invokeFormatPdfSummary(metadata)

            result shouldContain "Keywords: pdf, test, parser"
        }

        @Test
        fun `formatPdfSummary with all fields populated`() {
            val metadata = PdfMetadata(
                pageCount = 10,
                fileSize = 1_048_576,
                version = "2.0",
                isEncrypted = true,
                title = "Complete PDF",
                author = "Author Name",
                subject = "All Fields",
                creator = "Creator App",
                producer = "Producer Lib",
                creationDate = "D:20230101000000",
                modificationDate = "D:20240601120000",
                keywords = "complete, test",
            )
            val result = invokeFormatPdfSummary(metadata)

            result shouldContain "[PDF Document]"
            result shouldContain "Version: 2.0"
            result shouldContain "Pages: 10"
            result shouldContain "Size: 1.0 MB"
            result shouldContain "Status: Password-protected"
            result shouldContain "Title: Complete PDF"
            result shouldContain "Author: Author Name"
            result shouldContain "Subject: All Fields"
            result shouldContain "Creator: Creator App"
            result shouldContain "Producer: Producer Lib"
            result shouldContain "Created: 2023-01-01 00:00:00"
            result shouldContain "Modified: 2024-06-01 12:00:00"
            result shouldContain "Keywords: complete, test"
        }

        @Test
        fun `formatPdfSummary excludes null optional fields`() {
            val metadata = PdfMetadata(
                pageCount = 1,
                fileSize = 100,
                version = "1.4",
            )
            val result = invokeFormatPdfSummary(metadata)

            result shouldNotContain "Title:"
            result shouldNotContain "Author:"
            result shouldNotContain "Subject:"
            result shouldNotContain "Creator:"
            result shouldNotContain "Producer:"
            result shouldNotContain "Created:"
            result shouldNotContain "Modified:"
            result shouldNotContain "Keywords:"
        }

        @Test
        fun `formatPdfSummary with non-prefixed date passes through unchanged`() {
            val metadata = PdfMetadata(
                pageCount = 1,
                fileSize = 1024,
                version = "1.4",
                creationDate = "June 15 2023",
            )
            val result = invokeFormatPdfSummary(metadata)

            result shouldContain "Created: June 15 2023"
        }

        private fun invokeFormatPdfSummary(metadata: PdfMetadata): String {
            val method = PdfParser::class.java.getDeclaredMethod(
                "formatPdfSummary",
                PdfMetadata::class.java,
            )
            method.isAccessible = true
            return method.invoke(parser, metadata) as String
        }
    }

    @Nested
    inner class ExtractPdfStringFieldViaReflection {

        private val companionInstance = PdfParser::class.java.getDeclaredField("Companion").get(null)
        private val companionClass = companionInstance::class.java

        @Test
        fun `extractPdfStringField extracts literal string value`() {
            val content = "/Title (My Document Title)"
            invokeExtractPdfStringField(content, "Title") shouldBe "My Document Title"
        }

        @Test
        fun `extractPdfStringField extracts hex string value`() {
            // "Test" in hex = 54657374
            val content = "/Title <54657374>"
            invokeExtractPdfStringField(content, "Title") shouldBe "Test"
        }

        @Test
        fun `extractPdfStringField returns null for missing field`() {
            val content = "/Author (John)"
            invokeExtractPdfStringField(content, "Title").shouldBeNull()
        }

        @Test
        fun `extractPdfStringField handles field with spaces before value`() {
            val content = "/Author   (Jane Doe)"
            invokeExtractPdfStringField(content, "Author") shouldBe "Jane Doe"
        }

        @Test
        fun `extractPdfStringField handles literal string with escaped parens`() {
            val content = "/Title (My \\(Special\\) Doc)"
            val result = invokeExtractPdfStringField(content, "Title")
            // The regex [^)]+ stops at the first ), capturing "My \(Special\"
            // Then decodePdfLiteralString decodes \( to (, giving "My (Special\"
            result shouldBe "My (Special\\"
        }

        private fun invokeExtractPdfStringField(
            content: String,
            fieldName: String,
        ): String? {
            val method = companionClass.getDeclaredMethod(
                "extractPdfStringField",
                String::class.java,
                String::class.java,
            )
            method.isAccessible = true
            return method.invoke(companionInstance, content, fieldName) as? String
        }
    }

    @Nested
    inner class PdfMetadataToMetadataMap {

        @Test
        fun `toMetadataMap includes required fields`() {
            val metadata = PdfMetadata(
                pageCount = 3,
                fileSize = 1024,
                version = "1.4",
            )
            val map = metadata.toMetadataMap()

            map["pageCount"] shouldBe "3"
            map["fileSize"] shouldBe "1024"
            map["version"] shouldBe "1.4"
            map["isEncrypted"] shouldBe "false"
        }

        @Test
        fun `toMetadataMap includes optional fields when present`() {
            val metadata = PdfMetadata(
                pageCount = 1,
                fileSize = 512,
                version = "1.7",
                title = "Title",
                author = "Author",
                creator = "Creator",
                creationDate = "D:20230101",
                modificationDate = "D:20240101",
                producer = "Producer",
                subject = "Subject",
                keywords = "Keywords",
                isEncrypted = true,
            )
            val map = metadata.toMetadataMap()

            map["title"] shouldBe "Title"
            map["author"] shouldBe "Author"
            map["creator"] shouldBe "Creator"
            map["creationDate"] shouldBe "D:20230101"
            map["modificationDate"] shouldBe "D:20240101"
            map["producer"] shouldBe "Producer"
            map["subject"] shouldBe "Subject"
            map["keywords"] shouldBe "Keywords"
            map["isEncrypted"] shouldBe "true"
        }

        @Test
        fun `toMetadataMap excludes null optional fields`() {
            val metadata = PdfMetadata(
                pageCount = 1,
                fileSize = 512,
                version = "1.4",
            )
            val map = metadata.toMetadataMap()

            map.containsKey("title").shouldBeFalse()
            map.containsKey("author").shouldBeFalse()
            map.containsKey("creator").shouldBeFalse()
            map.containsKey("creationDate").shouldBeFalse()
            map.containsKey("modificationDate").shouldBeFalse()
            map.containsKey("producer").shouldBeFalse()
            map.containsKey("subject").shouldBeFalse()
            map.containsKey("keywords").shouldBeFalse()
        }
    }

    @Suppress("MagicNumber")
    private fun buildPdfWithMetadata(version: String): ByteArray {
        return "%PDF-$version\n1 0 obj\n<< /Type /Catalog >>\nendobj\n%%EOF".toByteArray()
    }
}

package com.azikar24.wormaceptor.infra.parser.image

import com.azikar24.wormaceptor.domain.contracts.ContentType
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImageParserTest {

    private val parser = ImageParser()

    @Nested
    inner class SupportedContentTypes {

        @Test
        fun `supportedContentTypes contains all image MIME types`() {
            parser.supportedContentTypes shouldBe listOf(
                "image/png",
                "image/jpeg",
                "image/jpg",
                "image/gif",
                "image/webp",
                "image/svg+xml",
                "image/bmp",
                "image/x-bmp",
                "image/x-icon",
                "image/vnd.microsoft.icon",
                "image/ico",
            )
        }

        @Test
        fun `priority is 150`() {
            parser.priority shouldBe 150
        }

        @Test
        fun `defaultContentType is IMAGE_OTHER`() {
            val result = parser.parse(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05))
            result.contentType shouldBe ContentType.IMAGE_OTHER
        }
    }

    @Nested
    inner class CanParse {

        @ParameterizedTest
        @ValueSource(
            strings = [
                "image/png",
                "image/jpeg",
                "image/jpg",
                "image/gif",
                "image/webp",
                "image/svg+xml",
                "image/bmp",
                "image/x-icon",
                "image/vnd.microsoft.icon",
                "image/ico",
                "image/png; charset=utf-8",
                "IMAGE/PNG",
                "image/tiff",
            ],
        )
        fun `canParse returns true for image MIME types`(contentType: String) {
            parser.canParse(contentType, byteArrayOf()).shouldBeTrue()
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "application/json",
                "text/plain",
                "application/xml",
                "application/octet-stream",
            ],
        )
        fun `canParse returns false for non-image MIME types without magic bytes`(contentType: String) {
            parser.canParse(contentType, byteArrayOf()).shouldBeFalse()
        }

        @Test
        fun `canParse returns true for null contentType when magic bytes match PNG`() {
            val pngHeader = byteArrayOf(
                0x89.toByte(),
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A,
            )
            parser.canParse(null, pngHeader).shouldBeTrue()
        }

        @Test
        fun `canParse returns false for null contentType with unrecognized bytes`() {
            parser.canParse(null, byteArrayOf(0x01, 0x02, 0x03)).shouldBeFalse()
        }

        @Test
        fun `canParse returns true for null contentType with JPEG magic bytes`() {
            val jpegHeader = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
            parser.canParse(null, jpegHeader).shouldBeTrue()
        }

        @Test
        fun `canParse returns true for null contentType with GIF magic bytes`() {
            val gifHeader = "GIF89a".toByteArray(Charsets.US_ASCII)
            parser.canParse(null, gifHeader).shouldBeTrue()
        }

        @Test
        fun `canParse returns true for null contentType with BMP magic bytes`() {
            val bmpHeader = byteArrayOf(0x42, 0x4D, 0x00, 0x00)
            parser.canParse(null, bmpHeader).shouldBeTrue()
        }
    }

    @Nested
    inner class Parse {

        @Test
        fun `parse returns empty body formatted for empty input`() {
            val result = parser.parse(byteArrayOf())
            result.formatted shouldBe "[Empty image data]"
            result.isValid.shouldBeTrue()
        }

        @Test
        fun `parse returns valid ParsedBody with metadata for PNG data`() {
            val data = buildMinimalPng(width = 640, height = 480)
            val result = parser.parse(data)

            result.isValid.shouldBeTrue()
            result.contentType shouldBe ContentType.IMAGE_PNG
            result.formatted shouldContain "PNG"
            result.formatted shouldContain "640x480"
            result.metadata["format"] shouldBe "PNG"
            result.metadata["width"] shouldBe "640"
            result.metadata["height"] shouldBe "480"
        }

        @Test
        fun `parse returns invalid result for unknown image format`() {
            val unknownData = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
            val result = parser.parse(unknownData)

            result.isValid.shouldBeFalse()
            result.contentType shouldBe ContentType.IMAGE_OTHER
            result.errorMessage shouldBe "Could not detect image format from magic bytes"
            result.formatted shouldContain "Unknown image format"
            result.metadata["size"] shouldBe "5"
        }

        @Test
        fun `parse returns sizeFormatted in metadata for unknown format`() {
            val unknownData = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05)
            val result = parser.parse(unknownData)

            result.metadata shouldContainKey "sizeFormatted"
            result.metadata["sizeFormatted"] shouldBe "5 B"
        }

        @Test
        fun `parse returns valid ParsedBody for JPEG data`() {
            val data = buildMinimalJpeg(width = 1920, height = 1080)
            val result = parser.parse(data)

            result.isValid.shouldBeTrue()
            result.contentType shouldBe ContentType.IMAGE_JPEG
            result.formatted shouldContain "JPEG"
            result.formatted shouldContain "1920x1080"
        }

        @Test
        fun `parse returns valid ParsedBody for GIF data`() {
            val data = buildMinimalGif(width = 320, height = 240)
            val result = parser.parse(data)

            result.isValid.shouldBeTrue()
            result.contentType shouldBe ContentType.IMAGE_GIF
            result.formatted shouldContain "GIF"
            result.formatted shouldContain "320x240"
        }

        @Test
        @Suppress("MagicNumber")
        fun `parse returns valid ParsedBody for BMP data`() {
            val data = buildMinimalBmp(width = 256, height = 128, bitCount = 24)
            val result = parser.parse(data)

            result.isValid.shouldBeTrue()
            result.contentType shouldBe ContentType.IMAGE_BMP
            result.formatted shouldContain "BMP"
            result.formatted shouldContain "256x128"
        }

        @Test
        @Suppress("MagicNumber")
        fun `parse returns valid ParsedBody for ICO data`() {
            val data = buildMinimalIco(width = 32, height = 32, imageCount = 1, bitCount = 32)
            val result = parser.parse(data)

            result.isValid.shouldBeTrue()
            result.contentType shouldBe ContentType.IMAGE_ICO
            result.formatted shouldContain "ICO"
            result.formatted shouldContain "32x32"
        }

        @Test
        fun `parse returns valid ParsedBody for SVG data`() {
            val svg = "<svg width=\"100\" height=\"200\"></svg>".toByteArray(Charsets.UTF_8)
            val result = parser.parse(svg)

            result.isValid.shouldBeTrue()
            result.contentType shouldBe ContentType.IMAGE_SVG
            result.formatted shouldContain "SVG"
            result.formatted shouldContain "100x200"
        }

        @Test
        @Suppress("MagicNumber")
        fun `parse returns valid ParsedBody for WebP VP8 lossy data`() {
            val data = buildMinimalWebPVP8(width = 800, height = 600)
            val result = parser.parse(data)

            result.isValid.shouldBeTrue()
            result.contentType shouldBe ContentType.IMAGE_WEBP
            result.formatted shouldContain "WebP"
            result.formatted shouldContain "800x600"
        }

        @Test
        @Suppress("MagicNumber")
        fun `parse returns valid ParsedBody for WebP VP8X extended data`() {
            val data = buildMinimalWebPVP8X(width = 1920, height = 1080, hasAlpha = true)
            val result = parser.parse(data)

            result.isValid.shouldBeTrue()
            result.contentType shouldBe ContentType.IMAGE_WEBP
            result.formatted shouldContain "WebP"
            result.formatted shouldContain "1920x1080"
        }

        @Test
        @Suppress("MagicNumber")
        fun `parse with BMP negative height returns valid result with absolute height`() {
            val data = buildMinimalBmp(width = 100, height = -200, bitCount = 24)
            val result = parser.parse(data)

            result.isValid.shouldBeTrue()
            result.formatted shouldContain "100x200"
        }
    }

    @Nested
    inner class BuildFormattedString {

        @Test
        fun `formatted string includes dimensions when width and height are positive`() {
            val data = buildMinimalPng(width = 640, height = 480)
            val result = parser.parse(data)

            result.formatted shouldContain "Dimensions: 640x480"
        }

        @Test
        fun `formatted string includes size`() {
            val data = buildMinimalPng(width = 100, height = 100)
            val result = parser.parse(data)

            result.formatted shouldContain "Size:"
        }

        @Test
        @Suppress("MagicNumber")
        fun `formatted string includes color space when available`() {
            val data = buildMinimalPng(width = 100, height = 100, colorType = 2)
            val result = parser.parse(data)

            result.formatted shouldContain "Color Space: RGB"
        }

        @Test
        @Suppress("MagicNumber")
        fun `formatted string includes alpha channel for RGBA PNG`() {
            val data = buildMinimalPng(width = 100, height = 100, colorType = 6)
            val result = parser.parse(data)

            result.formatted shouldContain "Alpha Channel: Yes"
        }

        @Test
        @Suppress("MagicNumber")
        fun `formatted string excludes alpha channel for RGB PNG`() {
            val data = buildMinimalPng(width = 100, height = 100, colorType = 2)
            val result = parser.parse(data)

            result.formatted shouldNotContain "Alpha Channel"
        }

        @Test
        @Suppress("MagicNumber")
        fun `formatted string includes bit depth`() {
            val data = buildMinimalPng(width = 100, height = 100, colorType = 2)
            val result = parser.parse(data)

            result.formatted shouldContain "Bit Depth: 8-bit"
        }

        @Test
        fun `formatted string for GIF includes Indexed color space`() {
            val data = buildMinimalGif(width = 100, height = 100)
            val result = parser.parse(data)

            result.formatted shouldContain "Color Space: Indexed"
        }

        @Test
        fun `formatted string for GIF includes Alpha Channel`() {
            val data = buildMinimalGif(width = 100, height = 100)
            val result = parser.parse(data)

            result.formatted shouldContain "Alpha Channel: Yes"
        }
    }

    @Nested
    inner class BuildMetadataMap {

        @Test
        fun `metadata map includes all expected keys for PNG`() {
            val data = buildMinimalPng(width = 640, height = 480)
            val result = parser.parse(data)

            result.metadata shouldContainKey "format"
            result.metadata shouldContainKey "width"
            result.metadata shouldContainKey "height"
            result.metadata shouldContainKey "dimensions"
            result.metadata shouldContainKey "size"
            result.metadata shouldContainKey "sizeFormatted"
            result.metadata shouldContainKey "hasAlpha"
        }

        @Test
        @Suppress("MagicNumber")
        fun `metadata map includes colorSpace when available`() {
            val data = buildMinimalPng(width = 100, height = 100, colorType = 2)
            val result = parser.parse(data)

            result.metadata shouldContainKey "colorSpace"
            result.metadata["colorSpace"] shouldBe "RGB"
        }

        @Test
        @Suppress("MagicNumber")
        fun `metadata map includes bitDepth when available`() {
            val data = buildMinimalPng(width = 100, height = 100, colorType = 2)
            val result = parser.parse(data)

            result.metadata shouldContainKey "bitDepth"
            result.metadata["bitDepth"] shouldBe "8"
        }

        @Test
        fun `metadata dimensions matches width x height format`() {
            val data = buildMinimalPng(width = 1920, height = 1080)
            val result = parser.parse(data)

            result.metadata["dimensions"] shouldBe "1920x1080"
        }
    }

    @Nested
    inner class ImageMetadataExtractorInterface {

        @Test
        fun `extractMetadata returns unknown for empty data`() {
            val metadata = parser.extractMetadata(byteArrayOf())
            metadata.width shouldBe 0
            metadata.height shouldBe 0
            metadata.format shouldBe "Unknown"
            metadata.fileSize shouldBe 0
        }

        @Test
        fun `extractMetadata returns metadata for valid PNG`() {
            val data = buildMinimalPng(width = 100, height = 200)
            val metadata = parser.extractMetadata(data)

            metadata.width shouldBe 100
            metadata.height shouldBe 200
            metadata.format shouldBe "PNG"
        }

        @Test
        fun `extractMetadata returns unknown for unrecognized data`() {
            val data = byteArrayOf(0x01, 0x02, 0x03, 0x04)
            val metadata = parser.extractMetadata(data)

            metadata.width shouldBe 0
            metadata.height shouldBe 0
            metadata.format shouldBe "Unknown"
        }

        @Test
        fun `extractMetadata returns metadata for valid JPEG`() {
            val data = buildMinimalJpeg(width = 800, height = 600)
            val metadata = parser.extractMetadata(data)

            metadata.width shouldBe 800
            metadata.height shouldBe 600
            metadata.format shouldBe "JPEG"
        }

        @Test
        fun `extractMetadata returns metadata for valid GIF`() {
            val data = buildMinimalGif(width = 320, height = 240)
            val metadata = parser.extractMetadata(data)

            metadata.width shouldBe 320
            metadata.height shouldBe 240
            metadata.format shouldBe "GIF"
        }

        @Test
        fun `extractMetadata returns file size for unrecognized data`() {
            val data = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)
            val metadata = parser.extractMetadata(data)

            metadata.fileSize shouldBe 8
        }

        @Test
        fun `isImageData returns true for PNG magic bytes`() {
            val pngHeader = byteArrayOf(
                0x89.toByte(),
                0x50,
                0x4E,
                0x47,
                0x0D,
                0x0A,
                0x1A,
                0x0A,
            )
            parser.isImageData(pngHeader).shouldBeTrue()
        }

        @Test
        fun `isImageData returns false for unrecognized data`() {
            parser.isImageData(byteArrayOf(0x01, 0x02, 0x03)).shouldBeFalse()
        }

        @Test
        fun `isImageData returns true for JPEG magic bytes`() {
            val jpegHeader = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
            parser.isImageData(jpegHeader).shouldBeTrue()
        }

        @Test
        fun `isImageData returns true for BMP magic bytes`() {
            val bmpHeader = byteArrayOf(0x42, 0x4D, 0x00, 0x00)
            parser.isImageData(bmpHeader).shouldBeTrue()
        }

        @Test
        fun `isImageData returns false for empty data`() {
            parser.isImageData(byteArrayOf()).shouldBeFalse()
        }
    }

    @Nested
    inner class ParseValidityRules {

        @Test
        fun `parse returns invalid when detected format has zero dimensions`() {
            // SVG without width/height/viewBox -> 0x0
            val svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>".toByteArray(Charsets.UTF_8)
            val result = parser.parse(svg)

            result.isValid.shouldBeFalse()
        }

        @Test
        fun `parse returns valid when dimensions are positive`() {
            val data = buildMinimalPng(width = 1, height = 1)
            val result = parser.parse(data)

            result.isValid.shouldBeTrue()
        }
    }

    @Suppress("MagicNumber")
    private fun buildMinimalPng(
        width: Int,
        height: Int,
        colorType: Int = 2,
    ): ByteArray {
        val buffer = ByteBuffer.allocate(26).order(ByteOrder.BIG_ENDIAN)
        buffer.put(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        buffer.putInt(13)
        buffer.put("IHDR".toByteArray(Charsets.US_ASCII))
        buffer.putInt(width)
        buffer.putInt(height)
        buffer.put(8.toByte()) // bit depth
        buffer.put(colorType.toByte()) // color type
        return buffer.array()
    }

    @Suppress("MagicNumber")
    private fun buildMinimalJpeg(
        width: Int,
        height: Int,
    ): ByteArray {
        val buffer = ByteBuffer.allocate(20).order(ByteOrder.BIG_ENDIAN)
        buffer.put(0xFF.toByte())
        buffer.put(0xD8.toByte())
        buffer.put(0xFF.toByte())
        buffer.put(0xC0.toByte())
        buffer.putShort(17.toShort())
        buffer.put(8.toByte()) // precision
        buffer.putShort(height.toShort())
        buffer.putShort(width.toShort())
        buffer.put(3.toByte()) // components (YCbCr)
        val remaining = buffer.remaining()
        buffer.put(ByteArray(remaining))
        return buffer.array()
    }

    @Suppress("MagicNumber")
    private fun buildMinimalGif(
        width: Int,
        height: Int,
    ): ByteArray {
        val buffer = ByteBuffer.allocate(13).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("GIF89a".toByteArray(Charsets.US_ASCII))
        buffer.putShort(width.toShort())
        buffer.putShort(height.toShort())
        buffer.put(0x00.toByte())
        return buffer.array()
    }

    @Suppress("MagicNumber")
    private fun buildMinimalBmp(
        width: Int,
        height: Int,
        bitCount: Int,
    ): ByteArray {
        val buffer = ByteBuffer.allocate(30).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(0x42.toByte()) // 'B'
        buffer.put(0x4D.toByte()) // 'M'
        buffer.putInt(0) // file size
        buffer.putShort(0) // reserved
        buffer.putShort(0) // reserved
        buffer.putInt(0) // pixel data offset
        buffer.putInt(40) // header size
        buffer.putInt(width)
        buffer.putInt(height)
        buffer.putShort(1) // color planes
        buffer.putShort(bitCount.toShort())
        return buffer.array()
    }

    @Suppress("MagicNumber")
    private fun buildMinimalIco(
        width: Int,
        height: Int,
        imageCount: Int,
        bitCount: Int,
    ): ByteArray {
        val buffer = ByteBuffer.allocate(22).order(ByteOrder.LITTLE_ENDIAN)
        buffer.putShort(0) // reserved
        buffer.putShort(1) // type (1 = ICO)
        buffer.putShort(imageCount.toShort())
        buffer.put(width.toByte())
        buffer.put(height.toByte())
        buffer.put(0) // color count
        buffer.put(0) // reserved
        buffer.putShort(1) // color planes
        buffer.putShort(bitCount.toShort())
        buffer.putInt(0) // image data size
        buffer.putInt(0) // image data offset
        return buffer.array()
    }

    @Suppress("MagicNumber")
    private fun buildMinimalWebPVP8(
        width: Int,
        height: Int,
    ): ByteArray {
        val buffer = ByteBuffer.allocate(30).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(22)
        buffer.put("WEBP".toByteArray(Charsets.US_ASCII))
        buffer.put("VP8 ".toByteArray(Charsets.US_ASCII))
        buffer.putInt(10)
        buffer.put(0x00.toByte())
        buffer.put(0x00.toByte())
        buffer.put(0x00.toByte())
        buffer.put(0x9D.toByte())
        buffer.put(0x01.toByte())
        buffer.put(0x2A.toByte())
        buffer.putShort(width.toShort())
        buffer.putShort(height.toShort())
        return buffer.array()
    }

    @Suppress("MagicNumber")
    private fun buildMinimalWebPVP8X(
        width: Int,
        height: Int,
        hasAlpha: Boolean,
    ): ByteArray {
        val buffer = ByteBuffer.allocate(30).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(22)
        buffer.put("WEBP".toByteArray(Charsets.US_ASCII))
        buffer.put("VP8X".toByteArray(Charsets.US_ASCII))
        buffer.putInt(10)
        val flags = if (hasAlpha) 0x10 else 0x00
        buffer.put(flags.toByte())
        buffer.put(0x00.toByte())
        buffer.put(0x00.toByte())
        buffer.put(0x00.toByte())
        val w = width - 1
        buffer.put((w and 0xFF).toByte())
        buffer.put((w shr 8 and 0xFF).toByte())
        buffer.put((w shr 16 and 0xFF).toByte())
        val h = height - 1
        buffer.put((h and 0xFF).toByte())
        buffer.put((h shr 8 and 0xFF).toByte())
        buffer.put((h shr 16 and 0xFF).toByte())
        return buffer.array()
    }
}

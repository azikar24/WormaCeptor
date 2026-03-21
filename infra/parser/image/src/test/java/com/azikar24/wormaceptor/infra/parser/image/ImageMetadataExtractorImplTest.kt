package com.azikar24.wormaceptor.infra.parser.image

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImageMetadataExtractorImplTest {

    @Nested
    inner class PngMetadata {

        @Test
        fun `extracts width, height, bit depth, and color type from PNG IHDR`() {
            val data = buildMinimalPng(width = 640, height = 480, bitDepth = 8, colorType = 2) // RGB
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.PNG)

            metadata.width shouldBe 640
            metadata.height shouldBe 480
            metadata.format shouldBe "PNG"
            metadata.bitDepth shouldBe 8
            metadata.colorSpace shouldBe "RGB"
            metadata.hasAlpha shouldBe false
        }

        @Test
        fun `extracts RGBA color type with alpha`() {
            val data = buildMinimalPng(width = 100, height = 200, bitDepth = 16, colorType = 6) // RGBA
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.PNG)

            metadata.width shouldBe 100
            metadata.height shouldBe 200
            metadata.colorSpace shouldBe "RGBA"
            metadata.hasAlpha shouldBe true
            metadata.bitDepth shouldBe 16
        }

        @Test
        fun `extracts grayscale with alpha color type`() {
            val data = buildMinimalPng(width = 50, height = 50, bitDepth = 8, colorType = 4)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.PNG)

            metadata.colorSpace shouldBe "Grayscale"
            metadata.hasAlpha shouldBe true
        }

        @Test
        fun `extracts indexed color type`() {
            val data = buildMinimalPng(width = 32, height = 32, bitDepth = 8, colorType = 3)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.PNG)

            metadata.colorSpace shouldBe "Indexed"
            metadata.hasAlpha shouldBe false
        }

        @Test
        fun `extracts grayscale without alpha color type`() {
            val data = buildMinimalPng(width = 64, height = 64, bitDepth = 8, colorType = 0)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.PNG)

            metadata.colorSpace shouldBe "Grayscale"
            metadata.hasAlpha shouldBe false
        }

        @Test
        fun `handles unknown color type gracefully`() {
            val data = buildMinimalPng(width = 10, height = 10, bitDepth = 8, colorType = 7)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.PNG)

            metadata.width shouldBe 10
            metadata.height shouldBe 10
            metadata.colorSpace shouldBe null
            metadata.hasAlpha shouldBe false
        }

        @Test
        fun `returns unknown metadata for PNG data too short`() {
            val shortData = byteArrayOf(
                0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, // IHDR length
            )
            val metadata = ImageMetadataExtractorImpl.extract(shortData, ImageFormat.PNG)

            metadata.width shouldBe 0
            metadata.height shouldBe 0
            metadata.format shouldBe "PNG"
        }

        @Test
        fun `fileSize matches input data size`() {
            val data = buildMinimalPng(width = 1, height = 1, bitDepth = 8, colorType = 2)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.PNG)

            metadata.fileSize shouldBe data.size.toLong()
        }
    }

    @Nested
    inner class JpegMetadata {

        @Test
        fun `extracts dimensions from JPEG SOF0 marker`() {
            val data = buildMinimalJpeg(width = 1920, height = 1080, components = 3)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.JPEG)

            metadata.width shouldBe 1920
            metadata.height shouldBe 1080
            metadata.format shouldBe "JPEG"
            metadata.colorSpace shouldBe "YCbCr"
            metadata.hasAlpha shouldBe false
        }

        @Test
        fun `extracts grayscale JPEG metadata`() {
            val data = buildMinimalJpeg(width = 800, height = 600, components = 1)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.JPEG)

            metadata.width shouldBe 800
            metadata.height shouldBe 600
            metadata.colorSpace shouldBe "Grayscale"
        }

        @Test
        fun `extracts CMYK JPEG metadata`() {
            val data = buildMinimalJpeg(width = 400, height = 300, components = 4)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.JPEG)

            metadata.width shouldBe 400
            metadata.height shouldBe 300
            metadata.colorSpace shouldBe "CMYK"
        }

        @Test
        fun `handles unknown component count`() {
            val data = buildMinimalJpeg(width = 200, height = 100, components = 5)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.JPEG)

            metadata.width shouldBe 200
            metadata.height shouldBe 100
            metadata.colorSpace shouldBe null
        }

        @Test
        fun `returns unknown metadata for JPEG data too short`() {
            val shortData = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
            val metadata = ImageMetadataExtractorImpl.extract(shortData, ImageFormat.JPEG)

            metadata.width shouldBe 0
            metadata.height shouldBe 0
            metadata.format shouldBe "JPEG"
        }

        @Test
        fun `extracts precision from JPEG`() {
            val data = buildMinimalJpeg(width = 100, height = 100, components = 3, precision = 12)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.JPEG)

            metadata.bitDepth shouldBe 12
        }

        @Test
        fun `JPEG always has hasAlpha false`() {
            val data = buildMinimalJpeg(width = 100, height = 100, components = 3)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.JPEG)

            metadata.hasAlpha shouldBe false
        }

        @Test
        @Suppress("MagicNumber")
        fun `skips non-SOF markers and finds SOF0`() {
            // Build a JPEG with an APP0 marker before SOF0
            val buffer = ByteBuffer.allocate(40).order(ByteOrder.BIG_ENDIAN)
            // SOI
            buffer.put(0xFF.toByte())
            buffer.put(0xD8.toByte())
            // APP0 marker (FF E0) with a small segment
            buffer.put(0xFF.toByte())
            buffer.put(0xE0.toByte())
            buffer.putShort(8.toShort()) // length including itself
            buffer.put(ByteArray(6)) // padding for APP0 data
            // SOF0 marker
            buffer.put(0xFF.toByte())
            buffer.put(0xC0.toByte())
            buffer.putShort(17.toShort()) // segment length
            buffer.put(8.toByte()) // precision
            buffer.putShort(768.toShort()) // height
            buffer.putShort(1024.toShort()) // width
            buffer.put(3.toByte()) // components
            val remaining = buffer.remaining()
            buffer.put(ByteArray(remaining))

            val metadata = ImageMetadataExtractorImpl.extract(buffer.array(), ImageFormat.JPEG)

            metadata.width shouldBe 1024
            metadata.height shouldBe 768
        }
    }

    @Nested
    inner class GifMetadata {

        @Test
        fun `extracts dimensions from GIF logical screen descriptor`() {
            val data = buildMinimalGif(width = 320, height = 240)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.GIF)

            metadata.width shouldBe 320
            metadata.height shouldBe 240
            metadata.format shouldBe "GIF"
            metadata.colorSpace shouldBe "Indexed"
            metadata.hasAlpha shouldBe true
            metadata.bitDepth shouldBe 8
        }

        @Test
        fun `returns unknown metadata for GIF data too short`() {
            val shortData = "GIF89a".toByteArray(Charsets.US_ASCII) // only 6 bytes, need 10
            val metadata = ImageMetadataExtractorImpl.extract(shortData, ImageFormat.GIF)

            metadata.width shouldBe 0
            metadata.height shouldBe 0
            metadata.format shouldBe "GIF"
        }

        @Test
        fun `extracts small GIF dimensions`() {
            val data = buildMinimalGif(width = 1, height = 1)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.GIF)

            metadata.width shouldBe 1
            metadata.height shouldBe 1
            metadata.format shouldBe "GIF"
        }

        @Test
        fun `extracts large GIF dimensions`() {
            val data = buildMinimalGif(width = 4096, height = 2048)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.GIF)

            metadata.width shouldBe 4096
            metadata.height shouldBe 2048
        }

        @Test
        fun `GIF fileSize matches input`() {
            val data = buildMinimalGif(width = 100, height = 100)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.GIF)

            metadata.fileSize shouldBe data.size.toLong()
        }
    }

    @Nested
    inner class WebPMetadata {

        @Test
        @Suppress("MagicNumber")
        fun `extracts dimensions from WebP VP8 lossy format`() {
            val data = buildMinimalWebPVP8(width = 800, height = 600)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.WEBP)

            metadata.width shouldBe 800
            metadata.height shouldBe 600
            metadata.format shouldBe "WebP"
            metadata.colorSpace shouldBe "YUV"
            metadata.hasAlpha shouldBe false
        }

        @Test
        @Suppress("MagicNumber")
        fun `extracts dimensions from WebP VP8L lossless format`() {
            val data = buildMinimalWebPVP8L(width = 400, height = 300, hasAlpha = false)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.WEBP)

            metadata.width shouldBe 400
            metadata.height shouldBe 300
            metadata.format shouldBe "WebP"
            metadata.colorSpace shouldBe "RGBA"
            metadata.hasAlpha shouldBe false
        }

        @Test
        @Suppress("MagicNumber")
        fun `extracts dimensions from WebP VP8L with alpha`() {
            val data = buildMinimalWebPVP8L(width = 200, height = 100, hasAlpha = true)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.WEBP)

            metadata.width shouldBe 200
            metadata.height shouldBe 100
            metadata.hasAlpha shouldBe true
        }

        @Test
        @Suppress("MagicNumber")
        fun `extracts dimensions from WebP VP8X extended format`() {
            val data = buildMinimalWebPVP8X(width = 1920, height = 1080, hasAlpha = false)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.WEBP)

            metadata.width shouldBe 1920
            metadata.height shouldBe 1080
            metadata.format shouldBe "WebP"
            metadata.colorSpace shouldBe "RGB"
            metadata.hasAlpha shouldBe false
        }

        @Test
        @Suppress("MagicNumber")
        fun `extracts dimensions from WebP VP8X with alpha`() {
            val data = buildMinimalWebPVP8X(width = 512, height = 256, hasAlpha = true)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.WEBP)

            metadata.width shouldBe 512
            metadata.height shouldBe 256
            metadata.colorSpace shouldBe "RGBA"
            metadata.hasAlpha shouldBe true
        }

        @Test
        fun `returns unknown metadata for WebP data too short`() {
            val shortData = buildWebPHeader("VP8 ", dataSize = 0)
            val metadata = ImageMetadataExtractorImpl.extract(shortData, ImageFormat.WEBP)

            metadata.width shouldBe 0
            metadata.height shouldBe 0
            metadata.format shouldBe "WebP"
        }

        @Test
        @Suppress("MagicNumber")
        fun `returns unknown for WebP with unknown chunk type`() {
            val data = buildWebPHeaderWithPayload("XXXX", ByteArray(20))
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.WEBP)

            metadata.width shouldBe 0
            metadata.height shouldBe 0
            metadata.format shouldBe "WebP"
        }

        @Test
        @Suppress("MagicNumber")
        fun `VP8 returns unknown when frame header signature is missing`() {
            // Build VP8 data without the 9D 01 2A signature at the correct offset
            val payload = ByteArray(20) // all zeros, no frame header signature
            val data = buildWebPHeaderWithPayload("VP8 ", payload)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.WEBP)

            metadata.width shouldBe 0
            metadata.height shouldBe 0
            metadata.format shouldBe "WebP"
        }

        @Test
        @Suppress("MagicNumber")
        fun `VP8L returns unknown when signature byte is wrong`() {
            // Build VP8L data without the 0x2F signature byte
            val payload = ByteArray(10) // all zeros, missing 0x2F at offset 0
            val data = buildWebPHeaderWithPayload("VP8L", payload)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.WEBP)

            metadata.width shouldBe 0
            metadata.height shouldBe 0
            metadata.format shouldBe "WebP"
        }

        @Test
        @Suppress("MagicNumber")
        fun `VP8X with small dimensions`() {
            val data = buildMinimalWebPVP8X(width = 1, height = 1, hasAlpha = false)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.WEBP)

            metadata.width shouldBe 1
            metadata.height shouldBe 1
        }
    }

    @Nested
    inner class BmpMetadata {

        @Test
        @Suppress("MagicNumber")
        fun `extracts dimensions and bit depth from BMP BITMAPINFOHEADER`() {
            val data = buildMinimalBmp(width = 256, height = 128, bitCount = 24)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.BMP)

            metadata.width shouldBe 256
            metadata.height shouldBe 128
            metadata.format shouldBe "BMP"
            metadata.colorSpace shouldBe "RGB"
            metadata.hasAlpha shouldBe false
            metadata.bitDepth shouldBe 24
        }

        @Test
        @Suppress("MagicNumber")
        fun `extracts 32-bit BMP with alpha`() {
            val data = buildMinimalBmp(width = 64, height = 64, bitCount = 32)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.BMP)

            metadata.hasAlpha shouldBe true
            metadata.bitDepth shouldBe 32
        }

        @Test
        @Suppress("MagicNumber")
        fun `handles negative height (top-down BMP) by taking absolute value`() {
            val data = buildMinimalBmp(width = 100, height = -100, bitCount = 24)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.BMP)

            metadata.height shouldBe 100
        }

        @Test
        fun `returns unknown metadata for BMP data too short`() {
            val shortData = byteArrayOf(0x42, 0x4D, 0x00) // "BM" + 1 byte
            val metadata = ImageMetadataExtractorImpl.extract(shortData, ImageFormat.BMP)

            metadata.width shouldBe 0
            metadata.height shouldBe 0
            metadata.format shouldBe "BMP"
        }

        @Test
        @Suppress("MagicNumber")
        fun `extracts indexed BMP with 8-bit color`() {
            val data = buildMinimalBmp(width = 48, height = 48, bitCount = 8)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.BMP)

            metadata.colorSpace shouldBe "Indexed"
            metadata.bitDepth shouldBe 8
            metadata.hasAlpha shouldBe false
        }

        @Test
        @Suppress("MagicNumber")
        fun `extracts 1-bit monochrome BMP`() {
            val data = buildMinimalBmp(width = 16, height = 16, bitCount = 1)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.BMP)

            metadata.colorSpace shouldBe "Indexed"
            metadata.bitDepth shouldBe 1
        }

        @Test
        @Suppress("MagicNumber")
        fun `extracts 16-bit BMP`() {
            val data = buildMinimalBmp(width = 32, height = 32, bitCount = 16)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.BMP)

            metadata.colorSpace shouldBe "RGB"
            metadata.bitDepth shouldBe 16
            metadata.hasAlpha shouldBe false
        }
    }

    @Nested
    inner class IcoMetadata {

        @Test
        @Suppress("MagicNumber")
        fun `extracts dimensions from ICO with single image`() {
            val data = buildMinimalIco(width = 32, height = 32, imageCount = 1, bitCount = 32)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.ICO)

            metadata.width shouldBe 32
            metadata.height shouldBe 32
            metadata.format shouldBe "ICO (1 images)"
            metadata.hasAlpha shouldBe true
            metadata.bitDepth shouldBe 32
        }

        @Test
        @Suppress("MagicNumber")
        fun `extracts ICO with multiple images`() {
            val data = buildMinimalIco(width = 48, height = 48, imageCount = 3, bitCount = 24)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.ICO)

            metadata.width shouldBe 48
            metadata.height shouldBe 48
            metadata.format shouldBe "ICO (3 images)"
        }

        @Test
        @Suppress("MagicNumber")
        fun `ICO with 256px dimensions uses 0 byte encoding`() {
            // When width/height is 256, it's encoded as 0 in the ICO header
            val data = buildMinimalIco(width = 0, height = 0, imageCount = 1, bitCount = 32)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.ICO)

            metadata.width shouldBe 256
            metadata.height shouldBe 256
        }

        @Test
        @Suppress("MagicNumber")
        fun `ICO with width 256 and height 16`() {
            val data = buildMinimalIco(width = 0, height = 16, imageCount = 1, bitCount = 32)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.ICO)

            metadata.width shouldBe 256
            metadata.height shouldBe 16
        }

        @Test
        @Suppress("MagicNumber")
        fun `ICO with bitCount 0 sets bitDepth to null`() {
            val data = buildMinimalIco(width = 16, height = 16, imageCount = 1, bitCount = 0)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.ICO)

            metadata.bitDepth shouldBe null
        }

        @Test
        @Suppress("MagicNumber")
        fun `ICO with zero image count returns unknown`() {
            val data = buildMinimalIco(width = 32, height = 32, imageCount = 0, bitCount = 32)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.ICO)

            metadata.width shouldBe 0
            metadata.height shouldBe 0
            metadata.format shouldBe "ICO"
        }

        @Test
        fun `returns unknown metadata for ICO data too short`() {
            val shortData = byteArrayOf(0x00, 0x00, 0x01, 0x00, 0x01, 0x00)
            val metadata = ImageMetadataExtractorImpl.extract(shortData, ImageFormat.ICO)

            metadata.width shouldBe 0
            metadata.height shouldBe 0
            metadata.format shouldBe "ICO"
        }
    }

    @Nested
    inner class SvgMetadata {

        @Test
        fun `extracts width and height from SVG attributes`() {
            val svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"150\"></svg>"
            val data = svg.toByteArray(Charsets.UTF_8)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.SVG)

            metadata.width shouldBe 200
            metadata.height shouldBe 150
            metadata.format shouldBe "SVG"
            metadata.hasAlpha shouldBe true
        }

        @Test
        fun `extracts dimensions from viewBox when width and height are missing`() {
            val svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 300 250\"></svg>"
            val data = svg.toByteArray(Charsets.UTF_8)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.SVG)

            metadata.width shouldBe 300
            metadata.height shouldBe 250
        }

        @Test
        fun `returns zero dimensions when no width height or viewBox`() {
            val svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>"
            val data = svg.toByteArray(Charsets.UTF_8)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.SVG)

            metadata.width shouldBe 0
            metadata.height shouldBe 0
            metadata.format shouldBe "SVG"
        }

        @Test
        fun `extracts width and height with px unit suffix`() {
            val svg = "<svg width=\"500px\" height=\"400px\"></svg>"
            val data = svg.toByteArray(Charsets.UTF_8)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.SVG)

            metadata.width shouldBe 500
            metadata.height shouldBe 400
        }

        @Test
        fun `falls back to viewBox when only width is specified`() {
            val svg = "<svg width=\"100\" viewBox=\"0 0 300 200\"></svg>"
            val data = svg.toByteArray(Charsets.UTF_8)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.SVG)

            // width is found but height is 0, so it falls back to viewBox
            metadata.width shouldBe 300
            metadata.height shouldBe 200
        }

        @Test
        fun `handles viewBox with floating point values`() {
            val svg = "<svg viewBox=\"0 0 100.5 200.7\"></svg>"
            val data = svg.toByteArray(Charsets.UTF_8)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.SVG)

            metadata.width shouldBe 100
            metadata.height shouldBe 200
        }

        @Test
        fun `SVG without svg tag returns zero dimensions`() {
            val notSvg = "<html><body>not an svg</body></html>"
            val data = notSvg.toByteArray(Charsets.UTF_8)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.SVG)

            metadata.width shouldBe 0
            metadata.height shouldBe 0
        }

        @Test
        fun `SVG with em units extracts numeric portion`() {
            val svg = "<svg width=\"50em\" height=\"30em\"></svg>"
            val data = svg.toByteArray(Charsets.UTF_8)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.SVG)

            metadata.width shouldBe 50
            metadata.height shouldBe 30
        }

        @Test
        fun `SVG colorSpace is null`() {
            val svg = "<svg width=\"100\" height=\"100\"></svg>"
            val data = svg.toByteArray(Charsets.UTF_8)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.SVG)

            metadata.colorSpace shouldBe null
        }

        @Test
        fun `SVG fileSize matches input data`() {
            val svg = "<svg width=\"100\" height=\"100\"></svg>"
            val data = svg.toByteArray(Charsets.UTF_8)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.SVG)

            metadata.fileSize shouldBe data.size.toLong()
        }

        @Test
        fun `viewBox with non-zero origin`() {
            val svg = "<svg viewBox=\"10 20 640 480\"></svg>"
            val data = svg.toByteArray(Charsets.UTF_8)
            val metadata = ImageMetadataExtractorImpl.extract(data, ImageFormat.SVG)

            // viewBox extracts 3rd and 4th values as width and height
            metadata.width shouldBe 640
            metadata.height shouldBe 480
        }
    }

    // --- Helper functions to build minimal valid binary headers ---

    @Suppress("MagicNumber")
    private fun buildMinimalPng(
        width: Int,
        height: Int,
        bitDepth: Int,
        colorType: Int,
    ): ByteArray {
        val buffer = ByteBuffer.allocate(26).order(ByteOrder.BIG_ENDIAN)
        // PNG signature (8 bytes)
        buffer.put(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        // IHDR chunk length (4 bytes)
        buffer.putInt(13)
        // "IHDR" (4 bytes)
        buffer.put("IHDR".toByteArray(Charsets.US_ASCII))
        // width (4 bytes)
        buffer.putInt(width)
        // height (4 bytes)
        buffer.putInt(height)
        // bit depth (1 byte)
        buffer.put(bitDepth.toByte())
        // color type (1 byte)
        buffer.put(colorType.toByte())
        return buffer.array()
    }

    @Suppress("MagicNumber")
    private fun buildMinimalJpeg(
        width: Int,
        height: Int,
        components: Int,
        precision: Int = 8,
    ): ByteArray {
        val buffer = ByteBuffer.allocate(20).order(ByteOrder.BIG_ENDIAN)
        // SOI marker
        buffer.put(0xFF.toByte())
        buffer.put(0xD8.toByte())
        // SOF0 marker
        buffer.put(0xFF.toByte())
        buffer.put(0xC0.toByte())
        // Segment length (including the 2-byte length field itself)
        buffer.putShort(17.toShort()) // 2 + 1 + 2 + 2 + 1 + (3 * components)
        // Precision
        buffer.put(precision.toByte())
        // Height
        buffer.putShort(height.toShort())
        // Width
        buffer.putShort(width.toShort())
        // Number of components
        buffer.put(components.toByte())
        // Pad remaining
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
        // Header "GIF89a"
        buffer.put("GIF89a".toByteArray(Charsets.US_ASCII))
        // Logical Screen Descriptor: width (2 bytes LE), height (2 bytes LE)
        buffer.putShort(width.toShort())
        buffer.putShort(height.toShort())
        // Packed byte (global color table flag, etc.)
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
        // BMP file header (14 bytes)
        buffer.put(0x42.toByte()) // 'B'
        buffer.put(0x4D.toByte()) // 'M'
        buffer.putInt(0) // file size placeholder
        buffer.putShort(0) // reserved
        buffer.putShort(0) // reserved
        buffer.putInt(0) // pixel data offset placeholder
        // DIB header (BITMAPINFOHEADER) - starts at offset 14
        buffer.putInt(40) // header size
        buffer.putInt(width) // width at offset 18
        buffer.putInt(height) // height at offset 22
        buffer.putShort(1) // color planes
        buffer.putShort(bitCount.toShort()) // bit count at offset 28
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
        // ICO header (6 bytes)
        buffer.putShort(0) // reserved
        buffer.putShort(1) // type (1 = ICO)
        buffer.putShort(imageCount.toShort()) // image count

        // First image entry (16 bytes, only need first 8 for our test)
        buffer.put(width.toByte()) // width (0 means 256)
        buffer.put(height.toByte()) // height (0 means 256)
        buffer.put(0) // color count
        buffer.put(0) // reserved
        buffer.putShort(1) // color planes
        buffer.putShort(bitCount.toShort()) // bit count
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
        // RIFF header
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(22) // file size - 8
        buffer.put("WEBP".toByteArray(Charsets.US_ASCII))
        // VP8 chunk header
        buffer.put("VP8 ".toByteArray(Charsets.US_ASCII))
        buffer.putInt(10) // chunk size

        // VP8 frame: 3 bytes of frame tag, then 3-byte signature
        buffer.put(0x00.toByte()) // frame tag byte 1
        buffer.put(0x00.toByte()) // frame tag byte 2
        buffer.put(0x00.toByte()) // frame tag byte 3

        // VP8 frame header signature: 9D 01 2A
        buffer.put(0x9D.toByte())
        buffer.put(0x01.toByte())
        buffer.put(0x2A.toByte())

        // Width and height in little-endian (with scale bits in upper 2 bits)
        buffer.putShort(width.toShort())
        buffer.putShort(height.toShort())

        return buffer.array()
    }

    @Suppress("MagicNumber")
    private fun buildMinimalWebPVP8L(
        width: Int,
        height: Int,
        hasAlpha: Boolean,
    ): ByteArray {
        // Must be at least 30 bytes because extractWebPMetadata checks data.size < 30
        val buffer = ByteBuffer.allocate(30).order(ByteOrder.LITTLE_ENDIAN)
        // RIFF header
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(22) // file size - 8
        buffer.put("WEBP".toByteArray(Charsets.US_ASCII))
        // VP8L chunk header
        buffer.put("VP8L".toByteArray(Charsets.US_ASCII))
        buffer.putInt(5) // chunk size

        // VP8L signature byte
        buffer.put(0x2F.toByte())

        // Packed bits: width-1 (14 bits) | height-1 (14 bits) | alpha (1 bit) | version (3 bits)
        val w = width - 1 and 0x3FFF
        val h = height - 1 and 0x3FFF
        val alphaBit = if (hasAlpha) 1 else 0
        val bits = w or (h shl 14) or (alphaBit shl 28)
        buffer.putInt(bits)

        // Pad to 30 bytes
        val remaining = buffer.remaining()
        buffer.put(ByteArray(remaining))

        return buffer.array()
    }

    @Suppress("MagicNumber")
    private fun buildMinimalWebPVP8X(
        width: Int,
        height: Int,
        hasAlpha: Boolean,
    ): ByteArray {
        val buffer = ByteBuffer.allocate(30).order(ByteOrder.LITTLE_ENDIAN)
        // RIFF header
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(22) // file size - 8
        buffer.put("WEBP".toByteArray(Charsets.US_ASCII))
        // VP8X chunk header
        buffer.put("VP8X".toByteArray(Charsets.US_ASCII))
        buffer.putInt(10) // chunk size

        // Flags byte at offset 20
        val flags = if (hasAlpha) 0x10 else 0x00
        buffer.put(flags.toByte())
        buffer.put(0x00.toByte()) // reserved
        buffer.put(0x00.toByte()) // reserved
        buffer.put(0x00.toByte()) // reserved

        // Canvas width - 1 (3 bytes, little-endian) at offset 24
        val w = width - 1
        buffer.put((w and 0xFF).toByte())
        buffer.put((w shr 8 and 0xFF).toByte())
        buffer.put((w shr 16 and 0xFF).toByte())

        // Canvas height - 1 (3 bytes, little-endian) at offset 27
        val h = height - 1
        buffer.put((h and 0xFF).toByte())
        buffer.put((h shr 8 and 0xFF).toByte())
        buffer.put((h shr 16 and 0xFF).toByte())

        return buffer.array()
    }

    @Suppress("MagicNumber")
    private fun buildWebPHeader(
        chunkType: String,
        dataSize: Int,
    ): ByteArray {
        val totalSize = 12 + 4 + 4 + dataSize // RIFF header + chunk header + chunk data
        val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(totalSize - 8)
        buffer.put("WEBP".toByteArray(Charsets.US_ASCII))
        buffer.put(chunkType.toByteArray(Charsets.US_ASCII))
        buffer.putInt(dataSize)
        if (dataSize > 0) {
            buffer.put(ByteArray(dataSize))
        }
        return buffer.array()
    }

    @Suppress("MagicNumber")
    private fun buildWebPHeaderWithPayload(
        chunkType: String,
        payload: ByteArray,
    ): ByteArray {
        val totalSize = 12 + 4 + 4 + payload.size
        val buffer = ByteBuffer.allocate(totalSize).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(totalSize - 8)
        buffer.put("WEBP".toByteArray(Charsets.US_ASCII))
        buffer.put(chunkType.toByteArray(Charsets.US_ASCII))
        buffer.putInt(payload.size)
        buffer.put(payload)
        return buffer.array()
    }
}

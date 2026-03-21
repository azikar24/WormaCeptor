package com.azikar24.wormaceptor.infra.parser.image

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MagicByteDetectorTest {

    @Nested
    inner class PngDetection {
        @Test
        fun `detect returns PNG for valid PNG signature`() {
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
            MagicByteDetector.detect(pngHeader) shouldBe ImageFormat.PNG
        }

        @Test
        fun `detect returns PNG for PNG signature with trailing data`() {
            val data = byteArrayOf(
                0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, // extra data
            )
            MagicByteDetector.detect(data) shouldBe ImageFormat.PNG
        }
    }

    @Nested
    inner class JpegDetection {
        @Test
        fun `detect returns JPEG for valid JPEG signature`() {
            val jpegHeader = byteArrayOf(
                0xFF.toByte(),
                0xD8.toByte(),
                0xFF.toByte(),
            )
            MagicByteDetector.detect(jpegHeader) shouldBe ImageFormat.JPEG
        }

        @Test
        fun `detect returns JPEG for JPEG with EXIF marker`() {
            val data = byteArrayOf(
                0xFF.toByte(),
                0xD8.toByte(),
                0xFF.toByte(),
                0xE1.toByte(),
            )
            MagicByteDetector.detect(data) shouldBe ImageFormat.JPEG
        }
    }

    @Nested
    inner class GifDetection {
        @Test
        fun `detect returns GIF for GIF87a signature`() {
            val gif87a = "GIF87a".toByteArray(Charsets.US_ASCII)
            MagicByteDetector.detect(gif87a) shouldBe ImageFormat.GIF
        }

        @Test
        fun `detect returns GIF for GIF89a signature`() {
            val gif89a = "GIF89a".toByteArray(Charsets.US_ASCII)
            MagicByteDetector.detect(gif89a) shouldBe ImageFormat.GIF
        }

        @Test
        fun `detect returns null for GIF with invalid version byte`() {
            // GIF80a - '0' is 0x30, neither 0x37 nor 0x39
            val invalidGif = "GIF80a".toByteArray(Charsets.US_ASCII)
            MagicByteDetector.detect(invalidGif).shouldBeNull()
        }

        @Test
        fun `detect returns null for GIF header too short`() {
            // Only "GIF8" (4 bytes) - need at least 6 for version check
            val shortGif = "GIF8".toByteArray(Charsets.US_ASCII)
            MagicByteDetector.detect(shortGif).shouldBeNull()
        }
    }

    @Nested
    inner class WebPDetection {
        @Test
        fun `detect returns WEBP for valid WebP signature`() {
            // RIFF????WEBP
            val webp = byteArrayOf(
                0x52, 0x49, 0x46, 0x46, // RIFF
                0x00, 0x00, 0x00, 0x00, // file size placeholder
                0x57, 0x45, 0x42, 0x50, // WEBP
            )
            MagicByteDetector.detect(webp) shouldBe ImageFormat.WEBP
        }

        @Test
        fun `detect returns null for RIFF without WEBP`() {
            val riffAvi = byteArrayOf(
                0x52, 0x49, 0x46, 0x46, // RIFF
                0x00, 0x00, 0x00, 0x00,
                0x41, 0x56, 0x49, 0x20, // AVI
            )
            MagicByteDetector.detect(riffAvi).shouldBeNull()
        }

        @Test
        fun `detect returns null for RIFF data too short for WEBP check`() {
            val shortRiff = byteArrayOf(
                0x52, 0x49, 0x46, 0x46,
                0x00, 0x00, 0x00, 0x00,
                0x57, 0x45, 0x42, // only 11 bytes, need 12
            )
            MagicByteDetector.detect(shortRiff).shouldBeNull()
        }
    }

    @Nested
    inner class BmpDetection {
        @Test
        fun `detect returns BMP for valid BMP signature`() {
            val bmp = byteArrayOf(0x42, 0x4D, 0x00, 0x00) // "BM" + data
            MagicByteDetector.detect(bmp) shouldBe ImageFormat.BMP
        }
    }

    @Nested
    inner class IcoDetection {
        @Test
        fun `detect returns ICO for valid ICO signature`() {
            val ico = byteArrayOf(0x00, 0x00, 0x01, 0x00)
            MagicByteDetector.detect(ico) shouldBe ImageFormat.ICO
        }

        @Test
        fun `detect does not return ICO for data shorter than 4 bytes`() {
            val shortData = byteArrayOf(0x00, 0x00, 0x01)
            // ICO needs exactly 4 bytes for signature check
            MagicByteDetector.detect(shortData).shouldBeNull()
        }
    }

    @Nested
    inner class SvgDetection {
        @Test
        fun `detect returns SVG for data containing svg tag`() {
            val svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\"></svg>"
                .toByteArray(Charsets.UTF_8)
            MagicByteDetector.detect(svg) shouldBe ImageFormat.SVG
        }

        @Test
        fun `detect returns SVG for XML declaration with svg content`() {
            val svg = "<?xml version=\"1.0\"?><svg></svg>".toByteArray(Charsets.UTF_8)
            MagicByteDetector.detect(svg) shouldBe ImageFormat.SVG
        }

        @Test
        fun `detect returns SVG for XML declaration containing svg keyword`() {
            val svg = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<svg></svg>"
                .toByteArray(Charsets.UTF_8)
            MagicByteDetector.detect(svg) shouldBe ImageFormat.SVG
        }
    }

    @Nested
    inner class EdgeCases {
        @Test
        fun `detect returns null for empty array`() {
            MagicByteDetector.detect(byteArrayOf()).shouldBeNull()
        }

        @Test
        fun `detect returns null for single byte`() {
            MagicByteDetector.detect(byteArrayOf(0x42)).shouldBeNull()
        }

        @Test
        fun `detect returns null for unrecognized bytes`() {
            val unknown = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)
            MagicByteDetector.detect(unknown).shouldBeNull()
        }

        @Test
        fun `detect returns null for data too short for any signature`() {
            val twoBytes = byteArrayOf(0x00, 0x00)
            MagicByteDetector.detect(twoBytes).shouldBeNull()
        }
    }
}

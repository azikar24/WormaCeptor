package com.azikar24.wormaceptor.api

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class BinaryContentDetectorTest {

    @Nested
    inner class `isBinaryContentType` {

        @ParameterizedTest
        @ValueSource(
            strings = [
                "image/png",
                "image/jpeg",
                "image/gif",
                "image/webp",
                "image/svg+xml",
                "audio/mpeg",
                "audio/ogg",
                "video/mp4",
                "video/webm",
                "application/octet-stream",
                "application/pdf",
                "application/zip",
                "application/gzip",
                "application/x-tar",
                "application/x-rar",
                "application/x-7z-compressed",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "font/woff2",
                "font/ttf",
                "model/gltf+json",
            ],
        )
        fun `detects binary content types`(contentType: String) {
            BinaryContentDetector.isBinaryContentType(contentType) shouldBe true
        }

        @ParameterizedTest
        @ValueSource(
            strings = [
                "text/plain",
                "text/html",
                "text/css",
                "text/javascript",
                "application/json",
                "application/xml",
                "application/x-www-form-urlencoded",
                "multipart/form-data",
            ],
        )
        fun `rejects text content types`(contentType: String) {
            BinaryContentDetector.isBinaryContentType(contentType) shouldBe false
        }

        @Test
        fun `returns false for null content type`() {
            BinaryContentDetector.isBinaryContentType(null) shouldBe false
        }

        @Test
        fun `is case-insensitive`() {
            BinaryContentDetector.isBinaryContentType("IMAGE/PNG") shouldBe true
            BinaryContentDetector.isBinaryContentType("Application/PDF") shouldBe true
        }

        @Test
        fun `handles content type with leading and trailing whitespace`() {
            BinaryContentDetector.isBinaryContentType("  image/png  ") shouldBe true
        }

        @Test
        fun `handles empty string`() {
            BinaryContentDetector.isBinaryContentType("") shouldBe false
        }
    }

    @Nested
    inner class `isBinaryByMagicBytes` {

        @Test
        fun `detects PNG by magic bytes`() {
            val png = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
            BinaryContentDetector.isBinaryByMagicBytes(png) shouldBe true
        }

        @Test
        fun `detects JPEG by magic bytes`() {
            val jpeg = byteArrayOf(
                0xFF.toByte(),
                0xD8.toByte(),
                0xFF.toByte(),
                0xE0.toByte(),
                0x00,
                0x10,
                0x4A,
                0x46,
            )
            BinaryContentDetector.isBinaryByMagicBytes(jpeg) shouldBe true
        }

        @Test
        fun `detects GIF by magic bytes`() {
            val gif = byteArrayOf(0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x00, 0x00)
            BinaryContentDetector.isBinaryByMagicBytes(gif) shouldBe true
        }

        @Test
        @Suppress("MagicNumber")
        fun `detects WebP by magic bytes`() {
            // RIFF....WEBP
            val webp = byteArrayOf(
                0x52, 0x49, 0x46, 0x46, // RIFF
                0x00, 0x00, 0x00, 0x00, // file size placeholder
                0x57, 0x45, 0x42, 0x50, // WEBP
            )
            BinaryContentDetector.isBinaryByMagicBytes(webp) shouldBe true
        }

        @Test
        fun `detects PDF by magic bytes`() {
            // %PDF
            val pdf = byteArrayOf(0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34)
            BinaryContentDetector.isBinaryByMagicBytes(pdf) shouldBe true
        }

        @Test
        fun `detects ZIP by magic bytes`() {
            val zip = byteArrayOf(0x50, 0x4B, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00)
            BinaryContentDetector.isBinaryByMagicBytes(zip) shouldBe true
        }

        @Test
        fun `detects GZIP by magic bytes`() {
            val gzip = byteArrayOf(0x1F, 0x8B.toByte(), 0x08, 0x00, 0x00, 0x00, 0x00, 0x00)
            BinaryContentDetector.isBinaryByMagicBytes(gzip) shouldBe true
        }

        @Test
        fun `detects BMP by magic bytes`() {
            // BM
            val bmp = byteArrayOf(0x42, 0x4D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
            BinaryContentDetector.isBinaryByMagicBytes(bmp) shouldBe true
        }

        @Test
        fun `returns false for plain text bytes`() {
            val text = """{"hello":"world"}""".toByteArray()
            BinaryContentDetector.isBinaryByMagicBytes(text) shouldBe false
        }

        @Test
        fun `returns false for data smaller than minimum magic bytes size`() {
            val small = byteArrayOf(0x89.toByte(), 0x50, 0x4E)
            BinaryContentDetector.isBinaryByMagicBytes(small) shouldBe false
        }

        @Test
        fun `returns false for empty byte array`() {
            BinaryContentDetector.isBinaryByMagicBytes(byteArrayOf()) shouldBe false
        }

        @Test
        fun `returns false for RIFF header without WEBP signature`() {
            // RIFF header but not WEBP (e.g., WAVE)
            val wave = byteArrayOf(
                0x52, 0x49, 0x46, 0x46,
                0x00, 0x00, 0x00, 0x00,
                0x57, 0x41, 0x56, 0x45, // WAVE, not WEBP
            )
            BinaryContentDetector.isBinaryByMagicBytes(wave) shouldBe false
        }
    }
}

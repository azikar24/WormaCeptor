package com.azikar24.wormaceptor.infra.parser.image

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Extracts metadata (dimensions, color info) from image data.
 *
 * Each format has specific locations where metadata is stored.
 * This extractor parses the raw bytes to find this information.
 */
object ImageMetadataExtractor {

    /**
     * Extracts metadata from image data based on the detected format.
     *
     * @param data The raw image bytes
     * @param format The detected image format
     * @return ImageMetadata with extracted information
     */
    fun extract(data: ByteArray, format: ImageFormat): ImageMetadata {
        return when (format) {
            ImageFormat.PNG -> extractPngMetadata(data)
            ImageFormat.JPEG -> extractJpegMetadata(data)
            ImageFormat.GIF -> extractGifMetadata(data)
            ImageFormat.WEBP -> extractWebPMetadata(data)
            ImageFormat.BMP -> extractBmpMetadata(data)
            ImageFormat.ICO -> extractIcoMetadata(data)
            ImageFormat.SVG -> extractSvgMetadata(data)
        }
    }

    /**
     * Extracts PNG metadata from IHDR chunk.
     * PNG structure: signature (8) + IHDR chunk
     * IHDR chunk: length (4) + "IHDR" (4) + width (4) + height (4) + bit depth (1) + color type (1) + ...
     */
    private fun extractPngMetadata(data: ByteArray): ImageMetadata {
        if (data.size < 24) return ImageMetadata.unknown(data.size.toLong()).copy(format = "PNG")

        try {
            // IHDR chunk starts at offset 8, data starts at offset 16
            val width = ByteBuffer.wrap(data, 16, 4).order(ByteOrder.BIG_ENDIAN).int
            val height = ByteBuffer.wrap(data, 20, 4).order(ByteOrder.BIG_ENDIAN).int
            val bitDepth = data[24].toInt() and 0xFF
            val colorType = data[25].toInt() and 0xFF

            val (colorSpace, hasAlpha) = when (colorType) {
                0 -> "Grayscale" to false
                2 -> "RGB" to false
                3 -> "Indexed" to false
                4 -> "Grayscale" to true
                6 -> "RGBA" to true
                else -> null to false
            }

            return ImageMetadata(
                width = width,
                height = height,
                format = "PNG",
                colorSpace = colorSpace,
                hasAlpha = hasAlpha,
                bitDepth = bitDepth,
                fileSize = data.size.toLong()
            )
        } catch (e: Exception) {
            return ImageMetadata.unknown(data.size.toLong()).copy(format = "PNG")
        }
    }

    /**
     * Extracts JPEG metadata by parsing SOF (Start of Frame) markers.
     * JPEG is segment-based: FF xx (marker) + length (2) + data
     * SOF0/SOF2 contains: precision (1) + height (2) + width (2) + components (1)
     */
    private fun extractJpegMetadata(data: ByteArray): ImageMetadata {
        if (data.size < 4) return ImageMetadata.unknown(data.size.toLong()).copy(format = "JPEG")

        try {
            var offset = 2 // Skip SOI marker (FF D8)

            while (offset < data.size - 1) {
                // Find next marker
                if (data[offset] != 0xFF.toByte()) {
                    offset++
                    continue
                }

                val marker = data[offset + 1].toInt() and 0xFF
                offset += 2

                // Skip padding FF bytes
                if (marker == 0xFF) continue

                // End of image
                if (marker == 0xD9) break

                // Markers without length (RST, SOI, EOI, TEM)
                if (marker in 0xD0..0xD9 || marker == 0x01) continue

                // Read segment length
                if (offset + 2 > data.size) break
                val length = ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)

                // SOF markers (Start of Frame) - contain dimensions
                // SOF0 (baseline), SOF1 (extended), SOF2 (progressive), etc.
                if (marker in 0xC0..0xCF && marker !in listOf(0xC4, 0xC8, 0xCC)) {
                    if (offset + 7 <= data.size) {
                        val precision = data[offset + 2].toInt() and 0xFF
                        val height = ((data[offset + 3].toInt() and 0xFF) shl 8) or (data[offset + 4].toInt() and 0xFF)
                        val width = ((data[offset + 5].toInt() and 0xFF) shl 8) or (data[offset + 6].toInt() and 0xFF)
                        val components = data[offset + 7].toInt() and 0xFF

                        val colorSpace = when (components) {
                            1 -> "Grayscale"
                            3 -> "YCbCr" // Usually converted to RGB
                            4 -> "CMYK"
                            else -> null
                        }

                        return ImageMetadata(
                            width = width,
                            height = height,
                            format = "JPEG",
                            colorSpace = colorSpace,
                            hasAlpha = false, // JPEG doesn't support alpha
                            bitDepth = precision,
                            fileSize = data.size.toLong()
                        )
                    }
                }

                offset += length
            }

            return ImageMetadata.unknown(data.size.toLong()).copy(format = "JPEG")
        } catch (e: Exception) {
            return ImageMetadata.unknown(data.size.toLong()).copy(format = "JPEG")
        }
    }

    /**
     * Extracts GIF metadata from Logical Screen Descriptor.
     * GIF structure: header (6) + LSD (7) where LSD contains width/height
     */
    private fun extractGifMetadata(data: ByteArray): ImageMetadata {
        if (data.size < 10) return ImageMetadata.unknown(data.size.toLong()).copy(format = "GIF")

        try {
            // Width and height are at offset 6-9, little-endian
            val width = ((data[7].toInt() and 0xFF) shl 8) or (data[6].toInt() and 0xFF)
            val height = ((data[9].toInt() and 0xFF) shl 8) or (data[8].toInt() and 0xFF)

            return ImageMetadata(
                width = width,
                height = height,
                format = "GIF",
                colorSpace = "Indexed",
                hasAlpha = true, // GIF supports transparency
                bitDepth = 8,
                fileSize = data.size.toLong()
            )
        } catch (e: Exception) {
            return ImageMetadata.unknown(data.size.toLong()).copy(format = "GIF")
        }
    }

    /**
     * Extracts WebP metadata.
     * WebP has multiple formats: VP8 (lossy), VP8L (lossless), VP8X (extended)
     */
    private fun extractWebPMetadata(data: ByteArray): ImageMetadata {
        if (data.size < 30) return ImageMetadata.unknown(data.size.toLong()).copy(format = "WebP")

        try {
            // Check chunk type at offset 12
            val chunkType = String(data.sliceArray(12..15), Charsets.US_ASCII)

            return when {
                chunkType == "VP8 " -> extractWebPVP8(data)
                chunkType == "VP8L" -> extractWebPVP8L(data)
                chunkType == "VP8X" -> extractWebPVP8X(data)
                else -> ImageMetadata.unknown(data.size.toLong()).copy(format = "WebP")
            }
        } catch (e: Exception) {
            return ImageMetadata.unknown(data.size.toLong()).copy(format = "WebP")
        }
    }

    /**
     * Extracts dimensions from VP8 (lossy) WebP.
     */
    private fun extractWebPVP8(data: ByteArray): ImageMetadata {
        // VP8 bitstream starts at offset 20
        // Frame header has width/height at specific bit positions
        if (data.size < 30) return ImageMetadata.unknown(data.size.toLong()).copy(format = "WebP")

        try {
            // Skip RIFF header (12) + VP8 chunk header (8) + frame tag (3)
            val offset = 23
            if (offset + 7 > data.size) return ImageMetadata.unknown(data.size.toLong()).copy(format = "WebP")

            // Check for keyframe signature
            if (data[offset] != 0x9D.toByte() || data[offset + 1] != 0x01.toByte() || data[offset + 2] != 0x2A.toByte()) {
                return ImageMetadata.unknown(data.size.toLong()).copy(format = "WebP")
            }

            // Width and height follow (little-endian, 16 bits each, but only 14 bits used)
            val width = ((data[offset + 4].toInt() and 0xFF) shl 8) or (data[offset + 3].toInt() and 0xFF)
            val height = ((data[offset + 6].toInt() and 0xFF) shl 8) or (data[offset + 5].toInt() and 0xFF)

            return ImageMetadata(
                width = width and 0x3FFF, // 14-bit value
                height = height and 0x3FFF,
                format = "WebP",
                colorSpace = "YUV",
                hasAlpha = false,
                fileSize = data.size.toLong()
            )
        } catch (e: Exception) {
            return ImageMetadata.unknown(data.size.toLong()).copy(format = "WebP")
        }
    }

    /**
     * Extracts dimensions from VP8L (lossless) WebP.
     */
    private fun extractWebPVP8L(data: ByteArray): ImageMetadata {
        if (data.size < 25) return ImageMetadata.unknown(data.size.toLong()).copy(format = "WebP")

        try {
            // VP8L signature byte at offset 20
            if (data[20] != 0x2F.toByte()) {
                return ImageMetadata.unknown(data.size.toLong()).copy(format = "WebP")
            }

            // Width and height encoded in 4 bytes starting at offset 21
            val bits = ByteBuffer.wrap(data, 21, 4).order(ByteOrder.LITTLE_ENDIAN).int

            val width = (bits and 0x3FFF) + 1 // 14 bits
            val height = ((bits shr 14) and 0x3FFF) + 1 // 14 bits
            val hasAlpha = ((bits shr 28) and 1) == 1

            return ImageMetadata(
                width = width,
                height = height,
                format = "WebP",
                colorSpace = "RGBA",
                hasAlpha = hasAlpha,
                fileSize = data.size.toLong()
            )
        } catch (e: Exception) {
            return ImageMetadata.unknown(data.size.toLong()).copy(format = "WebP")
        }
    }

    /**
     * Extracts dimensions from VP8X (extended) WebP.
     */
    private fun extractWebPVP8X(data: ByteArray): ImageMetadata {
        if (data.size < 30) return ImageMetadata.unknown(data.size.toLong()).copy(format = "WebP")

        try {
            // Flags at offset 20
            val flags = data[20].toInt() and 0xFF
            val hasAlpha = (flags and 0x10) != 0

            // Canvas width (24 bits, little-endian) at offset 24
            val width = ((data[24].toInt() and 0xFF) or
                    ((data[25].toInt() and 0xFF) shl 8) or
                    ((data[26].toInt() and 0xFF) shl 16)) + 1

            // Canvas height (24 bits, little-endian) at offset 27
            val height = ((data[27].toInt() and 0xFF) or
                    ((data[28].toInt() and 0xFF) shl 8) or
                    ((data[29].toInt() and 0xFF) shl 16)) + 1

            return ImageMetadata(
                width = width,
                height = height,
                format = "WebP",
                colorSpace = if (hasAlpha) "RGBA" else "RGB",
                hasAlpha = hasAlpha,
                fileSize = data.size.toLong()
            )
        } catch (e: Exception) {
            return ImageMetadata.unknown(data.size.toLong()).copy(format = "WebP")
        }
    }

    /**
     * Extracts BMP metadata from BITMAPINFOHEADER.
     * BMP structure: file header (14) + DIB header (varies)
     */
    private fun extractBmpMetadata(data: ByteArray): ImageMetadata {
        if (data.size < 26) return ImageMetadata.unknown(data.size.toLong()).copy(format = "BMP")

        try {
            // Width at offset 18, height at offset 22 (little-endian, signed 32-bit)
            val width = ByteBuffer.wrap(data, 18, 4).order(ByteOrder.LITTLE_ENDIAN).int
            val height = ByteBuffer.wrap(data, 22, 4).order(ByteOrder.LITTLE_ENDIAN).int
            val bitCount = ByteBuffer.wrap(data, 28, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt()

            val hasAlpha = bitCount == 32

            return ImageMetadata(
                width = width,
                height = kotlin.math.abs(height), // Height can be negative (top-down)
                format = "BMP",
                colorSpace = if (bitCount <= 8) "Indexed" else "RGB",
                hasAlpha = hasAlpha,
                bitDepth = bitCount,
                fileSize = data.size.toLong()
            )
        } catch (e: Exception) {
            return ImageMetadata.unknown(data.size.toLong()).copy(format = "BMP")
        }
    }

    /**
     * Extracts ICO metadata from the first icon entry.
     * ICO structure: header (6) + entries (16 each)
     */
    private fun extractIcoMetadata(data: ByteArray): ImageMetadata {
        if (data.size < 22) return ImageMetadata.unknown(data.size.toLong()).copy(format = "ICO")

        try {
            val imageCount = ByteBuffer.wrap(data, 4, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt()

            if (imageCount < 1) return ImageMetadata.unknown(data.size.toLong()).copy(format = "ICO")

            // First entry starts at offset 6
            // Entry: width (1) + height (1) + color count (1) + reserved (1) + planes (2) + bit count (2) + size (4) + offset (4)
            var width = data[6].toInt() and 0xFF
            var height = data[7].toInt() and 0xFF

            // 0 means 256 pixels
            if (width == 0) width = 256
            if (height == 0) height = 256

            val bitCount = ByteBuffer.wrap(data, 12, 2).order(ByteOrder.LITTLE_ENDIAN).short.toInt()

            return ImageMetadata(
                width = width,
                height = height,
                format = "ICO ($imageCount images)",
                colorSpace = null,
                hasAlpha = true, // ICO typically has transparency
                bitDepth = if (bitCount > 0) bitCount else null,
                fileSize = data.size.toLong()
            )
        } catch (e: Exception) {
            return ImageMetadata.unknown(data.size.toLong()).copy(format = "ICO")
        }
    }

    /**
     * Extracts SVG metadata by parsing width/height attributes.
     * SVG is XML-based, so we do basic string parsing.
     */
    private fun extractSvgMetadata(data: ByteArray): ImageMetadata {
        val fileSize = data.size.toLong()
        val defaultMetadata = ImageMetadata(
            width = 0,
            height = 0,
            format = "SVG",
            colorSpace = null,
            hasAlpha = true, // SVG supports transparency
            fileSize = fileSize
        )

        try {
            val content = String(data, Charsets.UTF_8)

            // Find <svg> tag
            val svgMatch = Regex("<svg[^>]*>", RegexOption.IGNORE_CASE).find(content)
                ?: return defaultMetadata

            val svgTag = svgMatch.value

            // Extract width and height attributes
            val widthMatch = Regex("""width\s*=\s*["']?(\d+(?:\.\d+)?)(px|em|%|pt|cm|mm|in)?["']?""", RegexOption.IGNORE_CASE)
                .find(svgTag)
            val heightMatch = Regex("""height\s*=\s*["']?(\d+(?:\.\d+)?)(px|em|%|pt|cm|mm|in)?["']?""", RegexOption.IGNORE_CASE)
                .find(svgTag)

            val width = widthMatch?.groupValues?.get(1)?.toFloatOrNull()?.toInt() ?: 0
            val height = heightMatch?.groupValues?.get(1)?.toFloatOrNull()?.toInt() ?: 0

            // If no explicit dimensions, try viewBox
            if (width == 0 || height == 0) {
                val viewBoxMatch = Regex("""viewBox\s*=\s*["']?(\d+(?:\.\d+)?)\s+(\d+(?:\.\d+)?)\s+(\d+(?:\.\d+)?)\s+(\d+(?:\.\d+)?)["']?""", RegexOption.IGNORE_CASE)
                    .find(svgTag)

                if (viewBoxMatch != null) {
                    val vbWidth = viewBoxMatch.groupValues[3].toFloatOrNull()?.toInt() ?: 0
                    val vbHeight = viewBoxMatch.groupValues[4].toFloatOrNull()?.toInt() ?: 0
                    return defaultMetadata.copy(width = vbWidth, height = vbHeight)
                }
            }

            return defaultMetadata.copy(width = width, height = height)
        } catch (e: Exception) {
            return defaultMetadata
        }
    }
}

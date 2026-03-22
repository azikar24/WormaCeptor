package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.ImageMetadata

/**
 * Extracts metadata from raw image data without requiring Android framework classes.
 *
 * Implementations parse image headers (magic bytes, format-specific structures)
 * to extract dimensions, color space, and other metadata.
 */
interface ImageMetadataExtractor {

    /**
     * Extracts metadata from raw image bytes.
     *
     * @param data The raw image bytes
     * @return Extracted metadata, or [ImageMetadata.unknown] if parsing fails
     */
    fun extractMetadata(data: ByteArray): ImageMetadata

    /**
     * Checks if the raw bytes represent a recognized image format.
     *
     * @param data The raw bytes to check
     * @return true if the data has known image magic bytes
     */
    fun isImageData(data: ByteArray): Boolean
}

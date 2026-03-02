package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.MultipartPart

/**
 * Parser for multipart form data content.
 *
 * Implementations split multipart data by boundary and extract each part's
 * headers, name, filename, content type, and body.
 */
interface MultipartParser {
    /**
     * Parses multipart data into structured parts.
     *
     * @param data The raw multipart data string
     * @param boundary The multipart boundary, or null to auto-detect
     * @return List of parsed [MultipartPart] entries
     */
    fun parse(
        data: String,
        boundary: String?,
    ): List<MultipartPart>
}

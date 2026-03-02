package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.FormParameter

/**
 * Parser for URL-encoded form data (application/x-www-form-urlencoded).
 *
 * Implementations decode key-value pairs from URL-encoded strings,
 * handling percent-encoded characters.
 */
interface FormDataParser {
    /**
     * Parses URL-encoded form data into key-value pairs.
     *
     * @param formData The URL-encoded form data string
     * @return List of decoded [FormParameter] entries
     */
    fun parse(formData: String): List<FormParameter>
}

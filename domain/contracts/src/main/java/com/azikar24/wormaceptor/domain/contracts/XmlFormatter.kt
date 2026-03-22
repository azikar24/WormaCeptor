package com.azikar24.wormaceptor.domain.contracts

/**
 * Formatter for XML content with proper indentation.
 *
 * Implementations take raw XML strings and produce formatted, indented output
 * suitable for display in a tree view.
 */
interface XmlFormatter {
    /**
     * Formats an XML string with proper indentation.
     *
     * @param xml The raw XML string
     * @return List of formatted lines with indentation applied
     */
    fun format(xml: String): List<String>
}

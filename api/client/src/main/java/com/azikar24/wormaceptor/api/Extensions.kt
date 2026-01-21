package com.azikar24.wormaceptor.api

/**
 * Re-exports for public API - extension system types.
 *
 * These type aliases allow users to work with the extension system
 * through the public API without depending on internal modules.
 */

/**
 * Context provided to extension providers for extracting custom metadata.
 * @see com.azikar24.wormaceptor.domain.contracts.ExtensionContext
 */
typealias ExtensionContext = com.azikar24.wormaceptor.domain.contracts.ExtensionContext

/**
 * Interface for custom extension providers that extract metadata from network transactions.
 * @see com.azikar24.wormaceptor.domain.contracts.ExtensionProvider
 */
typealias ExtensionProvider = com.azikar24.wormaceptor.domain.contracts.ExtensionProvider

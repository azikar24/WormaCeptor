package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response

/**
 * Context provided to extension providers for extracting custom metadata.
 *
 * @property request The HTTP request data
 * @property response The HTTP response data, may be null if the request failed
 * @property durationMs The total duration of the request in milliseconds
 * @property timestamp The timestamp when the request was initiated
 */
data class ExtensionContext(
    val request: Request,
    val response: Response?,
    val durationMs: Long?,
    val timestamp: Long,
)

/**
 * Interface for custom extension providers that extract metadata from network transactions.
 *
 * Implementations can analyze request/response data and return custom key-value pairs
 * that will be stored with the transaction and displayed in the UI.
 *
 * Example usage:
 * ```kotlin
 * class AuthTokenExtension : ExtensionProvider {
 *     override val name = "AuthToken"
 *     override fun extractExtensions(context: ExtensionContext): Map<String, String> {
 *         val authHeader = context.request.headers["Authorization"]?.firstOrNull()
 *         return if (authHeader != null) {
 *             mapOf("auth_type" to authHeader.substringBefore(" "))
 *         } else {
 *             emptyMap()
 *         }
 *     }
 * }
 * ```
 */
interface ExtensionProvider {
    /**
     * Unique name identifying this extension provider.
     * Used for registration and display purposes.
     */
    val name: String

    /**
     * Priority for extension extraction. Higher values are processed first.
     * Default is 50. Use higher values for extensions that should run first.
     */
    val priority: Int get() = 50

    /**
     * Extract custom metadata from the transaction context.
     *
     * @param context The transaction context containing request, response, and timing data
     * @return A map of custom key-value pairs to store with the transaction.
     *         Return an empty map if no metadata should be added.
     */
    fun extractExtensions(context: ExtensionContext): Map<String, String>
}

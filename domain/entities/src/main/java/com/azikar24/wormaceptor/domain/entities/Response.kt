package com.azikar24.wormaceptor.domain.entities

/**
 * Represents the received HTTP response portion of a network transaction.
 *
 * @property code HTTP status code (e.g. 200, 404, 500).
 * @property message HTTP reason phrase returned by the server.
 * @property headers Response headers keyed by name with multi-value support.
 * @property bodyRef Reference to the persisted response body blob, or null if absent.
 * @property error Error description if the response could not be received.
 * @property protocol Protocol used (e.g. "http/1.1", "h2").
 * @property tlsVersion TLS version negotiated for the connection.
 * @property bodySize Size of the response body in bytes.
 */
data class Response(
    val code: Int,
    val message: String,
    val headers: Map<String, List<String>>,
    val bodyRef: BlobID?,
    val error: String? = null,
    val protocol: String? = null,
    val tlsVersion: String? = null,
    val bodySize: Long = 0,
)

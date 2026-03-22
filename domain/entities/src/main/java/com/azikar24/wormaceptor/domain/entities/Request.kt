package com.azikar24.wormaceptor.domain.entities

/**
 * Represents the outgoing HTTP request portion of a network transaction.
 *
 * @property url The full request URL including query parameters.
 * @property method HTTP method (e.g. GET, POST, PUT, DELETE).
 * @property headers Request headers keyed by name with multi-value support.
 * @property bodyRef Reference to the persisted request body blob, or null if absent.
 * @property bodySize Size of the request body in bytes.
 */
data class Request(
    val url: String,
    val method: String,
    val headers: Map<String, List<String>>,
    val bodyRef: BlobID?,
    val bodySize: Long = 0,
)

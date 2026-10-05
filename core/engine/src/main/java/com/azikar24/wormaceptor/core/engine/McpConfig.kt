package com.azikar24.wormaceptor.core.engine

/**
 * Settings for the debug-only MCP device server.
 *
 * @property port Port the server binds on localhost
 * @property enableAuth Whether requests must carry a bearer token
 * @property authToken Bearer token required when [enableAuth] is true
 * @property maxBodySize Maximum transaction body size, in bytes, returned by the server
 * @property enabled Whether the server starts automatically
 */
data class McpConfig(
    val port: Int = DEFAULT_PORT,
    val enableAuth: Boolean = false,
    val authToken: String? = null,
    val maxBodySize: Long = DEFAULT_MAX_BODY_SIZE,
    val enabled: Boolean = true,
) {
    /** Default values for [McpConfig]. */
    companion object {
        /** Default server port. */
        const val DEFAULT_PORT = 8999

        /** Default maximum body size: 1 MiB. */
        const val DEFAULT_MAX_BODY_SIZE = 1_048_576L
    }
}

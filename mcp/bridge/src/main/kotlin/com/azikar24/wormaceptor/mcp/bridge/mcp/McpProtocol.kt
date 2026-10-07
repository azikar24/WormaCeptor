package com.azikar24.wormaceptor.mcp.bridge.mcp

internal object McpProtocol {
    /**
     * Newest first. 2025-03-26 is left out: it requires servers to accept JSON-RPC batches,
     * which the bridge doesn't; 2025-06-18 dropped batching again.
     */
    val SUPPORTED_PROTOCOL_VERSIONS = listOf("2025-11-25", "2025-06-18", "2024-11-05")
    const val SERVER_NAME = "wormaceptor-bridge"

    /** The WormaCeptor release, stamped into the jar manifest; "dev" when run from classes. */
    val SERVER_VERSION: String = McpProtocol::class.java.`package`?.implementationVersion ?: "dev"

    /** Spec lifecycle: echo the client's version when supported, otherwise offer the newest one. */
    fun negotiateVersion(requested: String?): String =
        requested?.takeIf { it in SUPPORTED_PROTOCOL_VERSIONS } ?: SUPPORTED_PROTOCOL_VERSIONS.first()
}

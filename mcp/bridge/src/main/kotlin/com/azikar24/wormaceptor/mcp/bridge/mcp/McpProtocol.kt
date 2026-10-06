package com.azikar24.wormaceptor.mcp.bridge.mcp

internal object McpProtocol {
    const val PROTOCOL_VERSION = "2024-11-05"
    const val SERVER_NAME = "wormaceptor-bridge"

    /** The WormaCeptor release, stamped into the jar manifest; "dev" when run from classes. */
    val SERVER_VERSION: String = McpProtocol::class.java.`package`?.implementationVersion ?: "dev"
}

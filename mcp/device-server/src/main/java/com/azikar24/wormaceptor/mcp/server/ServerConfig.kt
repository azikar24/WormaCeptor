package com.azikar24.wormaceptor.mcp.server

internal data class ServerConfig(
    val port: Int = DEFAULT_PORT,
    val enableAuth: Boolean = false,
    val authToken: String? = null,
    val maxBodySize: Long = DEFAULT_MAX_BODY_SIZE,
    val enabledCategories: Set<ApiCategory> = ApiCategory.entries.toSet(),
) {
    companion object {
        const val DEFAULT_PORT = 8999
        const val DEFAULT_MAX_BODY_SIZE = 1_048_576L
    }
}

internal enum class ApiCategory {
    NETWORK,
    DIAGNOSTICS,
    PERFORMANCE,
    STORAGE,
    ACTIONS,
    STREAMING,
}

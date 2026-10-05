package com.azikar24.wormaceptor.mcp.server.serialization.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class ApiResponse(
    val success: Boolean,
    val data: JsonElement? = null,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val meta: ResponseMeta? = null,
)

@Serializable
internal data class ResponseMeta(
    val total: Int? = null,
    val limit: Int? = null,
    val offset: Int? = null,
    val truncated: Boolean = false,
    val totalSize: Long? = null,
)

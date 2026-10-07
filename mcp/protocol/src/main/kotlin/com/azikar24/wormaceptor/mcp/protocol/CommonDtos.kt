package com.azikar24.wormaceptor.mcp.protocol

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

/** `/api/health`. The bridge caches [packageName] to run adb diagnostics when the app stops answering. */
@Serializable
internal data class HealthDto(
    val success: Boolean = true,
    val server: String,
    val version: String,
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String? = null,
)

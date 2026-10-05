package com.azikar24.wormaceptor.mcp.bridge.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val id: JsonElement? = null,
    val params: JsonElement? = null,
)

@Serializable
internal data class JsonRpcResponse(
    val jsonrpc: String = "2.0",
    val id: JsonElement? = null,
    val result: JsonElement? = null,
    val error: JsonRpcError? = null,
)

@Serializable
internal data class JsonRpcError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null,
)

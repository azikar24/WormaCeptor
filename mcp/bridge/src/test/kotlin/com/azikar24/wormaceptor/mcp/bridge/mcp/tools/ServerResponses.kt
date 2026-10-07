package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.protocol.ApiResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

/** Same encoding as the device server's `JsonConfig`: defaults are written out. */
internal val ServerJson = Json { encodeDefaults = true }

/** A successful device-server response carrying [data], encoded the way the server does it. */
internal inline fun <reified T> serverResponse(data: T): JsonElement =
    ServerJson.encodeToJsonElement(ApiResponse(success = true, data = ServerJson.encodeToJsonElement(data)))

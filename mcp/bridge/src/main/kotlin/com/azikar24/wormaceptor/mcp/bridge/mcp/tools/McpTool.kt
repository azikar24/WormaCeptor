package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal abstract class McpTool {

    abstract val name: String

    abstract val description: String

    abstract val inputSchema: JsonObject

    abstract suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String

    fun toSchema(): JsonObject = buildJsonObject {
        put("name", name)
        put("description", description)
        put("inputSchema", inputSchema)
    }
}

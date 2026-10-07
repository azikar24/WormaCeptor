package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/** MCP `ToolAnnotations` hints (spec 2025-03-26 onward). Every tool only touches the app on the device. */
internal data class ToolAnnotations(
    val readOnlyHint: Boolean,
    val destructiveHint: Boolean,
    val idempotentHint: Boolean,
    val openWorldHint: Boolean = false,
) {
    companion object {
        val ReadOnly = ToolAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true)
        val Destructive = ToolAnnotations(readOnlyHint = false, destructiveHint = true, idempotentHint = true)
    }
}

internal abstract class McpTool {

    abstract val name: String

    abstract val description: String

    abstract val inputSchema: JsonObject

    open val annotations: ToolAnnotations = ToolAnnotations.ReadOnly

    abstract suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String

    fun toSchema(): JsonObject = buildJsonObject {
        put("name", name)
        put("description", description)
        put("inputSchema", inputSchema)
        putJsonObject("annotations") {
            put("readOnlyHint", annotations.readOnlyHint)
            put("destructiveHint", annotations.destructiveHint)
            put("idempotentHint", annotations.idempotentHint)
            put("openWorldHint", annotations.openWorldHint)
        }
    }
}

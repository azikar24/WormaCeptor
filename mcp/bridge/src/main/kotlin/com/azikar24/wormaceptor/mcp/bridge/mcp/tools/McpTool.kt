package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
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

    /**
     * Safely extract a [JsonObject] from a parent object by key,
     * treating both missing keys and JSON null as Kotlin null.
     */
    protected fun JsonObject.objectOrNull(key: String): JsonObject? {
        val element = this[key] ?: return null
        if (element is JsonNull) return null
        return element.jsonObject
    }

    /**
     * Safely extract a [JsonArray] from a parent object by key,
     * treating both missing keys and JSON null as Kotlin null.
     */
    protected fun JsonObject.arrayOrNull(key: String): JsonArray? {
        val element = this[key] ?: return null
        if (element is JsonNull) return null
        return element.jsonArray
    }
}

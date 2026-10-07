package com.azikar24.wormaceptor.mcp.bridge.mcp

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

internal object McpCapabilities {

    fun initializeResponse(requestedVersion: String?): JsonElement = buildJsonObject {
        put("protocolVersion", McpProtocol.negotiateVersion(requestedVersion))
        putJsonObject("capabilities") {
            putJsonObject("tools") {}
        }
        putJsonObject("serverInfo") {
            put("name", McpProtocol.SERVER_NAME)
            put("version", McpProtocol.SERVER_VERSION)
        }
    }
}

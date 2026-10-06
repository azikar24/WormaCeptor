package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.bridge.util.TextFormatter
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

internal class ListTransactionsTool : McpTool() {

    override val name = "list_transactions"

    override val description = "List captured HTTP network transactions from the running Android app. " +
        "Returns method, URL, status code, duration, and timestamp for each request. " +
        "Use to investigate API calls, find failed requests, or verify request parameters. " +
        "Supports filtering by URL, method, or status code via the query parameter, " +
        "and pagination via limit/offset."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("query") {
                put("type", "string")
                put("description", "Filter transactions by URL, HTTP method, or status code substring match")
            }
            putJsonObject("limit") {
                put("type", "integer")
                put("description", "Maximum number of transactions to return (default: 50)")
                put("default", 50)
            }
            putJsonObject("offset") {
                put("type", "integer")
                put("description", "Number of transactions to skip for pagination (default: 0)")
                put("default", 0)
            }
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val params = buildMap {
            arguments["query"]?.jsonPrimitive?.contentOrNull?.let { put("query", it) }
            arguments["limit"]?.jsonPrimitive?.intOrNull?.let { put("limit", it.toString()) }
            arguments["offset"]?.jsonPrimitive?.intOrNull?.let { put("offset", it.toString()) }
        }

        val response = connection.apiClient.get("/api/transactions", params)
        val data = response.jsonObject.arrayOrNull("data") ?: return response.serverError() ?: "No transactions found."
        return TextFormatter.formatTransactionList(data)
    }
}

internal class GetTransactionTool : McpTool() {

    override val name = "get_transaction"

    override val description = "Get full details of a specific HTTP network transaction by its ID. " +
        "Returns method, URL, status code, duration, protocol, TLS version, " +
        "and all request/response headers. Use after list_transactions to drill into a specific request."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("id") {
                put("type", "string")
                put("description", "Transaction ID from list_transactions")
            }
        }
        putJsonArray("required") { add(JsonPrimitive("id")) }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val id = arguments["id"]?.jsonPrimitive?.contentOrNull
            ?: return "Error: 'id' parameter is required and must be non-empty."
        if (id.isBlank()) return "Error: 'id' parameter is required and must be non-empty."

        val response = connection.apiClient.get("/api/transactions/$id")
        val data = response.jsonObject.objectOrNull("data") ?: return response.serverError() ?: "Transaction not found."
        return TextFormatter.formatTransactionDetail(data)
    }
}

internal class GetRequestBodyTool : McpTool() {

    override val name = "get_request_body"

    override val description = "Get the request body of a specific HTTP transaction. " +
        "Returns the raw body content along with content type, size, and truncation info. " +
        "Use to inspect POST/PUT/PATCH payloads, JSON request bodies, form data, etc."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("id") {
                put("type", "string")
                put("description", "Transaction ID from list_transactions")
            }
        }
        putJsonArray("required") { add(JsonPrimitive("id")) }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val id = arguments["id"]?.jsonPrimitive?.contentOrNull
            ?: return "Error: 'id' parameter is required and must be non-empty."
        if (id.isBlank()) return "Error: 'id' parameter is required and must be non-empty."

        val response = connection.apiClient.get("/api/transactions/$id/request-body")
        val data = response.jsonObject.objectOrNull("data") ?: return response.serverError() ?: "No request body found."
        return TextFormatter.formatBody(
            body = data["body"]?.jsonPrimitive?.contentOrNull ?: "",
            contentType = data["contentType"]?.jsonPrimitive?.contentOrNull,
            truncated = data["truncated"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: false,
            totalSize = data["totalSize"]?.jsonPrimitive?.longOrNull,
        )
    }
}

internal class GetResponseBodyTool : McpTool() {

    override val name = "get_response_body"

    override val description = "Get the response body of a specific HTTP transaction. " +
        "Returns the raw body content along with content type, size, and truncation info. " +
        "Use to inspect API response data, JSON payloads, HTML content, or error messages."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("id") {
                put("type", "string")
                put("description", "Transaction ID from list_transactions")
            }
        }
        putJsonArray("required") { add(JsonPrimitive("id")) }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val id = arguments["id"]?.jsonPrimitive?.contentOrNull
            ?: return "Error: 'id' parameter is required and must be non-empty."
        if (id.isBlank()) return "Error: 'id' parameter is required and must be non-empty."

        val response = connection.apiClient.get("/api/transactions/$id/response-body")
        val data = response.jsonObject.objectOrNull(
            "data",
        ) ?: return response.serverError() ?: "No response body found."
        return TextFormatter.formatBody(
            body = data["body"]?.jsonPrimitive?.contentOrNull ?: "",
            contentType = data["contentType"]?.jsonPrimitive?.contentOrNull,
            truncated = data["truncated"]?.jsonPrimitive?.contentOrNull?.toBooleanStrictOrNull() ?: false,
            totalSize = data["totalSize"]?.jsonPrimitive?.longOrNull,
        )
    }
}

internal class ListWebSocketConnectionsTool : McpTool() {

    override val name = "list_websocket_connections"

    override val description = "List all captured WebSocket connections from the running Android app. " +
        "Shows connection URL, status, and connection time. " +
        "Use to monitor real-time communication channels, debug WebSocket connectivity issues, " +
        "or find active connections to inspect their messages."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.get("/api/websockets/connections")
        val data = response.jsonObject.arrayOrNull(
            "data",
        ) ?: return response.serverError() ?: "No WebSocket connections found."
        return TextFormatter.formatGenericList(data, "WebSocket connection(s)")
    }
}

internal class ListWebSocketMessagesTool : McpTool() {

    override val name = "list_websocket_messages"

    override val description = "List messages for a specific WebSocket connection or all connections. " +
        "Shows message direction (sent/received), content, and timestamp. " +
        "Use to debug real-time communication, inspect message payloads, " +
        "or trace WebSocket conversation flows."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("connection_id") {
                put("type", "string")
                put(
                    "description",
                    "WebSocket connection ID to filter messages. Omit to list messages from all connections.",
                )
            }
            putJsonObject("limit") {
                put("type", "integer")
                put("description", "Maximum number of messages to return (default: 50)")
                put("default", 50)
            }
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val params = buildMap {
            arguments["connection_id"]?.jsonPrimitive?.contentOrNull?.let { put("connection_id", it) }
            arguments["limit"]?.jsonPrimitive?.intOrNull?.let { put("limit", it.toString()) }
        }

        val response = connection.apiClient.get("/api/websockets/messages", params)
        val data = response.jsonObject.arrayOrNull(
            "data",
        ) ?: return response.serverError() ?: "No WebSocket messages found."
        return TextFormatter.formatGenericList(data, "WebSocket message(s)")
    }
}

internal class SetRateLimitTool : McpTool() {

    override val name = "set_rate_limit"

    override val description = "Configure network rate limiting (throttling) on the Android app to simulate " +
        "slow network conditions. Pass a named preset, or custom values for download/upload speed, " +
        "latency, and packet loss (unset custom values keep their current setting). " +
        "Pass enabled=false to turn throttling off. " +
        "Use to test app behavior under poor network conditions, verify loading states, " +
        "timeout handling, and offline-first features."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("enabled") {
                put("type", "boolean")
                put("description", "Set to false to turn throttling off. Other parameters are ignored when false.")
            }
            putJsonObject("preset") {
                put("type", "string")
                putJsonArray("enum") { RATE_LIMIT_PRESETS.forEach { add(JsonPrimitive(it)) } }
                put(
                    "description",
                    "Network preset. OFFLINE drops all traffic. " +
                        "Overrides individual speed/latency parameters when set.",
                )
            }
            putJsonObject("download_kbps") {
                put("type", "integer")
                put("description", "Download speed limit in kilobits per second")
            }
            putJsonObject("upload_kbps") {
                put("type", "integer")
                put("description", "Upload speed limit in kilobits per second")
            }
            putJsonObject("latency_ms") {
                put("type", "integer")
                put("description", "Additional network latency in milliseconds")
            }
            putJsonObject("packet_loss") {
                put("type", "number")
                put("description", "Packet loss as a percentage, 0 to 100")
            }
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val body = buildJsonObject {
            arguments["enabled"]?.jsonPrimitive?.booleanOrNull?.let { put("enabled", it) }
            arguments["preset"]?.jsonPrimitive?.contentOrNull?.let { put("preset", it) }
            arguments["download_kbps"]?.jsonPrimitive?.longOrNull?.let { put("downloadSpeedKbps", it) }
            arguments["upload_kbps"]?.jsonPrimitive?.longOrNull?.let { put("uploadSpeedKbps", it) }
            arguments["latency_ms"]?.jsonPrimitive?.longOrNull?.let { put("latencyMs", it) }
            arguments["packet_loss"]?.jsonPrimitive?.floatOrNull?.let { put("packetLossPercent", it) }
        }

        val response = connection.apiClient.post("/api/rate-limit", body).jsonObject
        val error = response["error"]?.jsonPrimitive?.contentOrNull
        if (error != null) return "Error: $error"
        return "Rate limit configuration updated."
    }

    companion object {
        /** Mirrors `RateLimitConfig.NetworkPreset` on the device server. */
        internal val RATE_LIMIT_PRESETS = listOf(
            "WIFI",
            "GOOD_3G",
            "REGULAR_3G",
            "SLOW_3G",
            "GOOD_2G",
            "SLOW_2G",
            "EDGE",
            "OFFLINE",
        )
    }
}

internal class GetRateLimitTool : McpTool() {

    override val name = "get_rate_limit"

    override val description = "Get the current network rate limiting (throttling) configuration. " +
        "Returns the active preset name or custom speed/latency/packet-loss values. " +
        "Use to check what network conditions are currently being simulated."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.get("/api/rate-limit")
        val data = response.jsonObject.objectOrNull(
            "data",
        ) ?: return response.serverError() ?: "No rate limit configuration found."
        val sb = StringBuilder("Rate Limit Configuration:\n")
        data.forEach { (key, value) ->
            sb.appendLine("  $key: ${value.jsonPrimitive.contentOrNull ?: "N/A"}")
        }
        return sb.toString()
    }
}

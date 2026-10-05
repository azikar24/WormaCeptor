package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

internal class ClearTransactionsTool : McpTool() {

    override val name = "clear_transactions"

    override val description = "Delete all captured HTTP network transactions from the WormaCeptor buffer. " +
        "This is permanent: deleted transactions can't be recovered. " +
        "Use to reset the transaction list before reproducing a specific flow, " +
        "or to reduce noise when debugging a particular set of API calls."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.post("/api/clear/transactions")
        return response.jsonObject["message"]?.jsonPrimitive?.contentOrNull
            ?: "Transactions cleared."
    }
}

internal class ClearCrashesTool : McpTool() {

    override val name = "clear_crashes"

    override val description = "Delete all captured crash reports from the WormaCeptor buffer. " +
        "This is permanent: deleted crash reports and stack traces can't be recovered. " +
        "Use to clear historical crashes before testing a fix, " +
        "or to reset the crash list for a clean debugging session."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.post("/api/clear/crashes")
        return response.jsonObject["message"]?.jsonPrimitive?.contentOrNull
            ?: "Crashes cleared."
    }
}

internal class ClearLogsTool : McpTool() {

    override val name = "clear_logs"

    override val description = "Delete all captured log entries from the WormaCeptor buffer. " +
        "This is permanent: deleted log entries can't be recovered. " +
        "Use to clear historical logs before reproducing an issue, " +
        "or to get a clean log stream for targeted debugging."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.post("/api/clear/logs")
        return response.jsonObject["message"]?.jsonPrimitive?.contentOrNull
            ?: "Logs cleared."
    }
}

internal class SimulateLocationTool : McpTool() {

    override val name = "simulate_location"

    override val description = "Set a mock GPS location on the Android device for testing location-based features. " +
        "Requires latitude and longitude; optionally accepts altitude and a descriptive name. " +
        "Use to test geofencing, location-based UI, map features, or location-dependent business logic " +
        "without physically moving the device."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("latitude") {
                put("type", "number")
                put("description", "GPS latitude in decimal degrees (e.g., 37.7749 for San Francisco)")
            }
            putJsonObject("longitude") {
                put("type", "number")
                put("description", "GPS longitude in decimal degrees (e.g., -122.4194 for San Francisco)")
            }
            putJsonObject("altitude") {
                put("type", "number")
                put("description", "Altitude in meters above sea level (optional)")
            }
            putJsonObject("name") {
                put("type", "string")
                put("description", "Human-readable name for this location (optional, e.g., 'Golden Gate Bridge')")
            }
        }
        putJsonArray("required") {
            add(JsonPrimitive("latitude"))
            add(JsonPrimitive("longitude"))
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val latitude = arguments["latitude"]?.jsonPrimitive?.doubleOrNull
            ?: return "Error: 'latitude' parameter is required."
        val longitude = arguments["longitude"]?.jsonPrimitive?.doubleOrNull
            ?: return "Error: 'longitude' parameter is required."
        if (latitude < -90.0 || latitude > 90.0) {
            return "Error: 'latitude' must be between -90 and 90 (got $latitude)"
        }
        if (longitude < -180.0 || longitude > 180.0) {
            return "Error: 'longitude' must be between -180 and 180 (got $longitude)"
        }

        val body = buildJsonObject {
            put("latitude", latitude)
            put("longitude", longitude)
            arguments["altitude"]?.jsonPrimitive?.doubleOrNull?.let { put("altitude", it) }
            arguments["name"]?.jsonPrimitive?.contentOrNull?.let { put("name", it) }
        }

        val response = connection.apiClient.post("/api/location", body)
        return response.jsonObject["message"]?.jsonPrimitive?.contentOrNull
            ?: "Location set to $latitude, $longitude."
    }
}

internal class StopLocationSimulationTool : McpTool() {

    override val name = "stop_location_simulation"

    override val description = "Stop the current GPS location simulation and restore real device location. " +
        "Use after simulate_location when you're done testing location features " +
        "and want the device to use its actual GPS position again."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.delete("/api/location")
        return response.jsonObject["message"]?.jsonPrimitive?.contentOrNull
            ?: "Location simulation stopped."
    }
}

internal class SendPushNotificationTool : McpTool() {

    override val name = "send_push_notification"

    override val description = "Send a local push notification to the Android device for testing notification handling. " +
        "Creates a notification with the specified title and body text. " +
        "Optionally set a notification channel ID and priority level. " +
        "Use to test notification display, deep link handling from notifications, " +
        "notification actions, or verify notification channel configuration."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("title") {
                put("type", "string")
                put("description", "Notification title text")
            }
            putJsonObject("body") {
                put("type", "string")
                put("description", "Notification body/content text")
            }
            putJsonObject("channel_id") {
                put("type", "string")
                put(
                    "description",
                    "Android notification channel ID. Must match a channel registered by the app. " +
                        "Defaults to the app's default channel if omitted.",
                )
            }
            putJsonObject("priority") {
                put("type", "string")
                putJsonArray("enum") { listOf("low", "default", "high", "max").forEach { add(JsonPrimitive(it)) } }
                put("description", "Notification priority (default: 'default')")
            }
        }
        putJsonArray("required") {
            add(JsonPrimitive("title"))
            add(JsonPrimitive("body"))
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val title = arguments["title"]?.jsonPrimitive?.contentOrNull
            ?: return "Error: 'title' parameter is required and must be non-empty."
        if (title.isBlank()) return "Error: 'title' parameter is required and must be non-empty."
        val body = arguments["body"]?.jsonPrimitive?.contentOrNull
            ?: return "Error: 'body' parameter is required and must be non-empty."
        if (body.isBlank()) return "Error: 'body' parameter is required and must be non-empty."

        val requestBody = buildJsonObject {
            put("title", title)
            put("body", body)
            arguments["channel_id"]?.jsonPrimitive?.contentOrNull?.let { put("channelId", it) }
            arguments["priority"]?.jsonPrimitive?.contentOrNull?.let { put("priority", it) }
        }

        val response = connection.apiClient.post("/api/push", requestBody)
        return response.jsonObject["message"]?.jsonPrimitive?.contentOrNull
            ?: "Push notification sent: '$title'."
    }
}

package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.bridge.util.errorText
import com.azikar24.wormaceptor.mcp.bridge.util.toApiResponse
import com.azikar24.wormaceptor.mcp.bridge.util.toRequestBody
import com.azikar24.wormaceptor.mcp.protocol.MockLocationDto
import com.azikar24.wormaceptor.mcp.protocol.SimulatedNotificationDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

internal class ClearTransactionsTool : McpTool() {

    override val name = "clear_transactions"

    override val annotations = ToolAnnotations.Destructive

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
        return connection.apiClient.post(
            "/api/clear/transactions",
        ).toApiResponse().errorText() ?: "Transactions cleared."
    }
}

internal class ClearCrashesTool : McpTool() {

    override val name = "clear_crashes"

    override val annotations = ToolAnnotations.Destructive

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
        return connection.apiClient.post("/api/clear/crashes").toApiResponse().errorText() ?: "Crashes cleared."
    }
}

internal class ClearLogsTool : McpTool() {

    override val name = "clear_logs"

    override val annotations = ToolAnnotations.Destructive

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
        return connection.apiClient.post("/api/clear/logs").toApiResponse().errorText() ?: "Logs cleared."
    }
}

internal class SimulateLocationTool : McpTool() {

    override val name = "simulate_location"

    override val annotations = ToolAnnotations(readOnlyHint = false, destructiveHint = false, idempotentHint = true)

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

        val body = MockLocationDto(latitude = latitude, longitude = longitude).let {
            it.copy(
                altitude = arguments["altitude"]?.jsonPrimitive?.doubleOrNull ?: it.altitude,
                name = arguments["name"]?.jsonPrimitive?.contentOrNull,
            )
        }

        val response = connection.apiClient.post("/api/location", body.toRequestBody()).toApiResponse()
        return response.errorText() ?: "Location set to $latitude, $longitude."
    }
}

internal class StopLocationSimulationTool : McpTool() {

    override val name = "stop_location_simulation"

    override val annotations = ToolAnnotations(readOnlyHint = false, destructiveHint = false, idempotentHint = true)

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
        return connection.apiClient.delete(
            "/api/location",
        ).toApiResponse().errorText() ?: "Location simulation stopped."
    }
}

internal class BringAppToFrontTool : McpTool() {

    override val name = "bring_app_to_front"

    override val annotations = ToolAnnotations(readOnlyHint = false, destructiveHint = false, idempotentHint = true)

    override val description = "Launch the app, or bring it back to the foreground, through adb. " +
        "Android freezes or kills backgrounded apps, and then every other tool times out; " +
        "call this when a tool says the app is frozen or not running, then retry that tool."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("package") {
                put("type", "string")
                put(
                    "description",
                    "Application id to launch. Omit to use the app the bridge last connected to.",
                )
            }
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val pkg = arguments["package"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
            ?: connection.packageName
            ?: return "${ERROR_PREFIX}The app's package isn't known yet (the bridge hasn't reached the app). " +
                "Pass 'package'."
        val failure = connection.bringAppToFront(pkg) ?: return "Brought $pkg to the foreground."
        return "${ERROR_PREFIX}Couldn't launch $pkg: $failure"
    }
}

internal class SendPushNotificationTool : McpTool() {

    override val name = "send_push_notification"

    override val annotations = ToolAnnotations(readOnlyHint = false, destructiveHint = false, idempotentHint = false)

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

        val requestBody = SimulatedNotificationDto(title = title, body = body).let {
            it.copy(
                channelId = arguments["channel_id"]?.jsonPrimitive?.contentOrNull ?: it.channelId,
                priority = arguments["priority"]?.jsonPrimitive?.contentOrNull ?: it.priority,
            )
        }

        val response = connection.apiClient.post("/api/push", requestBody.toRequestBody()).toApiResponse()
        return response.errorText() ?: "Push notification sent: '$title'."
    }
}

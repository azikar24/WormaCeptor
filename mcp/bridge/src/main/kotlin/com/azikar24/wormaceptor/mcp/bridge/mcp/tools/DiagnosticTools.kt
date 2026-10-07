package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.adb.filterLogs
import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.bridge.util.TextFormatter
import com.azikar24.wormaceptor.mcp.bridge.util.dataAs
import com.azikar24.wormaceptor.mcp.bridge.util.errorText
import com.azikar24.wormaceptor.mcp.bridge.util.toApiResponse
import com.azikar24.wormaceptor.mcp.protocol.CrashDto
import com.azikar24.wormaceptor.mcp.protocol.CrashSummaryDto
import com.azikar24.wormaceptor.mcp.protocol.DeviceInfoDto
import com.azikar24.wormaceptor.mcp.protocol.LeakInfoDto
import com.azikar24.wormaceptor.mcp.protocol.LogEntryDto
import com.azikar24.wormaceptor.mcp.protocol.ThreadViolationDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

private const val DefaultCrashLimit = 20
private const val DefaultLogLimit = 30

internal class ListCrashesTool : McpTool() {

    override val name = "list_crashes"

    override val description = "List all captured crash reports from the running Android app. " +
        "Returns timestamp, exception type, and message for each crash. " +
        "Use to identify app stability issues, find recurring exceptions, " +
        "or get an overview of recent failures."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("limit") {
                put("type", "integer")
                put("description", "Maximum number of crashes to return (default: $DefaultCrashLimit)")
                put("default", DefaultCrashLimit)
            }
            putJsonObject("offset") {
                put("type", "integer")
                put("description", "Number of crashes to skip for pagination (default: 0)")
                put("default", 0)
            }
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val params = buildMap {
            put("limit", (arguments["limit"]?.jsonPrimitive?.intOrNull ?: DefaultCrashLimit).toString())
            arguments["offset"]?.jsonPrimitive?.intOrNull?.let { put("offset", it.toString()) }
        }
        val response = connection.apiClient.get("/api/crashes", params).toApiResponse()
        val data = response.dataAs<List<CrashSummaryDto>>() ?: return response.errorText() ?: "No crashes found."
        return TextFormatter.formatCrashList(data)
    }
}

internal class GetCrashTool : McpTool() {

    override val name = "get_crash"

    override val description = "Get full details of a specific crash by its ID, including the complete stack trace. " +
        "Returns timestamp, exception type, message, and the full Java/Kotlin stack trace. " +
        "Use after list_crashes to investigate root cause of a specific crash."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("id") {
                put("type", "string")
                put("description", "Crash ID from list_crashes")
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

        val response = connection.apiClient.get("/api/crashes/$id").toApiResponse()
        val data = response.dataAs<CrashDto>() ?: return response.errorText() ?: "Crash not found."
        return TextFormatter.formatCrashDetail(data)
    }
}

internal class TailLogsTool : McpTool() {

    override val name = "tail_logs"

    override val description = "Retrieve the newest log entries of the running Android app's process, " +
        "read from logcat over adb (full buffer history, no in-app capture needed). " +
        "Returns timestamp, level (VERBOSE/DEBUG/INFO/WARN/ERROR/ASSERT), tag, and message for each entry. " +
        "Use to monitor app behavior, trace execution flow, find warning/error messages, " +
        "or debug specific features by filtering on tag or log level."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("level") {
                put("type", "string")
                putJsonArray("enum") { LOG_LEVELS.forEach { add(JsonPrimitive(it)) } }
                put(
                    "description",
                    "Return only entries at exactly this level (not this level and above). " +
                        "Omit to return all levels.",
                )
            }
            putJsonObject("tag") {
                put("type", "string")
                put("description", "Filter logs whose tag contains this text (case-insensitive). Example: 'OkHttp'.")
            }
            putJsonObject("limit") {
                put("type", "integer")
                put("description", "Maximum number of newest log entries to return (default: $DefaultLogLimit)")
                put("default", DefaultLogLimit)
            }
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val level = arguments["level"]?.jsonPrimitive?.contentOrNull
        val tag = arguments["tag"]?.jsonPrimitive?.contentOrNull
        val limit = arguments["limit"]?.jsonPrimitive?.intOrNull ?: DefaultLogLimit

        connection.readAppLogs()?.let { logs ->
            val entries = filterLogs(logs, level, tag, limit)
            return if (entries.isEmpty()) "No log entries found." else TextFormatter.formatLogEntries(entries)
        }

        // adb couldn't read logcat: fall back to the in-app capture, which only runs while started from the app.
        val params = buildMap {
            level?.let { put("level", it) }
            tag?.let { put("tag", it) }
            put("limit", limit.toString())
        }
        val response = connection.apiClient.get("/api/logs", params).toApiResponse()
        val data = response.dataAs<List<LogEntryDto>>() ?: return response.errorText() ?: "No log entries found."
        return TextFormatter.formatLogEntries(data)
    }

    companion object {
        /** Mirrors `LogLevel` on the device server. */
        internal val LOG_LEVELS = listOf("VERBOSE", "DEBUG", "INFO", "WARN", "ERROR", "ASSERT")
    }
}

internal class ListLeaksTool : McpTool() {

    override val name = "list_leaks"

    override val description = "List detected memory leaks from the running Android app (via LeakCanary integration). " +
        "Returns leak trace summaries including the leaking object, retained size, and reference chain. " +
        "Use to identify memory leaks that cause OutOfMemoryError crashes or degraded performance."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.get("/api/leaks").toApiResponse()
        val data = response.dataAs<List<LeakInfoDto>>() ?: return response.errorText() ?: "No leaks detected."
        return TextFormatter.formatGenericList(data, LeakInfoDto.serializer(), "memory leak(s)")
    }
}

internal class ListViolationsTool : McpTool() {

    override val name = "list_violations"

    override val description = "List StrictMode and other thread/policy violations from the running Android app. " +
        "Returns violation type, message, and stack trace location. " +
        "Use to find disk reads on the main thread, network calls on the UI thread, " +
        "resource misuse, or other Android best-practice violations."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.get("/api/violations").toApiResponse()
        val data = response.dataAs<List<ThreadViolationDto>>()
            ?: return response.errorText() ?: "No violations detected."
        return TextFormatter.formatGenericList(data, ThreadViolationDto.serializer(), "violation(s)")
    }
}

internal class GetDeviceInfoTool : McpTool() {

    override val name = "get_device_info"

    override val description = "Get comprehensive information about the connected Android device and the running app. " +
        "Returns device model, manufacturer, Android version, SDK level, screen density, " +
        "app package name, version, build type, and other system properties. " +
        "Use to understand the test environment or include device context in bug reports."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.get("/api/device-info").toApiResponse()
        val data = response.dataAs<DeviceInfoDto>() ?: return response.errorText() ?: "Device info unavailable."
        return TextFormatter.formatDeviceInfo(data)
    }
}

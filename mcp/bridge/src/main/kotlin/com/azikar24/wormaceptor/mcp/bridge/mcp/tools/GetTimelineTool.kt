package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.bridge.util.TextFormatter
import com.azikar24.wormaceptor.mcp.bridge.util.dataAs
import com.azikar24.wormaceptor.mcp.bridge.util.errorText
import com.azikar24.wormaceptor.mcp.bridge.util.toApiResponse
import com.azikar24.wormaceptor.mcp.protocol.TimelineDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

private const val DefaultTimelineLimit = 50

internal class GetTimelineTool : McpTool() {

    override val name = "get_timeline"

    override val description = "Get one time-ordered list (oldest first, newest last) of everything the app " +
        "recorded in a window: HTTP transactions (method, URL, status, duration), crashes, log entries, " +
        "StrictMode violations, memory leaks, and WebSocket messages. Defaults to the last 5 minutes. " +
        "Use to answer 'what happened when I tapped X': call it right after reproducing the flow. " +
        "Logs are included only while log capture is running; the output says when they were left out. " +
        "Each event carries the id that get_transaction / get_crash accept."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("since_ms") {
                put("type", "integer")
                put(
                    "description",
                    "Window start, epoch milliseconds on the device clock (default: until_ms minus 5 minutes). " +
                        "Timestamps from other tools use the same clock.",
                )
            }
            putJsonObject("until_ms") {
                put("type", "integer")
                put("description", "Window end, epoch milliseconds on the device clock (default: now)")
            }
            putJsonObject("limit") {
                put("type", "integer")
                put("description", "Maximum events to return, newest kept (default: $DefaultTimelineLimit, max 500)")
                put("default", DefaultTimelineLimit)
            }
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val params = buildMap {
            arguments["since_ms"]?.jsonPrimitive?.longOrNull?.let { put("since_ms", it.toString()) }
            arguments["until_ms"]?.jsonPrimitive?.longOrNull?.let { put("until_ms", it.toString()) }
            arguments["limit"]?.jsonPrimitive?.intOrNull?.let { put("limit", it.toString()) }
        }
        val response = connection.apiClient.get("/api/timeline", params).toApiResponse()
        val data = response.dataAs<TimelineDto>() ?: return response.errorText() ?: "Timeline is empty."
        return TextFormatter.formatTimeline(data)
    }
}

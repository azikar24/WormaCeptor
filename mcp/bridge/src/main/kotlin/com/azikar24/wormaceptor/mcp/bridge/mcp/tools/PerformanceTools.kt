package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.bridge.util.TextFormatter
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

internal class GetCpuStatsTool : McpTool() {

    override val name = "get_cpu_stats"

    override val description = "Get current CPU usage statistics from the running Android app. " +
        "Returns overall CPU usage percentage, core count, frequency, and temperature. " +
        "Optionally includes historical usage data for trend analysis. " +
        "Use to diagnose high CPU consumption, detect runaway threads, " +
        "or profile CPU-intensive operations."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("include_history") {
                put("type", "boolean")
                put(
                    "description",
                    "Include historical CPU usage data points (default: false). " +
                        "When true, returns a time-series of usage samples.",
                )
            }
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val params = buildMap {
            arguments["include_history"]?.jsonPrimitive?.contentOrNull?.let { put("include_history", it) }
        }

        val response = connection.apiClient.get("/api/cpu", params)
        val data = response.jsonObject.objectOrNull("data") ?: return "CPU stats unavailable."

        val sb = StringBuilder()
        sb.appendLine("CPU: ${data.str("overallUsagePercent")}% (${data.str("coreCount")} cores)")
        data.strOrNull("cpuFrequencyMHz")?.let { sb.appendLine("Frequency: $it MHz") }
        data.strOrNull("cpuTemperature")?.let { sb.appendLine("Temperature: $it\u00B0C") }

        data["history"]?.jsonArray?.let { history ->
            sb.appendLine("\nUsage History (${history.size} samples):")
            history.forEach { sample ->
                val s = sample.jsonObject
                sb.appendLine("  ${s.str("timestamp")}: ${s.str("usagePercent")}%")
            }
        }

        return sb.toString()
    }

    private fun JsonObject.str(key: String): String = this[key]?.jsonPrimitive?.contentOrNull ?: "N/A"

    private fun JsonObject.strOrNull(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull
}

internal class GetMemoryStatsTool : McpTool() {

    override val name = "get_memory_stats"

    override val description = "Get current memory usage statistics from the running Android app. " +
        "Returns total memory, used memory, heap usage percentage, native heap allocation, " +
        "and garbage collection info. " +
        "Use to diagnose memory pressure, detect memory leaks in progress, " +
        "or verify memory optimization changes."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.get("/api/memory")
        val data = response.jsonObject.objectOrNull("data") ?: return "Memory stats unavailable."

        val sb = StringBuilder()
        sb.appendLine("Memory: ${data.str("usedMemory")} / ${data.str("totalMemory")} bytes")
        sb.appendLine("Heap Usage: ${data.str("heapUsagePercent")}%")
        sb.appendLine("Native Heap: ${data.str("nativeHeapAllocated")} bytes allocated")
        data.strOrNull("gcCount")?.let { sb.appendLine("GC Count: $it") }
        data.strOrNull("gcTime")?.let { sb.appendLine("GC Time: ${it}ms") }
        return sb.toString()
    }

    private fun JsonObject.str(key: String): String = this[key]?.jsonPrimitive?.contentOrNull ?: "N/A"

    private fun JsonObject.strOrNull(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull
}

internal class GetFpsStatsTool : McpTool() {

    override val name = "get_fps_stats"

    override val description = "Get current frame rate (FPS) statistics from the running Android app. " +
        "Returns current FPS, average FPS, min/max FPS, dropped frame count, and jank frame count. " +
        "Use to diagnose UI performance issues, detect jank, verify smooth scrolling, " +
        "or measure the impact of UI changes on rendering performance."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.get("/api/fps")
        val data = response.jsonObject.objectOrNull("data") ?: return "FPS stats unavailable."

        val sb = StringBuilder()
        sb.appendLine(
            "FPS: ${data.str("currentFps")} (avg: ${data.str("averageFps")}, " +
                "min: ${data.str("minFps")}, max: ${data.str("maxFps")})",
        )
        sb.appendLine("Dropped Frames: ${data.str("droppedFrames")}")
        sb.appendLine("Jank Frames: ${data.str("jankFrames")}")
        return sb.toString()
    }

    private fun JsonObject.str(key: String): String = this[key]?.jsonPrimitive?.contentOrNull ?: "N/A"
}

internal class GetPerformanceSnapshotTool : McpTool() {

    override val name = "get_performance_snapshot"

    override val description = "Get a combined performance snapshot including CPU, memory, and FPS data in one call. " +
        "Returns an aggregate view of all performance metrics. " +
        "Use as a quick health check of app performance, " +
        "or when you need a holistic view rather than drilling into individual metrics."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {}
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val response = connection.apiClient.get("/api/performance")
        val data = response.jsonObject.objectOrNull("data") ?: return "Performance data unavailable."
        return TextFormatter.formatPerformanceSnapshot(data)
    }
}

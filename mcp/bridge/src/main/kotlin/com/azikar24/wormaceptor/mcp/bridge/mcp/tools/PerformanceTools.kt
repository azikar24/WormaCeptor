package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.bridge.util.TextFormatter
import com.azikar24.wormaceptor.mcp.bridge.util.dataAs
import com.azikar24.wormaceptor.mcp.bridge.util.errorText
import com.azikar24.wormaceptor.mcp.bridge.util.toApiResponse
import com.azikar24.wormaceptor.mcp.protocol.CpuHistoryDto
import com.azikar24.wormaceptor.mcp.protocol.CpuInfoDto
import com.azikar24.wormaceptor.mcp.protocol.FpsHistoryDto
import com.azikar24.wormaceptor.mcp.protocol.FpsInfoDto
import com.azikar24.wormaceptor.mcp.protocol.MemoryHistoryDto
import com.azikar24.wormaceptor.mcp.protocol.MemoryInfoDto
import com.azikar24.wormaceptor.mcp.protocol.PerformanceSnapshotDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
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

        val response = connection.apiClient.get("/api/cpu", params).toApiResponse()
        // With include_history the server wraps the reading as {current, history, isMonitoring}.
        val stats = if (params[HistoryParam] == "true") {
            response.dataAs<CpuHistoryDto>()?.let { it.current to it.history }
        } else {
            response.dataAs<CpuInfoDto>()?.let { it to null }
        }
        val (data, history) = stats ?: return response.errorText() ?: "CPU stats unavailable."

        val sb = StringBuilder()
        sb.appendLine("CPU: ${data.overallUsagePercent}% (${data.coreCount} cores)")
        sb.appendLine("Frequency: ${data.cpuFrequencyMHz} MHz")
        data.cpuTemperature?.let { sb.appendLine("Temperature: $it\u00B0C") }

        history?.let {
            sb.appendLine("\nUsage History (${it.size} samples):")
            it.forEach { s -> sb.appendLine("  ${s.timestamp}: ${s.overallUsagePercent}%") }
        }

        return sb.toString()
    }
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
        putJsonObject("properties") {
            putJsonObject("include_history") {
                put("type", "boolean")
                put("description", "Include recent memory samples (default: false)")
            }
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val params = historyParam(arguments)
        val response = connection.apiClient.get("/api/memory", params).toApiResponse()
        val stats = if (params[HistoryParam] == "true") {
            response.dataAs<MemoryHistoryDto>()?.let { it.current to it.history }
        } else {
            response.dataAs<MemoryInfoDto>()?.let { it to null }
        }
        val (data, history) = stats ?: return response.errorText() ?: "Memory stats unavailable."

        val sb = StringBuilder()
        sb.appendLine("Memory: ${data.usedMemory} / ${data.totalMemory} bytes")
        sb.appendLine("Heap Usage: ${data.heapUsagePercent}%")
        sb.appendLine("Native Heap: ${data.nativeHeapAllocated} bytes allocated")
        sb.appendLine("GC Count: ${data.gcCount}")
        history?.let {
            sb.appendLine("\nHistory (${it.size} samples):")
            it.forEach { s -> sb.appendLine("  ${s.timestamp}: ${s.usedMemory} bytes (${s.heapUsagePercent}%)") }
        }
        return sb.toString()
    }
}

internal class GetFpsStatsTool : McpTool() {

    override val name = "get_fps_stats"

    override val description = "Get current frame rate (FPS) statistics from the running Android app. " +
        "Returns current FPS, average FPS, min/max FPS, dropped frame count, and jank frame count. " +
        "Use to diagnose UI performance issues, detect jank, verify smooth scrolling, " +
        "or measure the impact of UI changes on rendering performance."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("include_history") {
                put("type", "boolean")
                put("description", "Include recent FPS samples (default: false)")
            }
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val params = historyParam(arguments)
        val response = connection.apiClient.get("/api/fps", params).toApiResponse()
        val stats = if (params[HistoryParam] == "true") {
            response.dataAs<FpsHistoryDto>()?.let { it.current to it.history }
        } else {
            response.dataAs<FpsInfoDto>()?.let { it to null }
        }
        val (data, history) = stats ?: return response.errorText() ?: "FPS stats unavailable."

        val sb = StringBuilder()
        sb.appendLine(
            "FPS: ${data.currentFps} (avg: ${data.averageFps}, " +
                "min: ${data.minFps}, max: ${data.maxFps})",
        )
        sb.appendLine("Dropped Frames: ${data.droppedFrames}")
        sb.appendLine("Jank Frames: ${data.jankFrames}")
        history?.let {
            sb.appendLine("\nHistory (${it.size} samples):")
            it.forEach { s -> sb.appendLine("  ${s.timestamp}: ${s.currentFps} fps") }
        }
        return sb.toString()
    }
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
        val response = connection.apiClient.get("/api/performance").toApiResponse()
        val data = response.dataAs<PerformanceSnapshotDto>()
            ?: return response.errorText() ?: "Performance data unavailable."
        return TextFormatter.formatPerformanceSnapshot(data)
    }
}

private const val HistoryParam = "include_history"

private fun historyParam(arguments: JsonObject): Map<String, String> = buildMap {
    arguments[HistoryParam]?.jsonPrimitive?.booleanOrNull?.let { put(HistoryParam, it.toString()) }
}

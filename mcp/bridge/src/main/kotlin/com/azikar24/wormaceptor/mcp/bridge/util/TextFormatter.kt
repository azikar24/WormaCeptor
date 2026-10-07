package com.azikar24.wormaceptor.mcp.bridge.util

import com.azikar24.wormaceptor.mcp.protocol.CrashDto
import com.azikar24.wormaceptor.mcp.protocol.CrashSummaryDto
import com.azikar24.wormaceptor.mcp.protocol.DatabaseInfoDto
import com.azikar24.wormaceptor.mcp.protocol.DeviceInfoDto
import com.azikar24.wormaceptor.mcp.protocol.FileEntryDto
import com.azikar24.wormaceptor.mcp.protocol.LogEntryDto
import com.azikar24.wormaceptor.mcp.protocol.PerformanceSnapshotDto
import com.azikar24.wormaceptor.mcp.protocol.PreferenceFileDto
import com.azikar24.wormaceptor.mcp.protocol.QueryResultDto
import com.azikar24.wormaceptor.mcp.protocol.RateLimitConfigDto
import com.azikar24.wormaceptor.mcp.protocol.TransactionDetailDto
import com.azikar24.wormaceptor.mcp.protocol.TransactionSummaryDto
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

internal object TextFormatter {

    private const val NotAvailable = "N/A"

    /** For the generic formatters: every DTO field is printed, defaults included. */
    private val displayJson = Json(ProtocolJson) { encodeDefaults = true }

    fun formatTransactionList(transactions: List<TransactionSummaryDto>): String {
        if (transactions.isEmpty()) return "No transactions captured."
        val sb = StringBuilder()
        sb.appendLine("Found ${transactions.size} transaction(s):\n")
        sb.appendLine("| # | ID | Method | URL | Status | Duration | Time |")
        sb.appendLine("|---|----|--------|-----|--------|----------|------|")
        transactions.forEachIndexed { i, tx ->
            sb.appendLine(
                "| ${i + 1} | ${tx.id} | ${tx.method} | ${tx.url} " +
                    "| ${tx.code ?: NotAvailable} | ${tx.tookMs ?: NotAvailable}ms | ${tx.timestamp} |",
            )
        }
        return sb.toString()
    }

    fun formatTransactionDetail(tx: TransactionDetailDto): String {
        val sb = StringBuilder()
        val request = tx.request
        val response = tx.response
        sb.appendLine("Transaction: ${tx.id}")
        sb.appendLine("Method: ${request.method}")
        sb.appendLine("URL: ${request.url}")
        sb.appendLine("Status: ${response?.let { "${it.code} ${it.message}" } ?: tx.status}")
        sb.appendLine("Duration: ${tx.durationMs ?: NotAvailable}ms")
        response?.protocol?.let { sb.appendLine("Protocol: $it") }
        response?.tlsVersion?.let { sb.appendLine("TLS: $it") }
        response?.error?.let { sb.appendLine("Error: $it") }
        sb.appendLine()

        sb.appendLine("Request Headers:")
        request.headers.forEach { (k, v) -> sb.appendLine("  $k: ${v.joinToString(", ")}") }
        sb.appendLine()
        response?.headers?.let { headers ->
            sb.appendLine("Response Headers:")
            headers.forEach { (k, v) -> sb.appendLine("  $k: ${v.joinToString(", ")}") }
        }
        return sb.toString()
    }

    fun formatBody(
        body: String,
        contentType: String?,
        truncated: Boolean,
        totalSize: Long?,
    ): String {
        val sb = StringBuilder()
        if (contentType != null) sb.appendLine("Content-Type: $contentType")
        if (totalSize != null) sb.appendLine("Size: $totalSize characters")
        if (truncated) sb.appendLine("[TRUNCATED]")
        sb.appendLine()
        sb.append(body)
        return sb.toString()
    }

    fun formatCrashList(crashes: List<CrashSummaryDto>): String {
        if (crashes.isEmpty()) return "No crashes recorded."
        val sb = StringBuilder()
        sb.appendLine("Found ${crashes.size} crash(es):\n")
        crashes.forEachIndexed { i, c ->
            sb.appendLine("${i + 1}. [id=${c.id}] ${c.exceptionType}: ${c.message ?: NotAvailable}")
        }
        return sb.toString()
    }

    fun formatCrashDetail(crash: CrashDto): String {
        val sb = StringBuilder()
        sb.appendLine("Crash: ${crash.id}")
        sb.appendLine("Time: ${crash.timestamp}")
        sb.appendLine("Exception: ${crash.exceptionType}")
        sb.appendLine("Message: ${crash.message ?: NotAvailable}")
        sb.appendLine()
        sb.appendLine("Stack Trace:")
        sb.appendLine(crash.stackTrace)
        return sb.toString()
    }

    fun formatLogEntries(logs: List<LogEntryDto>): String {
        if (logs.isEmpty()) {
            return "No log entries found. Capture starts with the first tail_logs call and records entries " +
                "logged after that; trigger the flow again, then retry."
        }
        return logs.joinToString("\n") { "${it.timestamp} ${it.level}/${it.tag}: ${it.message}" }
    }

    fun formatPerformanceSnapshot(data: PerformanceSnapshotDto): String {
        val sb = StringBuilder()
        val cpu = data.cpu
        sb.appendLine("CPU: ${cpu.overallUsagePercent}% (${cpu.coreCount} cores)")
        sb.appendLine("  Frequency: ${cpu.cpuFrequencyMHz} MHz")
        cpu.cpuTemperature?.let { sb.appendLine("  Temperature: $it°C") }
        sb.appendLine()
        val mem = data.memory
        sb.appendLine("Memory: ${mem.usedMemory} / ${mem.totalMemory} bytes")
        sb.appendLine("  Heap: ${mem.heapUsagePercent}%")
        sb.appendLine("  Native: ${mem.nativeHeapAllocated} bytes allocated")
        sb.appendLine()
        if (!data.fpsMonitoring) {
            sb.appendLine("FPS: monitoring is off (open the FPS tool or performance overlay to sample)")
        } else {
            val fps = data.fps
            sb.appendLine(
                "FPS: ${fps.currentFps} (avg: ${fps.averageFps}, min: ${fps.minFps}, max: ${fps.maxFps})",
            )
            sb.appendLine("  Dropped: ${fps.droppedFrames} | Jank: ${fps.jankFrames}")
        }
        return sb.toString()
    }

    fun formatDatabaseList(databases: List<DatabaseInfoDto>): String {
        if (databases.isEmpty()) return "No databases found."
        val sb = StringBuilder()
        sb.appendLine("Found ${databases.size} database(s):\n")
        databases.forEachIndexed { i, db ->
            sb.appendLine("${i + 1}. ${db.name} (${db.sizeBytes} bytes, tables: ${db.tableCount})")
        }
        return sb.toString()
    }

    fun formatQueryResult(result: QueryResultDto): String {
        result.error?.let { return "Error: $it" }
        val rows = result.rows
        if (rows.isEmpty()) return "Query returned 0 rows."

        val columns = result.columns
        val sb = StringBuilder()
        sb.appendLine("| ${columns.joinToString(" | ")} |")
        sb.appendLine("| ${columns.joinToString(" | ") { "---" }} |")
        rows.forEach { row ->
            sb.appendLine("| ${row.joinToString(" | ") { it ?: "null" }} |")
        }
        sb.appendLine("\n${rows.size} row(s) returned.")
        return sb.toString()
    }

    fun formatPreferences(prefs: List<PreferenceFileDto>): String {
        if (prefs.isEmpty()) return "No preferences found."
        val sb = StringBuilder()
        prefs.forEach { file ->
            sb.appendLine("File: ${file.name}")
            file.entries.forEach { e -> sb.appendLine("  ${e.key} = ${e.value} (${e.type})") }
            sb.appendLine()
        }
        return sb.toString()
    }

    fun formatDeviceInfo(info: DeviceInfoDto): String {
        val sb = StringBuilder()
        sb.appendLine("Device Information:")
        displayJson.encodeToJsonElement(DeviceInfoDto.serializer(), info).jsonObject.forEach { (key, value) ->
            val displayKey = key.replace(Regex("([A-Z])"), " $1").trim().replaceFirstChar { it.uppercase() }
            sb.appendLine("  $displayKey: ${value.content()}")
        }
        return sb.toString()
    }

    fun formatRateLimit(config: RateLimitConfigDto): String {
        val sb = StringBuilder("Rate Limit Configuration:\n")
        displayJson.encodeToJsonElement(RateLimitConfigDto.serializer(), config).jsonObject.forEach { (key, value) ->
            sb.appendLine("  $key: ${(value as JsonPrimitive).contentOrNull ?: NotAvailable}")
        }
        return sb.toString()
    }

    fun formatFileList(files: List<FileEntryDto>): String {
        if (files.isEmpty()) return "No files found."
        val sb = StringBuilder()
        files.forEach { f ->
            val type = if (f.isDirectory) "DIR " else "FILE"
            val size = if (type == "FILE") "  (${f.sizeBytes} bytes)" else ""
            sb.appendLine("[$type] ${f.name}$size  ${f.path}")
        }
        return sb.toString()
    }

    fun <T> formatGenericList(
        items: List<T>,
        serializer: KSerializer<T>,
        label: String,
    ): String {
        if (items.isEmpty()) return "No $label found."
        val sb = StringBuilder()
        sb.appendLine("Found ${items.size} $label:\n")
        items.forEachIndexed { i, item ->
            val o = displayJson.encodeToJsonElement(serializer, item).jsonObject
            val fields = o.entries.joinToString(", ") { "${it.key}: ${it.value.content()}" }
            sb.appendLine("${i + 1}. $fields")
        }
        return sb.toString()
    }

    private fun JsonElement.content(): String = when (this) {
        is JsonPrimitive -> contentOrNull ?: "null"
        is JsonArray -> joinToString(", ") { it.content() }
        is JsonObject -> toString()
    }
}

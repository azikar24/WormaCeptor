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
import kotlinx.serialization.json.longOrNull
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

internal object TextFormatter {

    /** Claude Code rejects tool results much past this; a real `tail_logs` call returned 142,529 characters. */
    const val MAX_RESULT_CHARS = 20_000

    /** Single log messages and field values are clipped to this many characters. */
    const val MAX_VALUE_CHARS = 500

    private const val NotAvailable = "N/A"
    private const val FooterReserve = 100
    private const val PageHint = "use limit/offset to page"
    private const val MsPerSecond = 1_000L
    private const val SecondsPerMinute = 60L
    private const val SecondsPerHour = 3_600L
    private const val SecondsPerDay = 86_400L

    /** Time-named values below this (2001-01-01) aren't epoch millis, so they print raw. */
    private const val MinEpochMs = 978_307_200_000L
    private val TimeField = Regex("^(timestamp|lastModified|.+At|.+Time)$")
    private val IsoSeconds: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    /** For the generic formatters: every DTO field is printed, defaults included. */
    private val displayJson = Json(ProtocolJson) { encodeDefaults = true }

    fun formatTransactionList(transactions: List<TransactionSummaryDto>): String {
        if (transactions.isEmpty()) return "No transactions captured."
        val header = "Found ${transactions.size} transaction(s):\n\n" +
            "| # | ID | Method | URL | Status | Duration | Time |\n" +
            "|---|----|--------|-----|--------|----------|------|\n"
        val rows = transactions.mapIndexed { i, tx ->
            "| ${i + 1} | ${tx.id} | ${tx.method} | ${tx.url.clip()} " +
                "| ${tx.code ?: NotAvailable} | ${tx.tookMs ?: NotAvailable}ms | ${formatTime(tx.timestamp)} |"
        }
        return joinRows(header, rows)
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
        val rows = crashes.mapIndexed { i, c ->
            "${i + 1}. [id=${c.id}] ${c.exceptionType}: ${c.message?.clip() ?: NotAvailable}"
        }
        return joinRows("Found ${crashes.size} crash(es):\n\n", rows)
    }

    fun formatCrashDetail(crash: CrashDto): String {
        val sb = StringBuilder()
        sb.appendLine("Crash: ${crash.id}")
        sb.appendLine("Time: ${formatTime(crash.timestamp)}")
        sb.appendLine("Exception: ${crash.exceptionType}")
        sb.appendLine("Message: ${crash.message ?: NotAvailable}")
        sb.appendLine()
        sb.appendLine("Stack Trace:")
        sb.appendLine(crash.stackTrace)
        return sb.toString()
    }

    fun formatLogEntries(logs: List<LogEntryDto>): String {
        if (logs.isEmpty()) {
            return "No log entries found. adb couldn't read logcat, and WormaCeptor's in-app capture only " +
                "records while it runs (open the Logs screen in the app), then retry."
        }
        val rows = logs.map { "${formatTime(it.timestamp)} ${it.level}/${it.tag}: ${it.message.clip()}" }
        return joinRows("", rows, keepNewest = true, hint = "lower limit or filter by level/tag")
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
            sb.appendLine("FPS: monitoring is off (start it with set_monitoring target=fps enabled=true)")
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
        val header = "| ${columns.joinToString(" | ")} |\n| ${columns.joinToString(" | ") { "---" }} |\n"
        val lines = rows.map { row -> "| ${row.joinToString(" | ") { it?.clip() ?: "null" }} |" }
        return joinRows(header, lines, hint = "add LIMIT/OFFSET to the query") + "\n${rows.size} row(s) returned.\n"
    }

    fun formatPreferences(prefs: List<PreferenceFileDto>): String {
        if (prefs.isEmpty()) return "No preferences found."
        val rows = prefs.flatMap { file ->
            listOf("File: ${file.name}") + file.entries.map { e -> "  ${e.key} = ${e.value.clip()} (${e.type})" } + ""
        }
        return joinRows("", rows, hint = "query a single file with read_file")
    }

    fun formatDeviceInfo(info: DeviceInfoDto): String {
        val sb = StringBuilder()
        sb.appendLine("Device Information:")
        displayJson.encodeToJsonElement(DeviceInfoDto.serializer(), info).jsonObject.forEach { (key, value) ->
            val displayKey = key.replace(Regex("([A-Z])"), " $1").trim().replaceFirstChar { it.uppercase() }
            sb.appendLine("  $displayKey: ${fieldValue(key, value)}")
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
        val rows = files.map { f ->
            val type = if (f.isDirectory) "DIR " else "FILE"
            val size = if (type == "FILE") "  (${f.sizeBytes} bytes)" else ""
            "[$type] ${f.name}$size  ${f.path}"
        }
        return joinRows("", rows, hint = "list a subdirectory")
    }

    fun <T> formatGenericList(
        items: List<T>,
        serializer: KSerializer<T>,
        label: String,
    ): String {
        if (items.isEmpty()) return "No $label found."
        val rows = items.mapIndexed { i, item ->
            val o = displayJson.encodeToJsonElement(serializer, item).jsonObject
            val fields = o.entries.joinToString(", ") { "${it.key}: ${fieldValue(it.key, it.value)}" }
            "${i + 1}. $fields"
        }
        return joinRows("Found ${items.size} $label:\n\n", rows)
    }

    /** Local ISO-8601 to the second plus age, e.g. `2026-10-07T18:05:12 (12 s ago)`. */
    fun formatTime(
        epochMs: Long,
        nowMs: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): String {
        val local = IsoSeconds.format(Instant.ofEpochMilli(epochMs).atZone(zone))
        val seconds = (nowMs - epochMs) / MsPerSecond
        val span = abs(seconds)
        val amount = when {
            span < SecondsPerMinute -> "$span s"
            span < SecondsPerHour -> "${span / SecondsPerMinute} min"
            span < SecondsPerDay -> "${span / SecondsPerHour} h"
            else -> "${span / SecondsPerDay} d"
        }
        return if (seconds >= 0) "$local ($amount ago)" else "$local (in $amount)"
    }

    /** Time-named fields holding epoch millis render via [formatTime]; everything else is clipped text. */
    private fun fieldValue(
        key: String,
        value: JsonElement,
    ): String {
        val epochMs = (value as? JsonPrimitive)?.longOrNull?.takeIf { it >= MinEpochMs && TimeField.matches(key) }
        return epochMs?.let { formatTime(it) } ?: value.content().clip()
    }

    /** Last line of defence for any tool result, bodies included. */
    fun capResult(text: String): String {
        if (text.length <= MAX_RESULT_CHARS) return text
        return text.take(MAX_RESULT_CHARS) + "\n… truncated, ${text.length - MAX_RESULT_CHARS} more characters"
    }

    private fun String.clip(): String =
        if (length <= MAX_VALUE_CHARS) this else take(MAX_VALUE_CHARS) + "… (+${length - MAX_VALUE_CHARS} chars)"

    /**
     * [header] plus as many [rows] as fit in [MAX_RESULT_CHARS], each on its own line, then a footer
     * naming how many were dropped. [keepNewest] keeps the last rows (logs are oldest first).
     */
    private fun joinRows(
        header: String,
        rows: List<String>,
        keepNewest: Boolean = false,
        hint: String = PageHint,
    ): String {
        val budget = MAX_RESULT_CHARS - header.length - FooterReserve
        val ordered = if (keepNewest) rows.asReversed() else rows
        var used = 0
        var fit = 0
        for (row in ordered) {
            used += row.length + 1
            if (used > budget) break
            fit++
        }
        val kept = if (keepNewest) rows.takeLast(fit) else rows.take(fit)
        return buildString {
            append(header)
            kept.forEach { appendLine(it) }
            if (fit < rows.size) appendLine("… truncated, ${rows.size - fit} more entries; $hint")
        }
    }

    private fun JsonElement.content(): String = when (this) {
        is JsonPrimitive -> contentOrNull ?: "null"
        is JsonArray -> joinToString(", ") { it.content() }
        is JsonObject -> toString()
    }
}

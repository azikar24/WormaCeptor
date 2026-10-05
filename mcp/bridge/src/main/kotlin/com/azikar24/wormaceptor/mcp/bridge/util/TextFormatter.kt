package com.azikar24.wormaceptor.mcp.bridge.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal object TextFormatter {

    fun formatTransactionList(transactions: JsonArray): String {
        if (transactions.isEmpty()) return "No transactions captured."
        val sb = StringBuilder()
        sb.appendLine("Found ${transactions.size} transaction(s):\n")
        sb.appendLine("| # | Method | URL | Status | Duration | Time |")
        sb.appendLine("|---|--------|-----|--------|----------|------|")
        transactions.forEachIndexed { i, tx ->
            val o = tx.jsonObject
            sb.appendLine(
                "| ${i + 1} | ${o.str("method")} | ${o.str("url")} " +
                    "| ${o.str("statusCode")} | ${o.str("durationMs")}ms | ${o.str("timestamp")} |",
            )
        }
        return sb.toString()
    }

    fun formatTransactionDetail(tx: JsonObject): String {
        val sb = StringBuilder()
        sb.appendLine("Transaction: ${tx.str("id")}")
        sb.appendLine("Method: ${tx.str("method")}")
        sb.appendLine("URL: ${tx.str("url")}")
        sb.appendLine("Status: ${tx.str("statusCode")}")
        sb.appendLine("Duration: ${tx.str("durationMs")}ms")
        tx.strOrNull("protocol")?.let { sb.appendLine("Protocol: $it") }
        tx.strOrNull("tlsVersion")?.let { sb.appendLine("TLS: $it") }
        sb.appendLine()

        tx["requestHeaders"]?.jsonObject?.let { headers ->
            sb.appendLine("Request Headers:")
            headers.forEach { (k, v) -> sb.appendLine("  $k: ${v.content()}") }
            sb.appendLine()
        }
        tx["responseHeaders"]?.jsonObject?.let { headers ->
            sb.appendLine("Response Headers:")
            headers.forEach { (k, v) -> sb.appendLine("  $k: ${v.content()}") }
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
        if (totalSize != null) sb.appendLine("Size: $totalSize bytes")
        if (truncated) sb.appendLine("[TRUNCATED]")
        sb.appendLine()
        sb.append(body)
        return sb.toString()
    }

    fun formatCrashList(crashes: JsonArray): String {
        if (crashes.isEmpty()) return "No crashes recorded."
        val sb = StringBuilder()
        sb.appendLine("Found ${crashes.size} crash(es):\n")
        crashes.forEachIndexed { i, c ->
            val o = c.jsonObject
            sb.appendLine("${i + 1}. [id=${o.str("id")}] ${o.str("exceptionType")}: ${o.str("message")}")
        }
        return sb.toString()
    }

    fun formatCrashDetail(crash: JsonObject): String {
        val sb = StringBuilder()
        sb.appendLine("Crash: ${crash.str("id")}")
        sb.appendLine("Time: ${crash.str("timestamp")}")
        sb.appendLine("Exception: ${crash.str("exceptionType")}")
        sb.appendLine("Message: ${crash.str("message")}")
        sb.appendLine()
        sb.appendLine("Stack Trace:")
        sb.appendLine(crash.str("stackTrace"))
        return sb.toString()
    }

    fun formatLogEntries(logs: JsonArray): String {
        if (logs.isEmpty()) return "No log entries found."
        return logs.joinToString("\n") { entry ->
            val o = entry.jsonObject
            "${o.str("timestamp")} ${o.str("level")}/${o.str("tag")}: ${o.str("message")}"
        }
    }

    fun formatPerformanceSnapshot(data: JsonObject): String {
        val sb = StringBuilder()
        data["cpu"]?.jsonObject?.let { cpu ->
            sb.appendLine("CPU: ${cpu.str("overallUsagePercent")}% (${cpu.str("coreCount")} cores)")
            cpu.strOrNull("cpuFrequencyMHz")?.let { sb.appendLine("  Frequency: $it MHz") }
            cpu.strOrNull("cpuTemperature")?.let { sb.appendLine("  Temperature: $it\u00B0C") }
            sb.appendLine()
        }
        data["memory"]?.jsonObject?.let { mem ->
            sb.appendLine("Memory: ${mem.str("usedMemory")} / ${mem.str("totalMemory")} bytes")
            sb.appendLine("  Heap: ${mem.str("heapUsagePercent")}%")
            sb.appendLine("  Native: ${mem.str("nativeHeapAllocated")} bytes allocated")
            sb.appendLine()
        }
        data["fps"]?.jsonObject?.let { fps ->
            sb.appendLine(
                "FPS: ${fps.str("currentFps")} (avg: ${fps.str("averageFps")}, " +
                    "min: ${fps.str("minFps")}, max: ${fps.str("maxFps")})",
            )
            sb.appendLine("  Dropped: ${fps.str("droppedFrames")} | Jank: ${fps.str("jankFrames")}")
        }
        return sb.toString()
    }

    fun formatDatabaseList(databases: JsonArray): String {
        if (databases.isEmpty()) return "No databases found."
        val sb = StringBuilder()
        sb.appendLine("Found ${databases.size} database(s):\n")
        databases.forEachIndexed { i, db ->
            val o = db.jsonObject
            val tableCount = o["tables"]?.jsonArray?.size ?: "?"
            sb.appendLine("${i + 1}. ${o.str("name")} (${o.str("size")} bytes, tables: $tableCount)")
        }
        return sb.toString()
    }

    fun formatQueryResult(result: JsonObject): String {
        val columns = result["columns"]?.jsonArray?.map { it.jsonPrimitive.content } ?: return "No results."
        val rows = result["rows"]?.jsonArray ?: return "No results."
        if (rows.isEmpty()) return "Query returned 0 rows."

        val sb = StringBuilder()
        sb.appendLine("| ${columns.joinToString(" | ")} |")
        sb.appendLine("| ${columns.joinToString(" | ") { "---" }} |")
        rows.forEach { row ->
            val cells = row.jsonArray.map { it.jsonPrimitive.content }
            sb.appendLine("| ${cells.joinToString(" | ")} |")
        }
        sb.appendLine("\n${rows.size} row(s) returned.")
        return sb.toString()
    }

    fun formatPreferences(prefs: JsonArray): String {
        if (prefs.isEmpty()) return "No preferences found."
        val sb = StringBuilder()
        prefs.forEach { file ->
            val o = file.jsonObject
            sb.appendLine("File: ${o.str("fileName")}")
            o["entries"]?.jsonArray?.forEach { entry ->
                val e = entry.jsonObject
                sb.appendLine("  ${e.str("key")} = ${e.str("value")} (${e.str("type")})")
            }
            sb.appendLine()
        }
        return sb.toString()
    }

    fun formatDeviceInfo(info: JsonObject): String {
        val sb = StringBuilder()
        sb.appendLine("Device Information:")
        info.forEach { (key, value) ->
            val displayKey = key.replace(Regex("([A-Z])"), " $1").trim().replaceFirstChar { it.uppercase() }
            sb.appendLine("  $displayKey: ${value.content()}")
        }
        return sb.toString()
    }

    fun formatFileList(files: JsonArray): String {
        if (files.isEmpty()) return "No files found."
        val sb = StringBuilder()
        files.forEach { f ->
            val o = f.jsonObject
            val type = if (o["isDirectory"]?.jsonPrimitive?.boolean == true) "DIR " else "FILE"
            sb.appendLine("[$type] ${o.str("name")}  (${o.str("size")} bytes)")
        }
        return sb.toString()
    }

    fun formatGenericList(
        items: JsonArray,
        label: String,
    ): String {
        if (items.isEmpty()) return "No $label found."
        val sb = StringBuilder()
        sb.appendLine("Found ${items.size} $label:\n")
        items.forEachIndexed { i, item ->
            val o = item.jsonObject
            val fields = o.entries.joinToString(", ") { "${it.key}: ${it.value.content()}" }
            sb.appendLine("${i + 1}. $fields")
        }
        return sb.toString()
    }

    private fun JsonObject.str(key: String): String = this[key]?.jsonPrimitive?.contentOrNull ?: "N/A"

    private fun JsonObject.strOrNull(key: String): String? = this[key]?.jsonPrimitive?.contentOrNull

    private fun JsonElement.content(): String = when (this) {
        is JsonPrimitive -> contentOrNull ?: "null"
        is JsonArray -> joinToString(", ") { it.content() }
        is JsonObject -> toString()
    }
}

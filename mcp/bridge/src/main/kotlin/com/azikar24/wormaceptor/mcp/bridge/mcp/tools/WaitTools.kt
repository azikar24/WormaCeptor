package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.bridge.util.TextFormatter
import com.azikar24.wormaceptor.mcp.bridge.util.dataAs
import com.azikar24.wormaceptor.mcp.bridge.util.errorText
import com.azikar24.wormaceptor.mcp.bridge.util.toApiResponse
import com.azikar24.wormaceptor.mcp.protocol.WaitForCrashDto
import com.azikar24.wormaceptor.mcp.protocol.WaitForTransactionDto
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.io.IOException

private const val DefaultWaitSeconds = 30
private const val MaxWaitSeconds = 120

/** Head room over the device-side wait, so the device answers "timed out" before the HTTP call gives up. */
private const val HttpMarginMs = 5_000L
private const val MillisPerSecond = 1000L

private fun JsonObject.waitSeconds(): Int =
    (this["timeout_s"]?.jsonPrimitive?.intOrNull ?: DefaultWaitSeconds).coerceIn(1, MaxWaitSeconds)

private fun httpTimeoutMs(waitSeconds: Int): Long = waitSeconds * MillisPerSecond + HttpMarginMs

private fun timeoutSchema() = buildJsonObject {
    put("type", "integer")
    put("description", "Seconds to wait before giving up (default: $DefaultWaitSeconds, max $MaxWaitSeconds)")
    put("default", DefaultWaitSeconds)
}

internal class WaitForTransactionTool : McpTool() {

    override val name = "wait_for_transaction"

    override val description = "Block until the app makes a NEW HTTP request matching the filters, then return it. " +
        "Only requests that start after this call count, and the call returns once the response (or failure) " +
        "is recorded. Use it to watch a flow live: call it, ask the user to tap the button (or trigger it " +
        "yourself), and get the request the moment it lands. Returns a timeout message if nothing matches in time."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("url_contains") {
                put("type", "string")
                put("description", "Case-insensitive substring of the full URL, e.g. '/api/login'")
            }
            putJsonObject("method") {
                put("type", "string")
                put("description", "HTTP method, e.g. 'POST' (case-insensitive)")
            }
            putJsonObject("status") {
                put("type", "integer")
                put("description", "Exact HTTP status code, e.g. 500")
            }
            put("timeout_s", timeoutSchema())
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val seconds = arguments.waitSeconds()
        val params = buildMap {
            arguments["url_contains"]?.jsonPrimitive?.contentOrNull?.let { put("url_contains", it) }
            arguments["method"]?.jsonPrimitive?.contentOrNull?.let { put("method", it) }
            arguments["status"]?.jsonPrimitive?.intOrNull?.let { put("status", it.toString()) }
            put("timeout_s", seconds.toString())
        }
        val response = try {
            connection.apiClient.get("/api/wait/transaction", params, httpTimeoutMs(seconds)).toApiResponse()
        } catch (e: HttpRequestTimeoutException) {
            return "Timed out after $seconds s waiting for the device to answer (${e.message})."
        }
        val data = response.dataAs<WaitForTransactionDto>() ?: return response.errorText() ?: "Empty response."
        val transaction = data.transaction ?: return "Timed out after $seconds s: no new transaction matched " +
            "${params - "timeout_s"}. Trigger the request in the app, then call wait_for_transaction again."
        return "Matched after ${data.waitedMs} ms.\n\n" + TextFormatter.formatTransactionList(listOf(transaction))
    }
}

internal class WaitForCrashTool : McpTool() {

    override val name = "wait_for_crash"

    override val description = "Block until the app records a NEW crash (one that happens after this call), " +
        "then return it with its stack trace. Use it while reproducing a crash: call it, trigger the flow, " +
        "and get the exception as soon as it is saved. A fatal crash kills the app process, so the call " +
        "may end with a 'connection dropped' message instead; relaunch the app and use list_crashes then."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            put("timeout_s", timeoutSchema())
        }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val seconds = arguments.waitSeconds()
        val params = mapOf("timeout_s" to seconds.toString())
        val response = try {
            connection.apiClient.get("/api/wait/crash", params, httpTimeoutMs(seconds)).toApiResponse()
        } catch (e: HttpRequestTimeoutException) {
            return "Timed out after $seconds s waiting for the device to answer (${e.message})."
        } catch (e: IOException) {
            // Handled here: a retry from McpServer would start a fresh wait and miss the crash that killed the app.
            return "Connection to the app dropped while waiting (${e.message}). That usually means the app " +
                "crashed and its process died. Relaunch the app, then call list_crashes and get_crash."
        }
        val data = response.dataAs<WaitForCrashDto>()
        val crash = data?.crash
        return when {
            data == null -> response.errorText() ?: "Empty response."
            crash == null -> "Timed out after $seconds s: no new crash was recorded."
            else -> "Crash recorded after ${data.waitedMs} ms.\n\n" + TextFormatter.formatCrashDetail(crash)
        }
    }
}

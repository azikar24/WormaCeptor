package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.bridge.util.dataAs
import com.azikar24.wormaceptor.mcp.bridge.util.errorText
import com.azikar24.wormaceptor.mcp.bridge.util.toApiResponse
import com.azikar24.wormaceptor.mcp.bridge.util.toRequestBody
import com.azikar24.wormaceptor.mcp.protocol.CurlDto
import com.azikar24.wormaceptor.mcp.protocol.ReplayRequestDto
import com.azikar24.wormaceptor.mcp.protocol.ReplayResultDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

private const val IdRequired = "Error: 'id' parameter is required and must be non-empty."

private fun JsonObject.id(): String? = this["id"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

internal class ExportCurlTool : McpTool() {

    override val name = "export_curl"

    override val description = "Build a cURL command that reproduces a captured request: method, URL, headers, " +
        "and body, shell-quoted for bash/zsh. Use it to rerun the call outside the app and tell whether the " +
        "backend or the app is at fault. Sensitive headers (Authorization, Cookie, API keys) come out as " +
        "[REDACTED] and are listed so you can fill them in."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("id") {
                put("type", "string")
                put("description", "Transaction ID from list_transactions")
            }
        }
        putJsonArray("required") { add(JsonPrimitive("id")) }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val id = arguments.id() ?: return IdRequired
        val response = connection.apiClient.get("/api/transactions/$id/curl").toApiResponse()
        val data = response.dataAs<CurlDto>() ?: return response.errorText() ?: "Transaction not found."
        return buildString {
            append(data.command)
            if (data.redactedHeaders.isNotEmpty()) {
                append("\n\nRedacted headers, replace [REDACTED] before running: ")
                append(data.redactedHeaders.joinToString())
            }
            if (data.bodyOmitted) append("\n\nThe request body is binary or too large and was left out.")
        }
    }
}

internal class ReplayTransactionTool : McpTool() {

    override val name = "replay_transaction"

    // Re-sends a real request: a replayed POST/DELETE can change server state.
    override val annotations = ToolAnnotations(
        readOnlyHint = false,
        destructiveHint = true,
        idempotentHint = false,
        openWorldHint = true,
    )

    override val description = "Send a captured request again from the device, with optional URL, header, or " +
        "body overrides, and capture the result as a new transaction. Returns the new transaction id and status; " +
        "use get_transaction / get_response_body on it. The replay goes straight to the server: mock rules and " +
        "throttling don't apply, and the app's own interceptors don't run, so headers the app adds at send time " +
        "are only present if they were captured. Headers redacted at capture time are sent as captured."

    override val inputSchema = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            putJsonObject("id") {
                put("type", "string")
                put("description", "Transaction ID from list_transactions")
            }
            putJsonObject("url") {
                put("type", "string")
                put("description", "Full URL to send to instead of the captured one, e.g. a staging host")
            }
            putJsonObject("headers") {
                put("type", "object")
                putJsonObject("additionalProperties") { put("type", "string") }
                put("description", "Headers to set, replacing captured ones with the same name (case-insensitive)")
            }
            putJsonObject("body") {
                put("type", "string")
                put("description", "Request body to send instead of the captured one")
            }
        }
        putJsonArray("required") { add(JsonPrimitive("id")) }
    }

    override suspend fun execute(
        arguments: JsonObject,
        connection: DeviceConnection,
    ): String {
        val id = arguments.id() ?: return IdRequired
        val body = ReplayRequestDto(
            url = arguments["url"]?.jsonPrimitive?.contentOrNull,
            headers = arguments["headers"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content },
            body = arguments["body"]?.jsonPrimitive?.contentOrNull,
        )
        val response = connection.apiClient.post("/api/transactions/$id/replay", body.toRequestBody()).toApiResponse()
        val result = response.dataAs<ReplayResultDto>() ?: return response.errorText() ?: "Replay sent."
        val outcome = result.code?.let { "$it ${result.message.orEmpty()}".trim() } ?: "failed: ${result.error}"
        return "Replayed ${result.method} ${result.url} -> $outcome in ${result.durationMs} ms.\n" +
            "New transaction: ${result.transactionId}"
    }
}

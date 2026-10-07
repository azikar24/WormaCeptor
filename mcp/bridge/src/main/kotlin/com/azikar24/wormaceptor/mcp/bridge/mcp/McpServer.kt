package com.azikar24.wormaceptor.mcp.bridge.mcp

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.bridge.mcp.tools.McpTool
import com.azikar24.wormaceptor.mcp.bridge.util.JsonRpcError
import com.azikar24.wormaceptor.mcp.bridge.util.JsonRpcRequest
import com.azikar24.wormaceptor.mcp.bridge.util.JsonRpcResponse
import com.azikar24.wormaceptor.mcp.bridge.util.TextFormatter
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException

internal class McpServer(
    private val connection: DeviceConnection,
    private val tools: List<McpTool>,
    private val verbose: Boolean = false,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    suspend fun run(
        reader: BufferedReader = System.`in`.bufferedReader(),
        writer: BufferedWriter = System.out.bufferedWriter(),
    ) {
        while (true) {
            val line = reader.readLine() ?: break
            if (line.isBlank()) continue

            if (verbose) System.err.println("<-- $line")

            val response = try {
                val request = json.decodeFromString<JsonRpcRequest>(line)
                handleRequest(request)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                JsonRpcResponse(
                    error = JsonRpcError(-32700, "Parse error: ${e.message}"),
                )
            }

            if (response != null) {
                val encoded = json.encodeToString(response)
                if (verbose) System.err.println("--> $encoded")
                writer.write(encoded)
                writer.newLine()
                writer.flush()
            }
        }
    }

    private suspend fun handleRequest(request: JsonRpcRequest): JsonRpcResponse? {
        return when (request.method) {
            "initialize" -> JsonRpcResponse(
                id = request.id,
                result = McpCapabilities.initializeResponse(
                    ((request.params as? JsonObject)?.get("protocolVersion") as? JsonPrimitive)?.contentOrNull,
                ),
            )
            "notifications/initialized" -> null
            "tools/list" -> handleToolsList(request)
            "tools/call" -> handleToolCall(request)
            "ping" -> JsonRpcResponse(id = request.id, result = buildJsonObject {})
            else -> JsonRpcResponse(
                id = request.id,
                error = JsonRpcError(-32601, "Method not found: ${request.method}"),
            )
        }
    }

    private fun handleToolsList(request: JsonRpcRequest): JsonRpcResponse {
        return JsonRpcResponse(
            id = request.id,
            result = buildJsonObject {
                putJsonArray("tools") {
                    tools.forEach { add(it.toSchema()) }
                }
            },
        )
    }

    private suspend fun handleToolCall(request: JsonRpcRequest): JsonRpcResponse {
        val params = request.params?.jsonObject
            ?: return errorResponse(request.id, "Missing params")

        val toolName = params["name"]?.jsonPrimitive?.contentOrNull
            ?: return errorResponse(request.id, "Missing tool name")

        val arguments = params["arguments"]?.jsonObject ?: buildJsonObject {}

        val tool = tools.find { it.name == toolName }
            ?: return errorResponse(request.id, "Unknown tool: $toolName")

        return try {
            val result = TextFormatter.capResult(executeWithReconnect(tool, arguments))
            toolResult(request.id, result, isError = result.startsWith(McpTool.ERROR_PREFIX))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            toolResult(request.id, "${McpTool.ERROR_PREFIX}${e.message}", isError = true)
        }
    }

    private suspend fun executeWithReconnect(
        tool: McpTool,
        arguments: JsonObject,
    ): String = try {
        tool.execute(arguments, connection)
    } catch (e: IOException) {
        // A frozen or dead app won't come back by reconnecting: say what to do instead.
        connection.unreachableAppHint()?.let { throw IOException(it, e) }
        // App restarted or the adb forward dropped: re-forward once, then retry the call.
        if (!connection.reconnect()) {
            throw IOException("Device server unreachable; is the debug app running? (${e.message})", e)
        }
        tool.execute(arguments, connection)
    }

    private fun toolResult(
        id: JsonElement?,
        text: String,
        isError: Boolean,
    ) = JsonRpcResponse(
        id = id,
        result = buildJsonObject {
            putJsonArray("content") {
                addJsonObject {
                    put("type", "text")
                    put("text", text)
                }
            }
            if (isError) put("isError", true)
        },
    )

    private fun errorResponse(
        id: JsonElement?,
        message: String,
    ) = JsonRpcResponse(
        id = id,
        error = JsonRpcError(-32602, message),
    )
}

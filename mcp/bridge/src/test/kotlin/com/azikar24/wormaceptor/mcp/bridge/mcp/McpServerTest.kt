package com.azikar24.wormaceptor.mcp.bridge.mcp

import com.azikar24.wormaceptor.mcp.bridge.mcp.tools.ToolRegistry
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.StringWriter

/** Wire-level checks with the messages Claude Code 2.1 actually sends (captured from a real session). */
class McpServerTest {

    private fun exchange(vararg lines: String): List<Map<String, Any?>> {
        val out = StringWriter()
        runTest {
            McpServer(mockk(), ToolRegistry.allTools()).run(
                lines.joinToString("\n").reader().buffered(),
                out.buffered(),
            )
        }
        return out.toString().lines().filter { it.isNotBlank() }.map { line ->
            val o = Json.parseToJsonElement(line).jsonObject
            mapOf("jsonrpc" to o["jsonrpc"]?.jsonPrimitive?.content, "id" to o["id"]?.toString(), "body" to o)
        }
    }

    @Test
    fun `every response carries jsonrpc 2_0 and the request id`() {
        val responses = exchange(
            """{"jsonrpc":"2.0","id":"server-discover-probe-1","method":"server/discover","params":{}}""",
            """{"method":"initialize","params":{"protocolVersion":"2025-11-25","capabilities":{}},""" +
                """"jsonrpc":"2.0","id":0}""",
            """{"jsonrpc":"2.0","method":"notifications/initialized"}""",
            """{"jsonrpc":"2.0","id":1,"method":"tools/list"}""",
        )
        assertEquals(listOf("\"server-discover-probe-1\"", "0", "1"), responses.map { it["id"] })
        assertEquals(listOf("2.0", "2.0", "2.0"), responses.map { it["jsonrpc"] })
    }

    @Test
    fun `tools list returns all registered tools`() {
        val list = exchange("""{"jsonrpc":"2.0","id":1,"method":"tools/list"}""").single()
        val body = list["body"] as kotlinx.serialization.json.JsonObject
        assertEquals(32, body["result"]!!.jsonObject["tools"]!!.jsonArray.size)
    }

    private fun negotiated(params: String): String? {
        val init = exchange("""{"jsonrpc":"2.0","id":0,"method":"initialize","params":$params}""").single()
        val body = init["body"] as kotlinx.serialization.json.JsonObject
        return body["result"]!!.jsonObject["protocolVersion"]!!.jsonPrimitive.content
    }

    @Test
    fun `initialize echoes a supported protocol version`() {
        listOf("2024-11-05", "2025-06-18", "2025-11-25").forEach { version ->
            assertEquals(version, negotiated("""{"protocolVersion":"$version","capabilities":{}}"""))
        }
    }

    @Test
    fun `initialize answers the latest version for unsupported or missing requests`() {
        // 2025-03-26 requires accepting JSON-RPC batches, which the bridge doesn't.
        listOf(
            """{"protocolVersion":"2025-03-26","capabilities":{}}""",
            """{"protocolVersion":"2099-01-01","capabilities":{}}""",
            """{"capabilities":{}}""",
            """[]""",
        ).forEach { params -> assertEquals("2025-11-25", negotiated(params)) }
    }

    @Test
    fun `tools list annotates which tools change device state`() {
        val list = exchange("""{"jsonrpc":"2.0","id":1,"method":"tools/list"}""").single()
        val tools = (list["body"] as kotlinx.serialization.json.JsonObject)["result"]!!.jsonObject["tools"]!!.jsonArray
        val hints = tools.associate { tool ->
            val o = tool.jsonObject
            val annotations = o["annotations"]!!.jsonObject
            val values = HINTS.associateWith { annotations[it]!!.jsonPrimitive.content.toBoolean() }
            o["name"]!!.jsonPrimitive.content to values
        }

        val writers = setOf(
            "clear_transactions",
            "clear_crashes",
            "clear_logs",
            "set_rate_limit",
            "simulate_location",
            "stop_location_simulation",
            "send_push_notification",
        )
        assertEquals(hints.keys - writers, hints.filterValues { it.getValue("readOnlyHint") }.keys)
        assertEquals(
            setOf("clear_transactions", "clear_crashes", "clear_logs"),
            hints.filterValues { it.getValue("destructiveHint") }.keys,
        )
        assertEquals(setOf("send_push_notification"), hints.filterValues { !it.getValue("idempotentHint") }.keys)
        assertEquals(emptySet<String>(), hints.filterValues { it.getValue("openWorldHint") }.keys)
    }

    private companion object {
        val HINTS = listOf("readOnlyHint", "destructiveHint", "idempotentHint", "openWorldHint")
    }
}

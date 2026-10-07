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
}

package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceApiClient
import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolContractTest {

    private val apiClient = mockk<DeviceApiClient>()
    private val connection = mockk<DeviceConnection> { every { apiClient } returns this@ToolContractTest.apiClient }

    private fun McpTool.enumFor(param: String): List<String> =
        inputSchema["properties"]!!.jsonObject[param]!!.jsonObject["enum"]!!.jsonArray.map { it.jsonPrimitive.content }

    @Test
    fun `set_rate_limit sends the field names the device server expects`() = runTest {
        val body = slot<JsonElement>()
        coEvery { apiClient.post("/api/rate-limit", capture(body)) } returns buildJsonObject { put("success", true) }

        val args = buildJsonObject {
            put("enabled", true)
            put("download_kbps", 100)
            put("upload_kbps", 50)
            put("latency_ms", 200)
            put("packet_loss", 2.5)
        }
        val result = SetRateLimitTool().execute(args, connection)

        assertEquals("Rate limit configuration updated.", result)
        assertEquals(
            setOf("enabled", "downloadSpeedKbps", "uploadSpeedKbps", "latencyMs", "packetLossPercent"),
            body.captured.jsonObject.keys,
        )
        assertEquals(2.5f, body.captured.jsonObject["packetLossPercent"]!!.jsonPrimitive.content.toFloat())
    }

    @Test
    fun `set_rate_limit surfaces server errors`() = runTest {
        coEvery { apiClient.post(any(), any()) } returns buildJsonObject {
            put("success", false)
            put("error", "Unknown preset 'x'")
        }
        val result = SetRateLimitTool().execute(buildJsonObject { put("preset", "x") }, connection)
        assertEquals("Error: Unknown preset 'x'", result)
    }

    @Test
    fun `schema enums list the values the device server accepts`() {
        assertEquals(
            listOf("WIFI", "GOOD_3G", "REGULAR_3G", "SLOW_3G", "GOOD_2G", "SLOW_2G", "EDGE", "OFFLINE"),
            SetRateLimitTool().enumFor("preset"),
        )
        assertEquals(
            listOf("VERBOSE", "DEBUG", "INFO", "WARN", "ERROR", "ASSERT"),
            TailLogsTool().enumFor("level"),
        )
        assertEquals(listOf("low", "default", "high", "max"), SendPushNotificationTool().enumFor("priority"))
    }

    @Test
    fun `query_database rejects PRAGMA without calling the device`() = runTest {
        val args = buildJsonObject {
            put("database", "app_db")
            put("query", "PRAGMA table_info(users)")
        }
        val result = QueryDatabaseTool().execute(args, connection)
        assertEquals("Error: Only SELECT queries are allowed.", result)
        coVerify(exactly = 0) { apiClient.post(any(), any()) }
    }

    @Test
    fun `query_database sends the sql field the device server reads`() = runTest {
        val body = slot<JsonElement>()
        coEvery { apiClient.post("/api/databases/app.db/query", capture(body)) } returns buildJsonObject {
            put("success", true)
        }
        QueryDatabaseTool().execute(
            buildJsonObject {
                put("database", "app.db")
                put("query", "SELECT 1")
            },
            connection,
        )
        assertEquals(setOf("sql"), body.captured.jsonObject.keys)
    }

    @Test
    fun `read_file renders content and mime type from the JSON envelope`() = runTest {
        coEvery { apiClient.get("/api/files/read", mapOf("path" to "files/a.json")) } returns buildJsonObject {
            put("success", true)
            put(
                "data",
                JsonObject(mapOf("content" to JsonPrimitive("{ }"), "mimeType" to JsonPrimitive("application/json"))),
            )
        }
        val result = ReadFileTool().execute(buildJsonObject { put("path", "files/a.json") }, connection)
        assertTrue(result.startsWith("MIME Type: application/json"))
        assertTrue(result.endsWith("{ }"))
    }
}

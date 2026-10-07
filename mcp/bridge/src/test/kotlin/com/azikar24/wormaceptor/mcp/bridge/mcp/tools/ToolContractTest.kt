package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceApiClient
import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.protocol.LogEntryDto
import com.azikar24.wormaceptor.mcp.protocol.MonitoringStateDto
import com.azikar24.wormaceptor.mcp.protocol.ReadFileDto
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
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
    fun `set_monitoring sends target and enabled and reports the resulting state`() = runTest {
        val body = slot<JsonElement>()
        coEvery { apiClient.post("/api/monitoring", capture(body)) } returns
            serverResponse(MonitoringStateDto("cpu", running = true))
        val args = buildJsonObject {
            put("target", "cpu")
            put("enabled", true)
        }
        assertEquals("cpu monitoring is on.", SetMonitoringTool().execute(args, connection))
        assertEquals(setOf("target", "enabled"), body.captured.jsonObject.keys)
        assertEquals(listOf("cpu", "memory", "fps"), SetMonitoringTool().enumFor("target"))
    }

    @Test
    fun `set_monitoring explains a stop the overlay prevented`() = runTest {
        coEvery { apiClient.post("/api/monitoring", any()) } returns
            serverResponse(MonitoringStateDto("fps", running = true, note = "kept running: the overlay shows it"))
        val args = buildJsonObject {
            put("target", "fps")
            put("enabled", false)
        }
        assertEquals(
            "fps monitoring is on (kept running: the overlay shows it).",
            SetMonitoringTool().execute(args, connection),
        )
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
    fun `bring_app_to_front launches the cached package, or the one passed in`() = runTest {
        coEvery { connection.resolvePackage() } returns "com.cached"
        coEvery { connection.bringAppToFront(any()) } returns null
        assertEquals(
            "Brought com.cached to the foreground.",
            BringAppToFrontTool().execute(buildJsonObject {}, connection),
        )
        val explicit = BringAppToFrontTool().execute(buildJsonObject { put("package", "com.other") }, connection)
        assertEquals("Brought com.other to the foreground.", explicit)
        coVerify { connection.bringAppToFront("com.other") }
    }

    @Test
    fun `bring_app_to_front reports an unknown package and launch failures as errors`() = runTest {
        coEvery { connection.resolvePackage() } returns null
        assertTrue(BringAppToFrontTool().execute(buildJsonObject {}, connection).startsWith("Error: "))

        coEvery { connection.bringAppToFront("com.gone") } returns "No activities found to run, monkey aborted."
        assertEquals(
            "Error: Couldn't launch com.gone: No activities found to run, monkey aborted.",
            BringAppToFrontTool().execute(buildJsonObject { put("package", "com.gone") }, connection),
        )
    }

    @Test
    fun `tail_logs reads logcat through adb and applies level, tag and limit without the device route`() = runTest {
        val logs = listOf(
            LogEntryDto(0L, 1L, "ERROR", "OkHttp", 1, 1, "old"),
            LogEntryDto(1L, 2L, "INFO", "OkHttp", 1, 1, "info"),
            LogEntryDto(2L, 3L, "ERROR", "Auth", 1, 1, "other tag"),
            LogEntryDto(3L, 4L, "ERROR", "okhttp.Client", 1, 1, "newest"),
        )
        coEvery { connection.readAppLogs() } returns logs
        val args = buildJsonObject {
            put("level", "ERROR")
            put("tag", "OKHTTP")
            put("limit", 1)
        }
        val text = TailLogsTool().execute(args, connection)
        assertTrue(text, text.contains("ERROR/okhttp.Client: newest"))
        assertEquals(1, text.trim().lines().size)
        coVerify(exactly = 0) { apiClient.get(any(), any()) }
    }

    @Test
    fun `tail_logs falls back to the device route when adb can't read logcat`() = runTest {
        coEvery { connection.readAppLogs() } returns null
        coEvery { apiClient.get("/api/logs", mapOf("limit" to "30")) } returns
            serverResponse(listOf(LogEntryDto(1L, 1L, "INFO", "Tag", 1, 2, "from device")))
        assertTrue(TailLogsTool().execute(buildJsonObject {}, connection).contains("INFO/Tag: from device"))
    }

    @Test
    fun `clear_logs hides earlier adb logs only after the server cleared its buffer`() = runTest {
        every { connection.markLogsCleared() } just Runs
        coEvery { apiClient.post("/api/clear/logs", any()) } returns buildJsonObject { put("success", true) }
        assertEquals("Logs cleared.", ClearLogsTool().execute(buildJsonObject {}, connection))
        verify(exactly = 1) { connection.markLogsCleared() }
    }

    @Test
    fun `read_file renders content and mime type from the JSON envelope`() = runTest {
        coEvery { apiClient.get("/api/files/read", mapOf("path" to "files/a.json")) } returns
            serverResponse(ReadFileDto(content = "{ }", mimeType = "application/json"))
        val result = ReadFileTool().execute(buildJsonObject { put("path", "files/a.json") }, connection)
        assertTrue(result.startsWith("MIME Type: application/json"))
        assertTrue(result.endsWith("{ }"))
    }
}

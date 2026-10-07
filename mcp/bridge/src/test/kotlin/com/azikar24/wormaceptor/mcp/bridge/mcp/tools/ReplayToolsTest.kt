package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceApiClient
import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.protocol.CurlDto
import com.azikar24.wormaceptor.mcp.protocol.ReplayResultDto
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplayToolsTest {

    private val apiClient = mockk<DeviceApiClient>()
    private val connection = mockk<DeviceConnection> { every { apiClient } returns this@ReplayToolsTest.apiClient }

    @Test
    fun `replay_transaction sends overrides in the shape the device server reads`() = runTest {
        val body = slot<JsonElement>()
        coEvery { apiClient.post("/api/transactions/tx1/replay", capture(body)) } returns serverResponse(
            ReplayResultDto("tx2", "POST", "https://staging.test/login", 401, "Unauthorized", 42L, null),
        )
        val args = buildJsonObject {
            put("id", "tx1")
            put("url", "https://staging.test/login")
            putJsonObject("headers") { put("Authorization", "Bearer t") }
            put("body", "{}")
        }

        val result = ReplayTransactionTool().execute(args, connection)

        assertEquals(setOf("url", "headers", "body"), body.captured.jsonObject.keys)
        assertEquals(
            "Bearer t",
            body.captured.jsonObject["headers"]!!.jsonObject["Authorization"]!!.jsonPrimitive.content,
        )
        assertEquals(
            "Replayed POST https://staging.test/login -> 401 Unauthorized in 42 ms.\nNew transaction: tx2",
            result,
        )
    }

    @Test
    fun `replay_transaction reports a failed send`() = runTest {
        coEvery { apiClient.post(any(), any()) } returns serverResponse(
            ReplayResultDto("tx2", "GET", "https://a.test", null, null, 5L, "java.net.UnknownHostException: a.test"),
        )

        val result = ReplayTransactionTool().execute(buildJsonObject { put("id", "tx1") }, connection)

        assertTrue(result, result.contains("-> failed: java.net.UnknownHostException"))
    }

    @Test
    fun `export_curl lists redacted headers after the command`() = runTest {
        coEvery { apiClient.get("/api/transactions/tx1/curl") } returns serverResponse(
            CurlDto("curl 'https://a.test'", listOf("Authorization", "Cookie"), bodyOmitted = false),
        )

        val result = ExportCurlTool().execute(buildJsonObject { put("id", "tx1") }, connection)

        assertEquals(
            "curl 'https://a.test'\n\nRedacted headers, replace [REDACTED] before running: Authorization, Cookie",
            result,
        )
    }
}

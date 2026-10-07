package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceApiClient
import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.protocol.WaitForCrashDto
import com.azikar24.wormaceptor.mcp.protocol.WaitForTransactionDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class WaitToolsTest {

    private val apiClient = mockk<DeviceApiClient>()
    private val connection = mockk<DeviceConnection> { every { apiClient } returns this@WaitToolsTest.apiClient }

    @Test
    fun `wait_for_transaction forwards filters and gives the HTTP call 5 s more than the wait`() = runTest {
        coEvery { apiClient.get(any(), any(), any()) } returns serverResponse(WaitForTransactionDto(null, 10_000L))
        val args = buildJsonObject {
            put("url_contains", "/login")
            put("method", "POST")
            put("status", 401)
            put("timeout_s", 10)
        }

        val result = WaitForTransactionTool().execute(args, connection)

        coVerify {
            apiClient.get(
                "/api/wait/transaction",
                mapOf("url_contains" to "/login", "method" to "POST", "status" to "401", "timeout_s" to "10"),
                15_000L,
            )
        }
        assertTrue(result, result.startsWith("Timed out after 10 s: no new transaction matched"))
    }

    @Test
    fun `wait timeout is clamped to 120 s`() = runTest {
        coEvery { apiClient.get(any(), any(), any()) } returns serverResponse(WaitForCrashDto(null, 1L))

        WaitForCrashTool().execute(buildJsonObject { put("timeout_s", 999) }, connection)

        coVerify { apiClient.get("/api/wait/crash", mapOf("timeout_s" to "120"), 125_000L) }
    }

    @Test
    fun `wait_for_crash explains a dropped connection instead of retrying`() = runTest {
        coEvery { apiClient.get(any(), any(), any()) } throws IOException("Connection reset")

        val result = WaitForCrashTool().execute(buildJsonObject {}, connection)

        assertTrue(result, result.startsWith("Connection to the app dropped while waiting"))
        coVerify(exactly = 1) { apiClient.get(any(), any(), any()) }
    }

    @Test
    fun `wait_for_crash defaults to 30 s`() = runTest {
        coEvery { apiClient.get(any(), any(), any()) } returns serverResponse(WaitForCrashDto(null, 30_000L))

        val result = WaitForCrashTool().execute(buildJsonObject {}, connection)

        coVerify { apiClient.get("/api/wait/crash", mapOf("timeout_s" to "30"), 35_000L) }
        assertEquals("Timed out after 30 s: no new crash was recorded.", result)
    }
}

package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType
import com.azikar24.wormaceptor.mcp.bridge.device.DeviceApiClient
import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.protocol.MockFromTransactionResultDto
import com.azikar24.wormaceptor.mcp.protocol.MockRuleDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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

class MockToolsTest {

    private val apiClient = mockk<DeviceApiClient>()
    private val connection = mockk<DeviceConnection> { every { apiClient } returns this@MockToolsTest.apiClient }

    private val rule = MockRuleDto(
        "r1", "GET /x", true, "GET", "https://a.test/x", "EXACT", 200, "OK", "image/png", null, "none", "Always", 0, 1L,
    )

    @Test
    fun `match_type enum mirrors the app's UrlMatchType`() {
        val enum = CreateMockRuleTool().inputSchema["properties"]!!.jsonObject["match_type"]!!
            .jsonObject["enum"]!!.jsonArray.map { it.jsonPrimitive.content }
        assertEquals(UrlMatchType.entries.map { it.name }, enum)
    }

    @Test
    fun `create_mock_rule sends the field names the device server expects`() = runTest {
        val body = slot<JsonElement>()
        coEvery { apiClient.post("/api/mock-rules", capture(body)) } returns serverResponse(rule)
        val args = buildJsonObject {
            put("url_pattern", "https://a.test/x")
            put("status", 503)
            put("method", "GET")
            put("match_type", "EXACT")
            put("body", "{}")
            put("content_type", "application/json")
            put("delay_ms", 100)
            put("enabled", false)
        }

        CreateMockRuleTool().execute(args, connection)

        assertEquals(
            setOf("urlPattern", "status", "method", "matchType", "body", "contentType", "delayMs", "enabled"),
            body.captured.jsonObject.keys,
        )
    }

    @Test
    fun `create_mock_rule requires url_pattern and status before calling the device`() = runTest {
        val noStatus = CreateMockRuleTool().execute(buildJsonObject { put("url_pattern", "https://a") }, connection)
        val noPattern = CreateMockRuleTool().execute(buildJsonObject { put("status", 500) }, connection)

        assertTrue(noStatus, noStatus.startsWith("Error: 'status'"))
        assertTrue(noPattern, noPattern.startsWith("Error: 'url_pattern'"))
        coVerify(exactly = 0) { apiClient.post(any(), any()) }
    }

    @Test
    fun `mock_from_transaction says when the captured body was left out`() = runTest {
        coEvery { apiClient.post("/api/transactions/tx1/mock", any()) } returns
            serverResponse(MockFromTransactionResultDto(rule, bodyOmitted = true))

        val result = MockFromTransactionTool().execute(buildJsonObject { put("id", "tx1") }, connection)

        assertTrue(result, result.contains("binary or too large"))
        assertTrue(result, result.contains("Body: (empty)"))
    }
}

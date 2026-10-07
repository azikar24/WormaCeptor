package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.bridge.device.DeviceApiClient
import com.azikar24.wormaceptor.mcp.bridge.device.DeviceConnection
import com.azikar24.wormaceptor.mcp.protocol.ApiResponse
import com.azikar24.wormaceptor.mcp.protocol.AppDetailsDto
import com.azikar24.wormaceptor.mcp.protocol.BodyDto
import com.azikar24.wormaceptor.mcp.protocol.CpuHistoryDto
import com.azikar24.wormaceptor.mcp.protocol.CpuInfoDto
import com.azikar24.wormaceptor.mcp.protocol.CrashDto
import com.azikar24.wormaceptor.mcp.protocol.CrashSummaryDto
import com.azikar24.wormaceptor.mcp.protocol.DatabaseInfoDto
import com.azikar24.wormaceptor.mcp.protocol.DependencyInfoDto
import com.azikar24.wormaceptor.mcp.protocol.DeviceDetailsDto
import com.azikar24.wormaceptor.mcp.protocol.DeviceInfoDto
import com.azikar24.wormaceptor.mcp.protocol.FileEntryDto
import com.azikar24.wormaceptor.mcp.protocol.FpsHistoryDto
import com.azikar24.wormaceptor.mcp.protocol.FpsInfoDto
import com.azikar24.wormaceptor.mcp.protocol.LeakInfoDto
import com.azikar24.wormaceptor.mcp.protocol.LoadedLibraryDto
import com.azikar24.wormaceptor.mcp.protocol.LogEntryDto
import com.azikar24.wormaceptor.mcp.protocol.MemoryDetailsDto
import com.azikar24.wormaceptor.mcp.protocol.MemoryHistoryDto
import com.azikar24.wormaceptor.mcp.protocol.MemoryInfoDto
import com.azikar24.wormaceptor.mcp.protocol.MonitoringStateDto
import com.azikar24.wormaceptor.mcp.protocol.NetworkDetailsDto
import com.azikar24.wormaceptor.mcp.protocol.OsDetailsDto
import com.azikar24.wormaceptor.mcp.protocol.PerformanceSnapshotDto
import com.azikar24.wormaceptor.mcp.protocol.PreferenceDto
import com.azikar24.wormaceptor.mcp.protocol.PreferenceFileDto
import com.azikar24.wormaceptor.mcp.protocol.QueryResultDto
import com.azikar24.wormaceptor.mcp.protocol.RateLimitConfigDto
import com.azikar24.wormaceptor.mcp.protocol.ReadFileDto
import com.azikar24.wormaceptor.mcp.protocol.RequestDto
import com.azikar24.wormaceptor.mcp.protocol.ResponseDto
import com.azikar24.wormaceptor.mcp.protocol.ScreenDetailsDto
import com.azikar24.wormaceptor.mcp.protocol.SecureStorageEntryDto
import com.azikar24.wormaceptor.mcp.protocol.StorageDetailsDto
import com.azikar24.wormaceptor.mcp.protocol.ThreadViolationDto
import com.azikar24.wormaceptor.mcp.protocol.TimelineDto
import com.azikar24.wormaceptor.mcp.protocol.TimelineEventDto
import com.azikar24.wormaceptor.mcp.protocol.TransactionDetailDto
import com.azikar24.wormaceptor.mcp.protocol.TransactionSummaryDto
import com.azikar24.wormaceptor.mcp.protocol.WebSocketConnectionDto
import com.azikar24.wormaceptor.mcp.protocol.WebSocketMessageDto
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Feeds every tool a response built from real DTO instances with every nullable field set, so a
 * formatter that reads a field the server doesn't send shows up as "N/A" or a "not found" fallback.
 */
class ToolOutputTest {

    private val cpu = CpuInfoDto(1L, 12.5f, listOf(10f, 15f), 8, 2400L, 41.5f, 1000L, "proc")
    private val memory = MemoryInfoDto(1L, 100L, 50L, 150L, 300L, 33.3f, 40L, 30L, 10L, 7L)
    private val fps = FpsInfoDto(59.9f, 58.1f, 30f, 60f, 3, 1, 1L)

    private val getFixtures: Map<String, JsonElement> = mapOf(
        "/api/transactions" to serverResponse(
            listOf(
                TransactionSummaryDto(
                    "tx1", "GET", "https://a.test/x", "a.test", "/x", 200, 35L, true, true, "COMPLETED", 1L,
                ),
            ),
        ),
        "/api/transactions/tx1" to serverResponse(
            TransactionDetailDto(
                id = "tx1",
                timestamp = 1L,
                durationMs = 35L,
                status = "COMPLETED",
                request = RequestDto("https://a.test/x", "GET", mapOf("Accept" to listOf("*/*")), 0L),
                response = ResponseDto(200, "OK", mapOf("Server" to listOf("t")), "boom", "h2", "TLSv1.3", 2L),
                extensions = mapOf("k" to "v"),
            ),
        ),
        "/api/transactions/tx1/request-body" to serverResponse(BodyDto("{}", "application/json", false, 2L)),
        "/api/transactions/tx1/response-body" to serverResponse(BodyDto("{}", "application/json", true, 2L)),
        "/api/websockets/connections" to serverResponse(
            listOf(WebSocketConnectionDto(1L, "wss://a.test", "OPEN", 1L, 2L, 1000, "bye", 1L, true)),
        ),
        "/api/websockets/messages" to serverResponse(listOf(WebSocketMessageDto(1L, 1L, "TEXT", "SENT", "hi", 1L, 2L))),
        "/api/rate-limit" to serverResponse(RateLimitConfigDto(true, 100L, 50L, 200L, 2.5f, "WIFI")),
        "/api/crashes" to serverResponse(listOf(CrashSummaryDto(1L, 1L, "IllegalStateException", "bad"))),
        "/api/crashes/tx1" to serverResponse(CrashDto(1L, 1L, "IllegalStateException", "bad", "at a.b(c.kt:1)")),
        "/api/logs" to serverResponse(listOf(LogEntryDto(1L, 1L, "INFO", "Tag", 1, 2, "hello"))),
        "/api/leaks" to serverResponse(listOf(LeakInfoDto(1L, "Activity", "leaked", 10L, listOf("a", "b"), "HIGH"))),
        "/api/violations" to serverResponse(
            listOf(ThreadViolationDto(1L, 1L, "DISK_READ", "read", listOf("at a"), 5L, "main")),
        ),
        "/api/device-info" to serverResponse(deviceInfo()),
        "/api/performance" to serverResponse(PerformanceSnapshotDto(cpu, memory, fps, fpsMonitoring = true)),
        "/api/preferences" to serverResponse(
            listOf(PreferenceFileDto("prefs", 1, listOf(PreferenceDto("k", "v", "String")))),
        ),
        "/api/databases" to serverResponse(listOf(DatabaseInfoDto("app.db", "/data/app.db", 4096L, 2))),
        "/api/files/browse" to serverResponse(
            listOf(FileEntryDto("a.txt", "/data/files/a.txt", false, 3L, 1L, "rw-", true, true)),
        ),
        "/api/files/read" to serverResponse(ReadFileDto("abc", "text/plain")),
        "/api/secure-storage" to serverResponse(listOf(SecureStorageEntryDto("token", "ENCRYPTED_PREFS", true, 1L))),
        "/api/dependencies" to serverResponse(
            listOf(
                DependencyInfoDto(
                    "OkHttp", "com.squareup.okhttp3", "okhttp", "4.12.0", "NETWORKING", "CLASS", "okhttp3",
                    true, "HTTP client", "https://square.github.io/okhttp", false, "com.squareup.okhttp3:okhttp:4.12.0",
                ),
            ),
        ),
        "/api/loaded-libraries" to serverResponse(
            listOf(LoadedLibraryDto("libc.so", "/system/lib64/libc.so", "NATIVE_SO", 10L, "0x1", "1", true)),
        ),
        "/api/timeline" to serverResponse(
            TimelineDto(
                1L,
                2L,
                listOf(TimelineEventDto(1L, "transaction", "tx1", "GET https://a.test/x -> 200")),
                1,
                true,
            ),
        ),
    )

    private val ok = ServerJson.encodeToJsonElement(ApiResponse(success = true))
    private val missing = mutableListOf<String>()

    private val apiClient = mockk<DeviceApiClient> {
        coEvery { get(any(), any()) } answers {
            val path: String = firstArg()
            val params: Map<String, String> = secondArg()
            historyFixture(path, params["include_history"] == "true")
                ?: getFixtures[path]
                ?: ok.also { missing += "GET $path" }
        }
        coEvery { post("/api/databases/app.db/query", any()) } returns serverResponse(
            QueryResultDto(listOf("id", "name"), listOf(listOf("1", null)), 1, error = null),
        )
        coEvery { post(match { it != "/api/databases/app.db/query" }, any()) } returns ok
        coEvery {
            post(
                "/api/monitoring",
                any(),
            )
        } returns serverResponse(MonitoringStateDto("fps", true, "already running"))
        coEvery { delete(any()) } returns ok
    }
    private val connection = mockk<DeviceConnection> {
        every { apiClient } returns this@ToolOutputTest.apiClient
        every { packageName } returns "com.example"
        coEvery { bringAppToFront(any()) } returns null
        coEvery { readAppLogs() } returns null
        every { markLogsCleared() } just Runs
    }

    private fun historyFixture(
        path: String,
        withHistory: Boolean,
    ): JsonElement? = when (path) {
        "/api/cpu" -> if (withHistory) serverResponse(CpuHistoryDto(cpu, listOf(cpu), true)) else serverResponse(cpu)
        "/api/memory" -> if (withHistory) {
            serverResponse(MemoryHistoryDto(memory, listOf(memory), true))
        } else {
            serverResponse(memory)
        }
        "/api/fps" -> if (withHistory) serverResponse(FpsHistoryDto(fps, listOf(fps), true)) else serverResponse(fps)
        else -> null
    }

    private fun args(withHistory: Boolean) = buildJsonObject {
        put("id", "tx1")
        put("database", "app.db")
        put("query", "SELECT 1")
        put("path", "files")
        put("latitude", 1.0)
        put("longitude", 2.0)
        put("title", "t")
        put("body", "b")
        put("target", "fps")
        put("enabled", true)
        put("include_history", withHistory)
    }

    private suspend fun outputGaps(withHistory: Boolean): List<String> = ToolRegistry.allTools().mapNotNull { tool ->
        val result = tool.execute(args(withHistory), connection)
        val gap = FALLBACK.containsMatchIn(result) || result.startsWith("Error")
        "${tool.name}: $result".takeIf { gap }
    }

    @Test
    fun `no tool prints N_A or a fallback for a fully populated server response`() = runTest {
        val gaps = outputGaps(withHistory = false) + outputGaps(withHistory = true)
        assertEquals("tools without a fixture", emptyList<String>(), missing)
        assertEquals("tool output missing DTO fields", emptyList<String>(), gaps)
    }

    @Test
    fun `query_database shows the server's SQL error instead of an empty table`() = runTest {
        coEvery { apiClient.post("/api/databases/app.db/query", any()) } returns serverResponse(
            QueryResultDto(emptyList(), emptyList(), 0, error = "no such table: users"),
        )
        val result = QueryDatabaseTool().execute(args(withHistory = false), connection)
        assertEquals("Error: no such table: users", result)
    }

    private fun deviceInfo() = DeviceInfoDto(
        device = DeviceDetailsDto("Google", "Pixel", "google", "oriole", "hw", "board", "product", false),
        os = OsDetailsDto("15", 35, "AP3A", "2025-01-01", "boot", "fp", "inc"),
        screen = ScreenDetailsDto(1080, 2400, 420, 2.625f, 2.625f, "NORMAL", "PORTRAIT", 120f),
        memory = MemoryDetailsDto(8L, 4L, 1L, false, 4L, 50f),
        storage = StorageDetailsDto(128L, 64L, 64L, 32L, 16L, 16L, true),
        app = AppDetailsDto("com.example", "1.0", 1L, 35, 23, 1L, 2L, true),
        network = NetworkDetailsDto("WIFI", true, true, false, false, "ssid", "LTE"),
        timestamp = 1L,
    )

    private companion object {
        val FALLBACK = Regex("""N/A|^No |not found|unavailable""")
    }
}

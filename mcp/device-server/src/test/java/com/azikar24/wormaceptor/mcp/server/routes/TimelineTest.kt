package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LogEntry
import com.azikar24.wormaceptor.domain.entities.LogLevel
import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.domain.entities.WebSocketMessage
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageDirection
import com.azikar24.wormaceptor.domain.entities.WebSocketMessageType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

class TimelineTest {

    private val sources = TimelineSources(
        transactions = listOf(tx(400), tx(100)),
        crashes = listOf(Crash(7, 500, "IllegalStateException", "boom", stackTrace = "")),
        logs = listOf(LogEntry(3, 200, LogLevel.ERROR, "Auth", pid = 1, message = "denied")),
        violations = listOf(
            ThreadViolation(9, 300, ThreadViolation.ViolationType.DISK_READ, "read", emptyList(), 5, "main"),
        ),
        leaks = listOf(LeakInfo(600, "MainActivity", "retained", 10, emptyList(), LeakInfo.LeakSeverity.HIGH)),
        webSocketMessages = listOf(
            WebSocketMessage(4, 1, WebSocketMessageType.TEXT, WebSocketMessageDirection.SENT, "hi", 250, 2),
        ),
    )

    private fun tx(
        timestamp: Long,
        code: Int? = 200,
        status: TransactionStatus = TransactionStatus.COMPLETED,
    ) = TransactionSummary(
        id = UUID.randomUUID(),
        method = "GET",
        host = "api.test",
        path = "/users",
        code = code,
        tookMs = 12,
        hasRequestBody = false,
        hasResponseBody = true,
        status = status,
        timestamp = timestamp,
        url = "https://api.test/users",
    )

    @Test
    fun `merges every source oldest first`() {
        val timeline = buildTimeline(sources, sinceMs = 0, untilMs = 1_000, limit = 50)

        timeline.events.map { it.type } shouldBe
            listOf("transaction", "log", "websocket", "violation", "transaction", "crash", "leak")
        timeline.events.map { it.timestamp } shouldBe listOf(100L, 200L, 250L, 300L, 400L, 500L, 600L)
        timeline.totalEvents shouldBe 7
        timeline.logsIncluded shouldBe true
    }

    @Test
    fun `window bounds are inclusive and drop everything outside`() {
        val timeline = buildTimeline(sources, sinceMs = 200, untilMs = 400, limit = 50)

        timeline.events.map { it.timestamp } shouldBe listOf(200L, 250L, 300L, 400L)
    }

    @Test
    fun `limit keeps the newest events and total counts the whole window`() {
        val timeline = buildTimeline(sources, sinceMs = 0, untilMs = 1_000, limit = 2)

        timeline.events.map { it.type } shouldBe listOf("crash", "leak")
        timeline.totalEvents shouldBe 7
    }

    @Test
    fun `logs are reported as not included when capture is off`() {
        val timeline = buildTimeline(sources.copy(logs = null), sinceMs = 0, untilMs = 1_000, limit = 50)

        timeline.logsIncluded shouldBe false
        timeline.events.none { it.type == "log" } shouldBe true
    }

    @Test
    fun `summaries carry the fields an agent needs`() {
        val inFlight = tx(10, code = null, status = TransactionStatus.ACTIVE)
        val events = buildTimeline(
            sources.copy(transactions = listOf(inFlight)),
            sinceMs = 0,
            untilMs = 1_000,
            limit = 50,
        ).events.associateBy { it.type }

        events.getValue("transaction").summary shouldBe "GET https://api.test/users -> in flight (12 ms)"
        events.getValue("transaction").id shouldBe inFlight.id.toString()
        events.getValue("crash").summary shouldBe "IllegalStateException: boom"
        events.getValue("log").summary shouldBe "E/Auth: denied"
        events.getValue("leak").id shouldBe null
    }
}

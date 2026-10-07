package com.azikar24.wormaceptor.mcp.bridge.mcp.tools

import com.azikar24.wormaceptor.mcp.protocol.LogEntryDto
import com.azikar24.wormaceptor.mcp.protocol.TimelineDto
import com.azikar24.wormaceptor.mcp.protocol.TimelineEventDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineLogMergeTest {

    private val timeline = TimelineDto(
        sinceMs = 100,
        untilMs = 200,
        events = listOf(tx(120), tx(180)),
        totalEvents = 2,
        logsIncluded = false,
    )

    private fun tx(at: Long) = TimelineEventDto(at, "transaction", "t$at", "GET /x")

    private fun log(at: Long) = LogEntryDto(at, at, "ERROR", "Auth", 1, 1, "failed at $at")

    @Test
    fun `adb logs inside the window are merged in time order`() {
        val merged = mergeLogs(timeline, listOf(log(90), log(150), log(250)), limit = 10)

        assertEquals(listOf(120L, 150L, 180L), merged.events.map { it.timestamp })
        assertEquals("ERROR/Auth: failed at 150", merged.events[1].summary)
        assertEquals(3, merged.totalEvents)
        assertTrue(merged.logsIncluded)
    }

    @Test
    fun `info logs stay out of the timeline`() {
        val info = log(150).copy(level = "INFO")
        assertEquals(listOf(120L, 180L), mergeLogs(timeline, listOf(info), limit = 10).events.map { it.timestamp })
    }

    @Test
    fun `limit keeps the newest events`() {
        val merged = mergeLogs(timeline, listOf(log(150), log(190)), limit = 2)
        assertEquals(listOf(180L, 190L), merged.events.map { it.timestamp })
    }
}

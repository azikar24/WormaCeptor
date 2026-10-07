package com.azikar24.wormaceptor.mcp.bridge.util

import com.azikar24.wormaceptor.mcp.protocol.LogEntryDto
import com.azikar24.wormaceptor.mcp.protocol.TransactionSummaryDto
import com.azikar24.wormaceptor.mcp.protocol.WebSocketMessageDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextFormatterTest {

    private fun log(
        id: Long,
        message: String,
    ) = LogEntryDto(id, 1L, "INFO", "Tag", 1, 2, message)

    @Test
    fun `long log list keeps the newest entries and says how many were cut`() {
        val logs = (1L..1_000L).map { log(it, "message $it ".padEnd(150, 'x')) }
        val text = TextFormatter.formatLogEntries(logs)

        assertTrue(text.length <= TextFormatter.MAX_RESULT_CHARS)
        assertTrue(text.contains("message 1000 "))
        assertFalse(text.contains("message 1 "))
        val cut = Regex("""truncated, (\d+) more entries""").find(text)!!.groupValues[1].toInt()
        assertEquals(1_000, cut + text.lines().count { it.contains("INFO/Tag") })
    }

    @Test
    fun `long log message is clipped`() {
        val text = TextFormatter.formatLogEntries(listOf(log(1, "a".repeat(2_000))))
        assertTrue(text.contains("a".repeat(TextFormatter.MAX_VALUE_CHARS) + "… (+1500 chars)"))
        assertFalse(text.contains("a".repeat(TextFormatter.MAX_VALUE_CHARS + 1)))
    }

    @Test
    fun `long transaction list keeps the first rows`() {
        val txs = (1..500).map {
            TransactionSummaryDto(
                "id-$it", "GET", "https://a.test/" + "p".repeat(100), "a.test", "/p", 200, 1L, false, false, "OK", 1L,
            )
        }
        val text = TextFormatter.formatTransactionList(txs)
        assertTrue(text.length <= TextFormatter.MAX_RESULT_CHARS)
        assertTrue(text.contains("| id-1 |"))
        assertFalse(text.contains("| id-500 |"))
        assertTrue(text.trimEnd().endsWith("use limit/offset to page"))
    }

    @Test
    fun `generic list values are clipped`() {
        val message = WebSocketMessageDto(1L, 1L, "TEXT", "SENT", "p".repeat(5_000), 1L, 5_000L)
        val text = TextFormatter.formatGenericList(listOf(message), WebSocketMessageDto.serializer(), "message(s)")
        assertTrue(text.contains("… (+4500 chars)"))
    }

    @Test
    fun `any oversized result is capped with a footer`() {
        val text = TextFormatter.capResult("b".repeat(50_000))
        assertTrue(text.length <= TextFormatter.MAX_RESULT_CHARS + 100)
        assertTrue(text.endsWith("… truncated, 30000 more characters"))
        assertEquals("short", TextFormatter.capResult("short"))
    }
}

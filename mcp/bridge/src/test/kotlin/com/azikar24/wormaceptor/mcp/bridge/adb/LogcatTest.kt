package com.azikar24.wormaceptor.mcp.bridge.adb

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class LogcatTest {

    private val zone = ZoneId.of("Asia/Riyadh")
    private val now = ZonedDateTime.of(2026, 10, 7, 18, 30, 0, 0, zone)

    // `adb logcat -d -v threadtime --pid=4321` shape: banner lines, padded tags, empty and tab-led messages.
    private val sample = """
        --------- beginning of main
        10-07 18:05:12.345  4321  4321 I ActivityThread: Displayed com.example/.MainActivity: +412ms
        10-07 18:05:12.400  4321  4377 D OkHttp  : --> GET https://httpbin.org/get
        10-07 18:05:13.002  4321  4377 E DemoAuth: java.lang.IllegalStateException: token expired
        10-07 18:05:13.002  4321  4377 E DemoAuth: ${'\t'}at com.example.Auth.refresh(Auth.kt:42)
        --------- beginning of crash
        10-07 18:05:14.110  4321  4321 F AndroidRuntime: FATAL EXCEPTION: main
        10-07 18:05:14.200  4321  4390 W System.err:${' '}
        10-07 18:05:15.000  4321  4390 V okhttp.OkHttpClient: verbose
    """.trimIndent()

    @Test
    fun `parses threadtime lines and skips banners`() {
        val entries = parseThreadtime(sample, now)
        assertEquals(7, entries.size)

        val first = entries.first()
        assertEquals(
            ZonedDateTime.of(2026, 10, 7, 18, 5, 12, 345_000_000, zone).toInstant().toEpochMilli(),
            first.timestamp,
        )
        assertEquals("INFO", first.level)
        assertEquals("ActivityThread", first.tag)
        assertEquals(4321, first.pid)
        assertEquals(4321, first.tid)
        assertEquals("Displayed com.example/.MainActivity: +412ms", first.message)

        assertEquals("OkHttp", entries[1].tag)
        assertEquals("--> GET https://httpbin.org/get", entries[1].message)
        assertEquals(4377, entries[1].tid)
        assertEquals("\tat com.example.Auth.refresh(Auth.kt:42)", entries[3].message)
        assertEquals("ASSERT", entries[4].level)
        assertEquals("", entries[5].message)
        assertEquals(listOf("INFO", "DEBUG", "ERROR", "ERROR", "ASSERT", "WARN", "VERBOSE"), entries.map { it.level })
    }

    @Test
    fun `a date after now belongs to last year`() {
        val newYear = ZonedDateTime.of(2027, 1, 1, 0, 0, 5, 0, zone)
        val entry = parseThreadtime("12-31 23:59:58.000  1  1 I T: m", newYear).single()
        assertEquals(ZonedDateTime.of(2026, 12, 31, 23, 59, 58, 0, zone).toInstant().toEpochMilli(), entry.timestamp)
    }

    @Test
    fun `filters by exact level, tag substring and newest limit`() {
        val entries = parseThreadtime(sample, now)
        assertEquals(
            listOf("DemoAuth", "DemoAuth"),
            filterLogs(entries, level = "ERROR", tag = null, limit = 10).map {
                it.tag
            },
        )
        assertEquals(
            listOf("OkHttp", "okhttp.OkHttpClient"),
            filterLogs(entries, level = null, tag = "okhttp", limit = 10).map { it.tag },
        )
        assertEquals(
            listOf("System.err", "okhttp.OkHttpClient"),
            filterLogs(entries, null, null, limit = 2).map { it.tag },
        )
        // Same as the device route: an unknown level is ignored rather than matching nothing.
        assertEquals(7, filterLogs(entries, level = "BOGUS", tag = null, limit = 10).size)
        assertEquals(2, filterLogs(entries, level = "error", tag = null, limit = 10).size)
    }
}

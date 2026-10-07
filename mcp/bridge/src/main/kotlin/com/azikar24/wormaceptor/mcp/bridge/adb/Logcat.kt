package com.azikar24.wormaceptor.mcp.bridge.adb

import com.azikar24.wormaceptor.mcp.protocol.LogEntryDto
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** Logcat priority letters to the device server's `LogLevel` names. `F` (fatal) is Android's ASSERT. */
private val Levels = mapOf(
    'V' to "VERBOSE",
    'D' to "DEBUG",
    'I' to "INFO",
    'W' to "WARN",
    'E' to "ERROR",
    'F' to "ASSERT",
    'A' to "ASSERT",
)

private val ThreadtimeLine = Regex(
    """^(\d\d-\d\d \d\d:\d\d:\d\d\.\d{3})\s+(\d+)\s+(\d+)\s+([VDIWEFA]) (.*?)\s*:(?: (.*))?$""",
)

// SMART resolving turns 02-29 in a non-leap year into 02-28 instead of failing.
private val StampFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS")

/**
 * Parses `logcat -v threadtime` output. The format has no year: dates are placed in [now]'s year,
 * or the year before when that would put them in the future (a buffer read just after New Year).
 */
internal fun parseThreadtime(
    output: String,
    now: ZonedDateTime = ZonedDateTime.now(),
): List<LogEntryDto> = output.lineSequence().mapNotNull { ThreadtimeLine.matchEntire(it.trimEnd('\r')) }
    .mapIndexed { index, match ->
        val groups = match.groupValues.drop(1)
        val (stamp, pid, tid, level, tag) = groups
        val message = groups.last()
        val time = LocalDateTime.parse("${now.year}-$stamp", StampFormat).atZone(now.zone)
            .let { if (it.isAfter(now.plusDays(1))) it.minusYears(1) else it }
        LogEntryDto(
            id = index.toLong(),
            timestamp = time.toInstant().toEpochMilli(),
            level = Levels.getValue(level.single()),
            tag = tag,
            pid = pid.toInt(),
            tid = tid.toInt(),
            message = message,
        )
    }
    .toList()

/** Same filters as the device's `/api/logs`: exact level (unknown levels ignored), tag substring, newest [limit]. */
internal fun filterLogs(
    entries: List<LogEntryDto>,
    level: String?,
    tag: String?,
    limit: Int,
): List<LogEntryDto> {
    val wanted = level?.uppercase()?.takeIf { it in Levels.values }
    return entries
        .filter { wanted == null || it.level == wanted }
        .filter { tag == null || it.tag.contains(tag, ignoreCase = true) }
        .takeLast(limit)
}

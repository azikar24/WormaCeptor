package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.LeakDetectionEngine
import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.core.engine.ThreadViolationEngine
import com.azikar24.wormaceptor.core.engine.WebSocketMonitorEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LogEntry
import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.domain.entities.WebSocketMessage
import com.azikar24.wormaceptor.mcp.protocol.ApiResponse
import com.azikar24.wormaceptor.mcp.protocol.TimelineDto
import com.azikar24.wormaceptor.mcp.protocol.TimelineEventDto
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.flow.first

private const val DefaultWindowMs = 5 * 60 * 1000L
private const val DefaultTimelineLimit = 50
private const val MaxTimelineLimit = 500
private const val MaxSummaryChars = 200

/** Everything the timeline merges. [logs] is null when log capture is off. */
internal data class TimelineSources(
    val transactions: List<TransactionSummary> = emptyList(),
    val crashes: List<Crash> = emptyList(),
    val logs: List<LogEntry>? = null,
    val violations: List<ThreadViolation> = emptyList(),
    val leaks: List<LeakInfo> = emptyList(),
    val webSocketMessages: List<WebSocketMessage> = emptyList(),
)

/** Merges [sources] into one list of events in `[sinceMs, untilMs]`, oldest first, keeping the newest [limit]. */
internal fun buildTimeline(
    sources: TimelineSources,
    sinceMs: Long,
    untilMs: Long,
    limit: Int,
): TimelineDto {
    val window = sinceMs..untilMs
    val events = buildList {
        sources.transactions.filter { it.timestamp in window }.mapTo(this) { it.toTimelineEvent() }
        sources.crashes.filter { it.timestamp in window }.mapTo(this) { it.toTimelineEvent() }
        sources.logs.orEmpty().filter { it.timestamp in window }.mapTo(this) { it.toTimelineEvent() }
        sources.violations.filter { it.timestamp in window }.mapTo(this) { it.toTimelineEvent() }
        sources.leaks.filter { it.timestamp in window }.mapTo(this) { it.toTimelineEvent() }
        sources.webSocketMessages.filter { it.timestamp in window }.mapTo(this) { it.toTimelineEvent() }
    }.sortedBy { it.timestamp }
    return TimelineDto(
        sinceMs = sinceMs,
        untilMs = untilMs,
        events = events.takeLast(limit),
        totalEvents = events.size,
        logsIncluded = sources.logs != null,
    )
}

internal fun Routing.timelineRoutes() {
    get("/api/timeline") {
        val queryEngine = CoreHolder.queryEngine
        if (queryEngine == null) {
            call.respond(ApiResponse(success = false, error = "Transaction engine not available"))
            return@get
        }
        val untilMs = call.parameters["until_ms"]?.toLongOrNull() ?: System.currentTimeMillis()
        val sinceMs = call.parameters["since_ms"]?.toLongOrNull() ?: (untilMs - DefaultWindowMs)
        val limit = call.parameters["limit"]?.toIntOrNull()?.coerceIn(1, MaxTimelineLimit) ?: DefaultTimelineLimit

        val koin = WormaCeptorKoin.getKoin()
        val sources = TimelineSources(
            transactions = queryEngine.observeTransactions().first(),
            crashes = queryEngine.observeCrashes().first(),
            logs = koin.getOrNull<LogCaptureEngine>()?.takeIf { it.isCapturing.value }?.logs?.value,
            violations = koin.getOrNull<ThreadViolationEngine>()?.violations?.value.orEmpty(),
            leaks = koin.getOrNull<LeakDetectionEngine>()?.detectedLeaks?.value.orEmpty(),
            webSocketMessages = koin.getOrNull<WebSocketMonitorEngine>()?.messages?.value.orEmpty(),
        )
        val timeline = buildTimeline(sources, sinceMs, untilMs, limit)
        val data = JsonConfig.instance.encodeToJsonElement(TimelineDto.serializer(), timeline)
        call.respond(ApiResponse(success = true, data = data))
    }
}

private fun TransactionSummary.toTimelineEvent(): TimelineEventDto {
    val outcome = when {
        code != null -> code.toString()
        status == TransactionStatus.ACTIVE -> "in flight"
        else -> status.name.lowercase()
    }
    val took = tookMs?.let { " ($it ms)" }.orEmpty()
    val summary = "$method ${url.ifEmpty { host + path }} -> $outcome$took"
    return TimelineEventDto(timestamp, "transaction", id.toString(), summary)
}

private fun Crash.toTimelineEvent() =
    TimelineEventDto(timestamp, "crash", id.toString(), "$exceptionType: ${message.orEmpty()}".clip())

private fun LogEntry.toTimelineEvent() =
    TimelineEventDto(timestamp, "log", id.toString(), "${level.tag}/$tag: $message".clip())

private fun ThreadViolation.toTimelineEvent() =
    TimelineEventDto(timestamp, "violation", id.toString(), "$violationType on $threadName: $description".clip())

private fun LeakInfo.toTimelineEvent() =
    TimelineEventDto(timestamp, "leak", null, "$severity $objectClass: $leakDescription".clip())

private fun WebSocketMessage.toTimelineEvent() = TimelineEventDto(
    timestamp,
    "websocket",
    id.toString(),
    "$direction $type on connection $connectionId: ${payloadPreview(MaxSummaryChars)}",
)

private fun String.clip(): String = if (length <= MaxSummaryChars) this else take(MaxSummaryChars) + "..."

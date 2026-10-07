package com.azikar24.wormaceptor.mcp.protocol

import kotlinx.serialization.Serializable

/**
 * One item on the timeline. [type] is `transaction`, `crash`, `log`, `violation`, `leak` or `websocket`;
 * [id] is the id the matching get/list tool accepts (leaks have none).
 */
@Serializable
internal data class TimelineEventDto(
    val timestamp: Long,
    val type: String,
    val id: String?,
    val summary: String,
)

/**
 * Events between [sinceMs] and [untilMs] (device clock), oldest first. [totalEvents] counts the whole window;
 * [events] keeps only the newest ones up to the requested limit.
 */
@Serializable
internal data class TimelineDto(
    val sinceMs: Long,
    val untilMs: Long,
    val events: List<TimelineEventDto>,
    val totalEvents: Int,
    val logsIncluded: Boolean,
)

package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.domain.entities.LogLevel
import com.azikar24.wormaceptor.mcp.protocol.ApiResponse
import com.azikar24.wormaceptor.mcp.protocol.LogEntryDto
import com.azikar24.wormaceptor.mcp.protocol.ResponseMeta
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_LIMIT = 100
private const val DEFAULT_OFFSET = 0

internal fun Routing.logRoutes() {
    get("/api/logs") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val logEngine = koin.getOrNull<LogCaptureEngine>()
            if (logEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Log engine not available"),
                )
                return@get
            }

            val limit = call.parameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT
            val offset = call.parameters["offset"]?.toIntOrNull() ?: DEFAULT_OFFSET
            val levelFilter = call.parameters["level"]
            val tagFilter = call.parameters["tag"]

            // No auto-start: starting capture clears logcat and triggers Android 13+'s log access prompt.
            // The bridge reads logcat over adb and only falls back here.
            var logs = logEngine.logs.value

            if (levelFilter != null) {
                val targetLevel = try {
                    LogLevel.valueOf(levelFilter.uppercase())
                } catch (_: IllegalArgumentException) {
                    null
                }
                if (targetLevel != null) {
                    logs = logs.filter { it.level == targetLevel }
                }
            }

            if (tagFilter != null) {
                logs = logs.filter { it.tag.contains(tagFilter, ignoreCase = true) }
            }

            val total = logs.size
            // Newest entries, oldest first within the page; offset skips the most recent ones.
            val paged = logs.dropLast(offset).takeLast(limit)
            val dtos = paged.map { it.toDto() }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                ListSerializer(LogEntryDto.serializer()),
                dtos,
            )

            call.respond(
                ApiResponse(
                    success = true,
                    data = dataElement,
                    meta = ResponseMeta(
                        total = total,
                        limit = limit,
                        offset = offset,
                    ),
                ),
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to fetch logs"),
            )
        }
    }
}

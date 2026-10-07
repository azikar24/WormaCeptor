package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.mcp.protocol.ApiResponse
import com.azikar24.wormaceptor.mcp.protocol.CrashDto
import com.azikar24.wormaceptor.mcp.protocol.CrashSummaryDto
import com.azikar24.wormaceptor.mcp.protocol.ResponseMeta
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import com.azikar24.wormaceptor.mcp.server.serialization.toSummaryDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_LIMIT = 50
private const val DEFAULT_OFFSET = 0

internal fun Routing.crashRoutes() {
    get("/api/crashes") {
        try {
            val queryEngine = CoreHolder.queryEngine
            if (queryEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Crash engine not available"),
                )
                return@get
            }

            val limit = call.parameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT
            val offset = call.parameters["offset"]?.toIntOrNull() ?: DEFAULT_OFFSET

            val allCrashes = queryEngine.observeCrashes().first()
            val total = allCrashes.size
            val paged = allCrashes.drop(offset).take(limit)
            val dtos = paged.map { it.toSummaryDto() }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                ListSerializer(CrashSummaryDto.serializer()),
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
                ApiResponse(success = false, error = e.message ?: "Failed to fetch crashes"),
            )
        }
    }

    get("/api/crashes/{id}") {
        try {
            val queryEngine = CoreHolder.queryEngine
            if (queryEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Crash engine not available"),
                )
                return@get
            }

            val crashId = call.parameters["id"]?.toLongOrNull()
            if (crashId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse(success = false, error = "Invalid crash ID"),
                )
                return@get
            }

            val allCrashes = queryEngine.observeCrashes().first()
            val crash = allCrashes.find { it.id == crashId }
            if (crash == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse(success = false, error = "Crash not found"),
                )
                return@get
            }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                CrashDto.serializer(),
                crash.toDto(),
            )

            call.respond(ApiResponse(success = true, data = dataElement))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to fetch crash"),
            )
        }
    }
}

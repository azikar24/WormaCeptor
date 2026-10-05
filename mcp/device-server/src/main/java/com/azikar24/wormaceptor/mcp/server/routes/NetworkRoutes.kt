package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.RateLimitEngine
import com.azikar24.wormaceptor.core.engine.WebSocketMonitorEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.RateLimitUpdate
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ApiResponse
import com.azikar24.wormaceptor.mcp.server.serialization.dto.RateLimitConfigDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ResponseMeta
import com.azikar24.wormaceptor.mcp.server.serialization.dto.SetRateLimitRequestDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.WebSocketConnectionDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.WebSocketMessageDto
import com.azikar24.wormaceptor.mcp.server.serialization.resolve
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_LIMIT = 50
private const val DEFAULT_OFFSET = 0

internal fun Routing.networkRoutes() {
    get("/api/websockets/connections") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val wsEngine = koin.getOrNull<WebSocketMonitorEngine>()
            if (wsEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "WebSocket monitor not available"),
                )
                return@get
            }

            val connections = wsEngine.connections.value
            val dtos = connections.map { it.toDto() }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                ListSerializer(WebSocketConnectionDto.serializer()),
                dtos,
            )

            call.respond(
                ApiResponse(
                    success = true,
                    data = dataElement,
                    meta = ResponseMeta(total = connections.size),
                ),
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to fetch WebSocket connections"),
            )
        }
    }

    get("/api/websockets/messages") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val wsEngine = koin.getOrNull<WebSocketMonitorEngine>()
            if (wsEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "WebSocket monitor not available"),
                )
                return@get
            }

            val limit = call.parameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT
            val offset = call.parameters["offset"]?.toIntOrNull() ?: DEFAULT_OFFSET
            val connectionIdFilter = call.parameters["connection_id"]?.toLongOrNull()

            var messages = wsEngine.messages.value
            if (connectionIdFilter != null) {
                messages = messages.filter { it.connectionId == connectionIdFilter }
            }

            val total = messages.size
            val paged = messages.drop(offset).take(limit)
            val dtos = paged.map { it.toDto() }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                ListSerializer(WebSocketMessageDto.serializer()),
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
                ApiResponse(success = false, error = e.message ?: "Failed to fetch WebSocket messages"),
            )
        }
    }

    get("/api/rate-limit") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val rateLimitEngine = koin.getOrNull<RateLimitEngine>()
            if (rateLimitEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Rate limit engine not available"),
                )
                return@get
            }

            val config = rateLimitEngine.config.value
            val dto = config.toDto()

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                RateLimitConfigDto.serializer(),
                dto,
            )

            call.respond(ApiResponse(success = true, data = dataElement))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to fetch rate limit config"),
            )
        }
    }

    post("/api/rate-limit") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val rateLimitEngine = koin.getOrNull<RateLimitEngine>()
            if (rateLimitEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Rate limit engine not available"),
                )
                return@post
            }

            val request = call.receive<SetRateLimitRequestDto>()
            when (val update = request.resolve(rateLimitEngine.config.value)) {
                is RateLimitUpdate.Invalid -> {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse(success = false, error = update.message),
                    )
                    return@post
                }
                is RateLimitUpdate.Valid -> rateLimitEngine.setConfig(update.config)
            }

            val updatedDto = rateLimitEngine.config.value.toDto()
            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                RateLimitConfigDto.serializer(),
                updatedDto,
            )

            call.respond(ApiResponse(success = true, data = dataElement))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to set rate limit config"),
            )
        }
    }
}

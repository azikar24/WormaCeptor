package com.azikar24.wormaceptor.mcp.server.routes

import android.content.Context
import com.azikar24.wormaceptor.core.engine.LeakDetectionEngine
import com.azikar24.wormaceptor.core.engine.ThreadViolationEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ApiResponse
import com.azikar24.wormaceptor.mcp.server.serialization.dto.DeviceInfoDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.LeakInfoDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ResponseMeta
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ThreadViolationDto
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_LIMIT = 50
private const val DEFAULT_OFFSET = 0

internal fun Routing.diagnosticRoutes() {
    get("/api/leaks") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val leakEngine = koin.getOrNull<LeakDetectionEngine>()
            if (leakEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Leak detection engine not available"),
                )
                return@get
            }

            val limit = call.parameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT
            val offset = call.parameters["offset"]?.toIntOrNull() ?: DEFAULT_OFFSET

            val allLeaks = leakEngine.detectedLeaks.value
            val total = allLeaks.size
            val paged = allLeaks.drop(offset).take(limit)
            val dtos = paged.map { it.toDto() }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                ListSerializer(LeakInfoDto.serializer()),
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
                ApiResponse(success = false, error = e.message ?: "Failed to fetch leaks"),
            )
        }
    }

    get("/api/violations") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val violationEngine = koin.getOrNull<ThreadViolationEngine>()
            if (violationEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Thread violation engine not available"),
                )
                return@get
            }

            val limit = call.parameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT
            val offset = call.parameters["offset"]?.toIntOrNull() ?: DEFAULT_OFFSET

            val allViolations = violationEngine.violations.value
            val total = allViolations.size
            val paged = allViolations.drop(offset).take(limit)
            val dtos = paged.map { it.toDto() }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                ListSerializer(ThreadViolationDto.serializer()),
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
                ApiResponse(success = false, error = e.message ?: "Failed to fetch violations"),
            )
        }
    }

    get("/api/device-info") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val context = koin.get<Context>()
            val deviceInfo = DeviceInfoCollector.collect(context)

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                DeviceInfoDto.serializer(),
                deviceInfo.toDto(),
            )

            call.respond(ApiResponse(success = true, data = dataElement))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to fetch device info"),
            )
        }
    }
}

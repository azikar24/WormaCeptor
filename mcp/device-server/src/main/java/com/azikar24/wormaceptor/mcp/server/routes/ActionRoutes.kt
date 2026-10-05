package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.LocationSimulatorEngine
import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.core.engine.PushSimulatorEngine
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.domain.entities.MockLocation
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.domain.entities.SimulatedNotification
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ApiResponse
import com.azikar24.wormaceptor.mcp.server.serialization.dto.MockLocationDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.SimulatedNotificationDto
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import kotlin.coroutines.cancellation.CancellationException

internal fun Routing.actionRoutes() {
    post("/api/clear/transactions") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val queryEngine = koin.getOrNull<QueryEngine>()
            if (queryEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Transaction engine not available"),
                )
                return@post
            }

            queryEngine.clear()
            call.respond(ApiResponse(success = true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to clear transactions"),
            )
        }
    }

    post("/api/clear/crashes") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val queryEngine = koin.getOrNull<QueryEngine>()
            if (queryEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Crash engine not available"),
                )
                return@post
            }

            queryEngine.clearCrashes()
            call.respond(ApiResponse(success = true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to clear crashes"),
            )
        }
    }

    post("/api/clear/logs") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val logEngine = koin.getOrNull<LogCaptureEngine>()
            if (logEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Log engine not available"),
                )
                return@post
            }

            logEngine.clear()
            call.respond(ApiResponse(success = true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to clear logs"),
            )
        }
    }

    post("/api/location") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val locationEngine = koin.getOrNull<LocationSimulatorEngine>()
            if (locationEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Location simulator not available"),
                )
                return@post
            }

            val dto = call.receive<MockLocationDto>()
            val mockLocation = MockLocation(
                latitude = dto.latitude,
                longitude = dto.longitude,
                altitude = dto.altitude,
                accuracy = dto.accuracy,
                speed = dto.speed,
                bearing = dto.bearing,
                timestamp = dto.timestamp,
                name = dto.name,
            )

            val success = locationEngine.setLocation(mockLocation)
            if (success) {
                call.respond(ApiResponse(success = true))
            } else {
                val error = locationEngine.lastError.value ?: "Failed to set mock location"
                call.respond(ApiResponse(success = false, error = error))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to set location"),
            )
        }
    }

    delete("/api/location") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val locationEngine = koin.getOrNull<LocationSimulatorEngine>()
            if (locationEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Location simulator not available"),
                )
                return@delete
            }

            locationEngine.clearMockLocation()
            call.respond(ApiResponse(success = true))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to clear location"),
            )
        }
    }

    post("/api/push") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val pushEngine = koin.getOrNull<PushSimulatorEngine>()
            if (pushEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Push simulator not available"),
                )
                return@post
            }

            val dto = call.receive<SimulatedNotificationDto>()
            val priority = try {
                NotificationPriority.valueOf(dto.priority.uppercase())
            } catch (_: IllegalArgumentException) {
                NotificationPriority.DEFAULT
            }

            val notification = SimulatedNotification(
                id = dto.id,
                title = dto.title,
                body = dto.body,
                channelId = dto.channelId,
                priority = priority,
                extras = dto.extras,
                timestamp = dto.timestamp,
            )

            val notificationId = pushEngine.sendNotification(notification)
            call.respond(
                ApiResponse(
                    success = true,
                    data = kotlinx.serialization.json.JsonPrimitive(notificationId),
                ),
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to send notification"),
            )
        }
    }
}

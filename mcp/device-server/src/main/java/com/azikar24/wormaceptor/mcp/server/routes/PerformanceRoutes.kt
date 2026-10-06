package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import com.azikar24.wormaceptor.core.engine.FpsMonitorEngine
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ApiResponse
import com.azikar24.wormaceptor.mcp.server.serialization.dto.CpuInfoDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.FpsInfoDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.MemoryInfoDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.PerformanceSnapshotDto
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.coroutines.cancellation.CancellationException

private const val FpsOffMessage = "FPS monitoring is off. It only samples while running: open the FPS tool or " +
    "the performance overlay in WormaCeptor, then ask again."

// Monitors only sample while their screen or the overlay runs; a one-off sample beats returning zeros.
private fun CpuMonitorEngine.currentOrSample() = if (isMonitoring.value) currentCpu.value else takeSample()

private fun MemoryMonitorEngine.currentOrSample() = if (isMonitoring.value) currentMemory.value else takeSample()

internal fun Routing.performanceRoutes() {
    get("/api/cpu") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val cpuEngine = koin.getOrNull<CpuMonitorEngine>()
            if (cpuEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "CPU engine not available"),
                )
                return@get
            }

            val includeHistory = call.parameters["include_history"]?.toBooleanStrictOrNull() == true
            val json = JsonConfig.instance

            val currentDto = cpuEngine.currentOrSample().toDto()
            val currentElement = json.encodeToJsonElement(CpuInfoDto.serializer(), currentDto)

            val dataElement: JsonElement = if (includeHistory) {
                val historyDtos = cpuEngine.cpuHistory.value.map { it.toDto() }
                val historyElement = json.encodeToJsonElement(
                    ListSerializer(CpuInfoDto.serializer()),
                    historyDtos,
                )
                JsonObject(
                    mapOf(
                        "current" to currentElement,
                        "history" to historyElement,
                        "isMonitoring" to JsonPrimitive(cpuEngine.isMonitoring.value),
                    ),
                )
            } else {
                currentElement
            }

            call.respond(ApiResponse(success = true, data = dataElement))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to fetch CPU info"),
            )
        }
    }

    get("/api/memory") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val memoryEngine = koin.getOrNull<MemoryMonitorEngine>()
            if (memoryEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Memory engine not available"),
                )
                return@get
            }

            val includeHistory = call.parameters["include_history"]?.toBooleanStrictOrNull() == true
            val json = JsonConfig.instance

            val currentDto = memoryEngine.currentOrSample().toDto()
            val currentElement = json.encodeToJsonElement(MemoryInfoDto.serializer(), currentDto)

            val dataElement: JsonElement = if (includeHistory) {
                val historyDtos = memoryEngine.memoryHistory.value.map { it.toDto() }
                val historyElement = json.encodeToJsonElement(
                    ListSerializer(MemoryInfoDto.serializer()),
                    historyDtos,
                )
                JsonObject(
                    mapOf(
                        "current" to currentElement,
                        "history" to historyElement,
                        "isMonitoring" to JsonPrimitive(memoryEngine.isMonitoring.value),
                    ),
                )
            } else {
                currentElement
            }

            call.respond(ApiResponse(success = true, data = dataElement))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to fetch memory info"),
            )
        }
    }

    get("/api/fps") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val fpsEngine = koin.getOrNull<FpsMonitorEngine>()
            if (fpsEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "FPS engine not available"),
                )
                return@get
            }

            if (!fpsEngine.isRunning.value) {
                call.respond(ApiResponse(success = false, error = FpsOffMessage))
                return@get
            }

            val includeHistory = call.parameters["include_history"]?.toBooleanStrictOrNull() == true
            val json = JsonConfig.instance

            val currentDto = fpsEngine.currentFpsInfo.value.toDto()
            val currentElement = json.encodeToJsonElement(FpsInfoDto.serializer(), currentDto)

            val dataElement: JsonElement = if (includeHistory) {
                val historyDtos = fpsEngine.fpsHistory.value.map { it.toDto() }
                val historyElement = json.encodeToJsonElement(
                    ListSerializer(FpsInfoDto.serializer()),
                    historyDtos,
                )
                JsonObject(
                    mapOf(
                        "current" to currentElement,
                        "history" to historyElement,
                        "isRunning" to JsonPrimitive(fpsEngine.isRunning.value),
                    ),
                )
            } else {
                currentElement
            }

            call.respond(ApiResponse(success = true, data = dataElement))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to fetch FPS info"),
            )
        }
    }

    get("/api/performance") {
        try {
            val koin = WormaCeptorKoin.getKoin()
            val cpuEngine = koin.getOrNull<CpuMonitorEngine>()
            val memoryEngine = koin.getOrNull<MemoryMonitorEngine>()
            val fpsEngine = koin.getOrNull<FpsMonitorEngine>()

            if (cpuEngine == null || memoryEngine == null || fpsEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Performance engines not fully available"),
                )
                return@get
            }

            val snapshot = PerformanceSnapshotDto(
                cpu = cpuEngine.currentOrSample().toDto(),
                memory = memoryEngine.currentOrSample().toDto(),
                fps = fpsEngine.currentFpsInfo.value.toDto(),
                fpsMonitoring = fpsEngine.isRunning.value,
            )

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                PerformanceSnapshotDto.serializer(),
                snapshot,
            )

            call.respond(ApiResponse(success = true, data = dataElement))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to fetch performance snapshot"),
            )
        }
    }
}

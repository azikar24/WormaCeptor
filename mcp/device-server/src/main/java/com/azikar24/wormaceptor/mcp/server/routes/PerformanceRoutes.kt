package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import com.azikar24.wormaceptor.core.engine.FpsMonitorEngine
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.mcp.protocol.ApiResponse
import com.azikar24.wormaceptor.mcp.protocol.CpuHistoryDto
import com.azikar24.wormaceptor.mcp.protocol.CpuInfoDto
import com.azikar24.wormaceptor.mcp.protocol.FpsHistoryDto
import com.azikar24.wormaceptor.mcp.protocol.FpsInfoDto
import com.azikar24.wormaceptor.mcp.protocol.MemoryHistoryDto
import com.azikar24.wormaceptor.mcp.protocol.MemoryInfoDto
import com.azikar24.wormaceptor.mcp.protocol.PerformanceSnapshotDto
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.serialization.json.JsonElement
import kotlin.coroutines.cancellation.CancellationException

private const val FpsOffMessage = "FPS monitoring is off. It only samples while running: call set_monitoring " +
    "with target=fps and enabled=true (or open the FPS tool or performance overlay), then ask again."

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

            val dataElement: JsonElement = if (includeHistory) {
                json.encodeToJsonElement(
                    CpuHistoryDto.serializer(),
                    CpuHistoryDto(
                        current = currentDto,
                        history = cpuEngine.cpuHistory.value.map { it.toDto() },
                        isMonitoring = cpuEngine.isMonitoring.value,
                    ),
                )
            } else {
                json.encodeToJsonElement(CpuInfoDto.serializer(), currentDto)
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

            val dataElement: JsonElement = if (includeHistory) {
                json.encodeToJsonElement(
                    MemoryHistoryDto.serializer(),
                    MemoryHistoryDto(
                        current = currentDto,
                        history = memoryEngine.memoryHistory.value.map { it.toDto() },
                        isMonitoring = memoryEngine.isMonitoring.value,
                    ),
                )
            } else {
                json.encodeToJsonElement(MemoryInfoDto.serializer(), currentDto)
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

            val dataElement: JsonElement = if (includeHistory) {
                json.encodeToJsonElement(
                    FpsHistoryDto.serializer(),
                    FpsHistoryDto(
                        current = currentDto,
                        history = fpsEngine.fpsHistory.value.map { it.toDto() },
                        isRunning = fpsEngine.isRunning.value,
                    ),
                )
            } else {
                json.encodeToJsonElement(FpsInfoDto.serializer(), currentDto)
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

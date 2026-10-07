package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import com.azikar24.wormaceptor.core.engine.FpsMonitorEngine
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayState
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.mcp.protocol.ApiResponse
import com.azikar24.wormaceptor.mcp.protocol.MonitoringStateDto
import com.azikar24.wormaceptor.mcp.protocol.SetMonitoringRequestDto
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.Koin

internal enum class MonitorTarget {
    CPU,
    MEMORY,
    FPS,
    ;

    companion object {
        fun from(name: String): MonitorTarget? = entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }
}

internal enum class MonitoringChange(val note: String?) {
    Start(null),
    Stop(null),
    AlreadyRunning("already running"),
    AlreadyStopped("already stopped"),
    KeptForOverlay("kept running: the performance overlay is showing it"),
}

/** Stopping never takes a metric away from the overlay; feature screens restart their monitor when reopened. */
internal fun monitoringChange(
    enabled: Boolean,
    running: Boolean,
    overlayUses: Boolean,
): MonitoringChange = when {
    enabled && running -> MonitoringChange.AlreadyRunning
    enabled -> MonitoringChange.Start
    !running -> MonitoringChange.AlreadyStopped
    overlayUses -> MonitoringChange.KeptForOverlay
    else -> MonitoringChange.Stop
}

private data class Monitor(
    val isRunning: () -> Boolean,
    val start: () -> Unit,
    val stop: () -> Unit,
    val overlayUses: (PerformanceOverlayState) -> Boolean,
)

private fun Koin.monitor(target: MonitorTarget): Monitor? = when (target) {
    MonitorTarget.CPU -> getOrNull<CpuMonitorEngine>()?.let {
        Monitor({ it.isMonitoring.value }, it::start, it::stop) { s -> s.cpuEnabled }
    }
    MonitorTarget.MEMORY -> getOrNull<MemoryMonitorEngine>()?.let {
        Monitor({ it.isMonitoring.value }, it::start, it::stop) { s -> s.memoryEnabled }
    }
    MonitorTarget.FPS -> getOrNull<FpsMonitorEngine>()?.let {
        Monitor({ it.isRunning.value }, it::start, it::stop) { s -> s.fpsEnabled }
    }
}

internal fun Routing.monitoringRoutes() {
    post("/api/monitoring") {
        val request = call.receive<SetMonitoringRequestDto>()
        val target = MonitorTarget.from(request.target)
        if (target == null) {
            val valid = MonitorTarget.entries.joinToString { it.name.lowercase() }
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse(success = false, error = "Unknown target '${request.target}'. Valid targets: $valid"),
            )
            return@post
        }
        val koin = WormaCeptorKoin.getKoin()
        val monitor = koin.monitor(target)
        if (monitor == null) {
            call.respond(ApiResponse(success = false, error = "${target.name} engine not available"))
            return@post
        }
        val overlay = koin.getOrNull<PerformanceOverlayEngine>()
        val overlayUses = overlay?.isVisible?.value == true && monitor.overlayUses(overlay.state.value)
        val change = monitoringChange(request.enabled, monitor.isRunning(), overlayUses)
        // The FPS monitor posts Choreographer callbacks, which must start on the main thread.
        withContext(Dispatchers.Main) {
            when (change) {
                MonitoringChange.Start -> monitor.start()
                MonitoringChange.Stop -> monitor.stop()
                else -> Unit
            }
        }
        val state = MonitoringStateDto(target.name.lowercase(), monitor.isRunning(), change.note)
        val data = JsonConfig.instance.encodeToJsonElement(MonitoringStateDto.serializer(), state)
        call.respond(ApiResponse(success = true, data = data))
    }
}

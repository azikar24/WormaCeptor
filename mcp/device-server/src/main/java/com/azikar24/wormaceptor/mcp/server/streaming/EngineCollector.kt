package com.azikar24.wormaceptor.mcp.server.streaming

import android.util.Log
import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import com.azikar24.wormaceptor.core.engine.FpsMonitorEngine
import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.coroutines.cancellation.CancellationException

internal class EngineCollector(
    private val eventManager: EventStreamManager,
    private val scope: CoroutineScope,
) {

    private val json = JsonConfig.instance

    fun startCollecting() {
        val koin = try {
            WormaCeptorKoin.getKoin()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.w(TAG, "Koin not available, skipping engine collection", e)
            return
        }

        collectEngine<CpuMonitorEngine>(koin, "cpu") { engine ->
            engine.currentCpu
                .collect { cpu ->
                    broadcast("cpu", "update", json.encodeToJsonElement(cpu.toDto()))
                }
        }

        collectEngine<MemoryMonitorEngine>(koin, "memory") { engine ->
            engine.currentMemory
                .collect { memory ->
                    broadcast("memory", "update", json.encodeToJsonElement(memory.toDto()))
                }
        }

        collectEngine<FpsMonitorEngine>(koin, "fps") { engine ->
            engine.currentFpsInfo
                .collect { fps ->
                    broadcast("fps", "update", json.encodeToJsonElement(fps.toDto()))
                }
        }

        collectEngine<LogCaptureEngine>(koin, "logs") { engine ->
            var lastSize = 0
            engine.logs
                .collect { logs ->
                    val newLogs = logs.drop(lastSize)
                    lastSize = logs.size
                    newLogs.forEach { log ->
                        broadcast("logs", "new", json.encodeToJsonElement(log.toDto()))
                    }
                }
        }
    }

    private inline fun <reified T : Any> collectEngine(
        koin: org.koin.core.Koin,
        channel: String,
        crossinline collector: suspend (T) -> Unit,
    ) {
        val engine: T? = try {
            koin.getOrNull()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.w(TAG, "Engine not available for channel: $channel", e)
            null
        }
        if (engine != null) {
            scope.launch {
                try {
                    collector(engine)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.w(TAG, "Error collecting $channel", e)
                }
            }
        }
    }

    private suspend fun broadcast(
        channel: String,
        event: String,
        data: JsonElement,
    ) {
        eventManager.broadcast(
            channel,
            StreamEvent(
                channel = channel,
                event = event,
                timestamp = System.currentTimeMillis(),
                data = data,
            ),
        )
    }

    companion object {
        private const val TAG = "EngineCollector"
    }
}

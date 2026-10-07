package com.azikar24.wormaceptor.mcp.server.streaming

import android.util.Log
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import com.azikar24.wormaceptor.core.engine.FpsMonitorEngine
import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import com.azikar24.wormaceptor.mcp.server.serialization.toSummaryDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.koin.core.Koin
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

/** Feeds engine updates into [eventManager]. All collection stops when [scope] is cancelled. */
internal class EngineCollector(
    private val eventManager: EventStreamManager,
    private val scope: CoroutineScope,
) {

    private val json = JsonConfig.instance

    fun startCollecting() {
        scope.launch {
            // The server starts from a ContentProvider, before the host calls WormaCeptor.init().
            while (!WormaCeptorKoin.isInitialized || CoreHolder.queryEngine == null) {
                delay(INIT_POLL_INTERVAL_MS)
            }
            collectEngines(WormaCeptorKoin.getKoin())
            CoreHolder.queryEngine?.let(::collectQueryEngine)
        }
    }

    private fun collectEngines(koin: Koin) {
        collectEngine<CpuMonitorEngine>(koin, "cpu") { engine ->
            engine.currentCpu.collect { broadcast("cpu", "update", json.encodeToJsonElement(it.toDto())) }
        }
        collectEngine<MemoryMonitorEngine>(koin, "memory") { engine ->
            engine.currentMemory.collect { broadcast("memory", "update", json.encodeToJsonElement(it.toDto())) }
        }
        collectEngine<FpsMonitorEngine>(koin, "fps") { engine ->
            engine.currentFpsInfo.collect { broadcast("fps", "update", json.encodeToJsonElement(it.toDto())) }
        }
        collectEngine<LogCaptureEngine>(koin, "logs") { engine ->
            // Ids are monotonic; the buffer is circular, so its size can't be used to find new entries.
            var lastId = engine.logs.value.lastOrNull()?.id ?: -1L
            engine.logs.collect { logs ->
                logs.filter { it.id > lastId }.forEach { log ->
                    broadcast("logs", "new", json.encodeToJsonElement(log.toDto()))
                }
                logs.lastOrNull()?.let { lastId = maxOf(lastId, it.id) }
            }
        }
    }

    private fun collectQueryEngine(queryEngine: QueryEngine) {
        launchCollector("transactions") {
            val seen = HashSet<UUID>()
            var first = true
            queryEngine.observeTransactions().collect { transactions ->
                transactions.filter { seen.add(it.id) && !first }.forEach {
                    broadcast("transactions", "new", json.encodeToJsonElement(it.toDto()))
                }
                first = false
            }
        }
        launchCollector("crashes") {
            val seen = HashSet<Long>()
            var first = true
            queryEngine.observeCrashes().collect { crashes ->
                crashes.filter { seen.add(it.id) && !first }.forEach {
                    broadcast("crashes", "new", json.encodeToJsonElement(it.toSummaryDto()))
                }
                first = false
            }
        }
    }

    private inline fun <reified T : Any> collectEngine(
        koin: Koin,
        channel: String,
        crossinline collector: suspend (T) -> Unit,
    ) {
        val engine = koin.getOrNull<T>()
        if (engine == null) {
            Log.w(TAG, "Engine not available for channel: $channel")
            return
        }
        launchCollector(channel) { collector(engine) }
    }

    private fun launchCollector(
        channel: String,
        block: suspend () -> Unit,
    ) {
        scope.launch {
            try {
                block()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "Error collecting $channel", e)
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
            StreamEvent(channel = channel, event = event, timestamp = System.currentTimeMillis(), data = data),
        )
    }

    companion object {
        private const val TAG = "EngineCollector"
        private const val INIT_POLL_INTERVAL_MS = 200L
    }
}

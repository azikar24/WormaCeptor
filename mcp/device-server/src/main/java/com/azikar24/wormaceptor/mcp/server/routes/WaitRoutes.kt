package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.mcp.protocol.ApiResponse
import com.azikar24.wormaceptor.mcp.protocol.WaitForCrashDto
import com.azikar24.wormaceptor.mcp.protocol.WaitForTransactionDto
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

private const val DefaultWaitSeconds = 30
private const val MaxWaitSeconds = 120
private const val MillisPerSecond = 1000L

/** Filters for `wait_for_transaction`; null fields match anything. */
internal data class TransactionFilter(
    val urlContains: String? = null,
    val method: String? = null,
    val status: Int? = null,
)

/** The earliest finished transaction in [items] that isn't in [baseline] and passes [filter]. */
internal fun pickNewTransaction(
    items: List<TransactionSummary>,
    baseline: Set<UUID>,
    filter: TransactionFilter,
): TransactionSummary? = items
    .filter { it.id !in baseline && it.status != TransactionStatus.ACTIVE && it.matches(filter) }
    .minByOrNull { it.timestamp }

/** The earliest crash in [items] that isn't in [baseline]. */
internal fun pickNewCrash(
    items: List<Crash>,
    baseline: Set<Long>,
): Crash? = items.filter { it.id !in baseline }.minByOrNull { it.timestamp }

/**
 * Takes the current list as the baseline, then waits up to [timeoutMs] for an emission where [pick] finds
 * an item outside it. Items that already existed never match, even if they change later.
 */
internal suspend fun <T, K> Flow<List<T>>.awaitNew(
    timeoutMs: Long,
    key: (T) -> K,
    pick: (List<T>, Set<K>) -> T?,
): T? {
    val baseline = first().mapTo(HashSet(), key)
    return withTimeoutOrNull(timeoutMs) { mapNotNull { pick(it, baseline) }.first() }
}

private fun TransactionSummary.matches(filter: TransactionFilter): Boolean {
    val fullUrl = url.ifEmpty { host + path }
    return (filter.urlContains == null || fullUrl.contains(filter.urlContains, ignoreCase = true)) &&
        (filter.method == null || method.equals(filter.method, ignoreCase = true)) &&
        (filter.status == null || code == filter.status)
}

private fun ApplicationCall.waitTimeoutMs(): Long =
    (parameters["timeout_s"]?.toIntOrNull() ?: DefaultWaitSeconds).coerceIn(1, MaxWaitSeconds) * MillisPerSecond

internal fun Routing.waitRoutes() {
    get("/api/wait/transaction") {
        val queryEngine = CoreHolder.queryEngine
        if (queryEngine == null) {
            call.respond(ApiResponse(success = false, error = "Transaction engine not available"))
            return@get
        }
        val filter = TransactionFilter(
            urlContains = call.parameters["url_contains"]?.takeIf { it.isNotBlank() },
            method = call.parameters["method"]?.takeIf { it.isNotBlank() },
            status = call.parameters["status"]?.toIntOrNull(),
        )
        val startedAt = System.currentTimeMillis()
        val match = queryEngine.observeTransactions().awaitNew(call.waitTimeoutMs(), key = { it.id }) { items, seen ->
            pickNewTransaction(items, seen, filter)
        }
        val result = WaitForTransactionDto(match?.toDto(), System.currentTimeMillis() - startedAt)
        val data = JsonConfig.instance.encodeToJsonElement(WaitForTransactionDto.serializer(), result)
        call.respond(ApiResponse(success = true, data = data))
    }

    get("/api/wait/crash") {
        val queryEngine = CoreHolder.queryEngine
        if (queryEngine == null) {
            call.respond(ApiResponse(success = false, error = "Crash engine not available"))
            return@get
        }
        val startedAt = System.currentTimeMillis()
        val crash = queryEngine.observeCrashes().awaitNew(call.waitTimeoutMs(), key = { it.id }, pick = ::pickNewCrash)
        val result = WaitForCrashDto(crash?.toDto(), System.currentTimeMillis() - startedAt)
        val data = JsonConfig.instance.encodeToJsonElement(WaitForCrashDto.serializer(), result)
        call.respond(ApiResponse(success = true, data = data))
    }
}

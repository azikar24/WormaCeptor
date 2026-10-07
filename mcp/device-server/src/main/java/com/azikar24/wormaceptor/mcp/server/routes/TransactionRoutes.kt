package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.domain.entities.BlobID
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.mcp.protocol.ApiResponse
import com.azikar24.wormaceptor.mcp.protocol.BodyDto
import com.azikar24.wormaceptor.mcp.protocol.ResponseMeta
import com.azikar24.wormaceptor.mcp.protocol.TransactionDetailDto
import com.azikar24.wormaceptor.mcp.protocol.TransactionSummaryDto
import com.azikar24.wormaceptor.mcp.server.ServerConfig
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.toDetailDto
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_LIMIT = 50
private const val DEFAULT_OFFSET = 0

/** Case-insensitive substring match on URL, method, or status code, as documented for `list_transactions`. */
internal fun TransactionSummary.matchesQuery(query: String): Boolean {
    val url = url.ifEmpty { host + path }
    return url.contains(query, ignoreCase = true) ||
        method.contains(query, ignoreCase = true) ||
        code?.toString()?.contains(query) == true
}

internal fun Routing.transactionRoutes(maxBodySize: Long = ServerConfig.DEFAULT_MAX_BODY_SIZE) {
    get("/api/transactions") {
        try {
            val queryEngine = CoreHolder.queryEngine
            if (queryEngine == null) {
                call.respond(ApiResponse(success = false, error = "Transaction engine not available"))
                return@get
            }

            val limit = call.parameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT
            val offset = call.parameters["offset"]?.toIntOrNull() ?: DEFAULT_OFFSET
            val query = call.parameters["query"]?.trim().orEmpty()

            val matching = queryEngine.observeTransactions().first()
                .let { all -> if (query.isEmpty()) all else all.filter { it.matchesQuery(query) } }
            val dtos = matching.drop(offset).take(limit).map { it.toDto() }

            val dataElement: JsonElement = JsonConfig.instance.encodeToJsonElement(
                ListSerializer(TransactionSummaryDto.serializer()),
                dtos,
            )

            call.respond(
                ApiResponse(
                    success = true,
                    data = dataElement,
                    meta = ResponseMeta(total = matching.size, limit = limit, offset = offset),
                ),
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to fetch transactions"),
            )
        }
    }

    get("/api/transactions/{id}") {
        try {
            val (_, transaction) = call.findTransaction() ?: return@get
            val dataElement: JsonElement = JsonConfig.instance.encodeToJsonElement(
                TransactionDetailDto.serializer(),
                transaction.toDetailDto(),
            )
            call.respond(ApiResponse(success = true, data = dataElement))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to fetch transaction"),
            )
        }
    }

    get("/api/transactions/{id}/request-body") {
        call.respondBody(maxBodySize) { it.request.bodyRef to it.request.headers }
    }

    get("/api/transactions/{id}/response-body") {
        call.respondBody(maxBodySize) { it.response?.bodyRef to it.response?.headers.orEmpty() }
    }
}

/** Responds with 400/404/error itself and returns null when the transaction can't be loaded. */
private suspend fun ApplicationCall.findTransaction(): Pair<QueryEngine, NetworkTransaction>? {
    val queryEngine = CoreHolder.queryEngine
    if (queryEngine == null) {
        respond(ApiResponse(success = false, error = "Transaction engine not available"))
        return null
    }
    val uuid = parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }
    if (uuid == null) {
        respond(HttpStatusCode.BadRequest, ApiResponse(success = false, error = "Invalid UUID"))
        return null
    }
    val transaction = queryEngine.getDetails(uuid)
    if (transaction == null) {
        respond(HttpStatusCode.NotFound, ApiResponse(success = false, error = "Transaction not found"))
        return null
    }
    return queryEngine to transaction
}

private suspend fun ApplicationCall.respondBody(
    maxBodySize: Long,
    select: (NetworkTransaction) -> Pair<BlobID?, Map<String, List<String>>>,
) {
    try {
        val (queryEngine, transaction) = findTransaction() ?: return
        val (bodyRef, headers) = select(transaction)
        val body = bodyRef?.let { queryEngine.getBody(it) }.orEmpty()
        val contentType = headers.entries
            .firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }
            ?.value?.firstOrNull()
        val dto = BodyDto(
            body = body.take(maxBodySize.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()),
            contentType = contentType,
            truncated = body.length > maxBodySize,
            totalSize = body.length.toLong(),
        )
        respond(ApiResponse(success = true, data = JsonConfig.instance.encodeToJsonElement(BodyDto.serializer(), dto)))
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        respond(ApiResponse(success = false, error = e.message ?: "Failed to read body"))
    }
}

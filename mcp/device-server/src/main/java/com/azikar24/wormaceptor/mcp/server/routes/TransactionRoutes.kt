package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.mcp.server.ServerConfig
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ApiResponse
import com.azikar24.wormaceptor.mcp.server.serialization.dto.ResponseMeta
import com.azikar24.wormaceptor.mcp.server.serialization.dto.TransactionDetailDto
import com.azikar24.wormaceptor.mcp.server.serialization.dto.TransactionSummaryDto
import com.azikar24.wormaceptor.mcp.server.serialization.toDetailDto
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.flow.first
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonElement
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_LIMIT = 50
private const val DEFAULT_OFFSET = 0

internal fun Routing.transactionRoutes(maxBodySize: Long = ServerConfig.DEFAULT_MAX_BODY_SIZE) {
    get("/api/transactions") {
        try {
            val queryEngine = CoreHolder.queryEngine
            if (queryEngine == null) {
                call.respond(
                    ApiResponse(
                        success = false,
                        error = "Transaction engine not available",
                    ),
                )
                return@get
            }

            val limit = call.parameters["limit"]?.toIntOrNull() ?: DEFAULT_LIMIT
            val offset = call.parameters["offset"]?.toIntOrNull() ?: DEFAULT_OFFSET

            val allTransactions = queryEngine.observeTransactions().first()
            val total = allTransactions.size
            val paged = allTransactions.drop(offset).take(limit)
            val dtos = paged.map { it.toDto() }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
                ListSerializer(TransactionSummaryDto.serializer()),
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
                ApiResponse(success = false, error = e.message ?: "Failed to fetch transactions"),
            )
        }
    }

    get("/api/transactions/{id}") {
        try {
            val queryEngine = CoreHolder.queryEngine
            if (queryEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Transaction engine not available"),
                )
                return@get
            }

            val idStr = call.parameters["id"]
            if (idStr == null) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(success = false, error = "Missing id"))
                return@get
            }

            val uuid = try {
                UUID.fromString(idStr)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(success = false, error = "Invalid UUID"))
                return@get
            }

            val transaction = queryEngine.getDetails(uuid)
            if (transaction == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse(success = false, error = "Transaction not found"),
                )
                return@get
            }

            val json = JsonConfig.instance
            val dataElement: JsonElement = json.encodeToJsonElement(
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
        try {
            val queryEngine = CoreHolder.queryEngine
            if (queryEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Transaction engine not available"),
                )
                return@get
            }

            val idStr = call.parameters["id"]
            val uuid = try {
                UUID.fromString(idStr)
            } catch (_: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(success = false, error = "Invalid UUID"))
                return@get
            }

            val transaction = queryEngine.getDetails(uuid)
            if (transaction == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse(success = false, error = "Transaction not found"),
                )
                return@get
            }

            val bodyRef = transaction.request.bodyRef
            if (bodyRef == null) {
                call.respondText("", ContentType.Text.Plain)
                return@get
            }

            val body = queryEngine.getBody(bodyRef) ?: ""
            val truncated = body.length > maxBodySize
            val content = if (truncated) body.take(maxBodySize.toInt()) else body
            call.respondText(content, ContentType.Text.Plain)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to read request body"),
            )
        }
    }

    get("/api/transactions/{id}/response-body") {
        try {
            val queryEngine = CoreHolder.queryEngine
            if (queryEngine == null) {
                call.respond(
                    ApiResponse(success = false, error = "Transaction engine not available"),
                )
                return@get
            }

            val idStr = call.parameters["id"]
            val uuid = try {
                UUID.fromString(idStr)
            } catch (_: Exception) {
                call.respond(HttpStatusCode.BadRequest, ApiResponse(success = false, error = "Invalid UUID"))
                return@get
            }

            val transaction = queryEngine.getDetails(uuid)
            if (transaction == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse(success = false, error = "Transaction not found"),
                )
                return@get
            }

            val bodyRef = transaction.response?.bodyRef
            if (bodyRef == null) {
                call.respondText("", ContentType.Text.Plain)
                return@get
            }

            val body = queryEngine.getBody(bodyRef) ?: ""
            val truncated = body.length > maxBodySize
            val content = if (truncated) body.take(maxBodySize.toInt()) else body
            call.respondText(content, ContentType.Text.Plain)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            call.respond(
                ApiResponse(success = false, error = e.message ?: "Failed to read response body"),
            )
        }
    }
}

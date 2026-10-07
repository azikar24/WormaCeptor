package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.CaptureEngine
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.mcp.protocol.ApiResponse
import com.azikar24.wormaceptor.mcp.protocol.CurlDto
import com.azikar24.wormaceptor.mcp.protocol.ReplayRequestDto
import com.azikar24.wormaceptor.mcp.protocol.ReplayResultDto
import com.azikar24.wormaceptor.mcp.server.serialization.JsonConfig
import com.azikar24.wormaceptor.mcp.server.serialization.decodeTextBody
import com.azikar24.wormaceptor.mcp.server.serialization.toDto
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.UUID

/** Same default cap as the interceptor's `maxContentLength`. */
private const val MaxCapturedBodyBytes = 250_000L

/** Recomputed by curl/OkHttp or wrong once the URL or body changes. */
private val DroppedHeaders = setOf("content-length", "host", "connection", "accept-encoding")
private val NoBodyMethods = setOf("GET", "HEAD")
private val BodyRequiredMethods = setOf("POST", "PUT", "PATCH")

/** No interceptors: a replay skips mock rules and throttling and reaches the real server. */
private val replayClient by lazy { OkHttpClient() }

/** A request to send again. [body] is null for none. */
internal data class ReplayPlan(
    val method: String,
    val url: String,
    val headers: Map<String, List<String>>,
    val body: ByteArray?,
)

/** POSIX shell single-quoting: safe for any content, including quotes and newlines. */
internal fun shellQuote(value: String): String = "'" + value.replace("'", "'\\''") + "'"

/** A cURL command for the request. Content-Length and friends are left for curl to compute. */
internal fun buildCurl(
    method: String,
    url: String,
    headers: Map<String, List<String>>,
    body: String?,
): String {
    val parts = buildList {
        add("curl")
        when (method.uppercase()) {
            "GET" -> Unit
            "HEAD" -> add("--head")
            else -> add("-X ${method.uppercase()}")
        }
        add(shellQuote(url))
        headers.filterKeys { it.lowercase() !in DroppedHeaders }.forEach { (name, values) ->
            values.forEach { add("-H ${shellQuote("$name: $it")}") }
        }
        body?.let { add("--data-binary ${shellQuote(it)}") }
    }
    return parts.joinToString(" \\\n  ")
}

/** The stored request with [overrides] applied; override headers replace stored ones of the same name. */
internal fun buildReplayPlan(
    transaction: NetworkTransaction,
    overrides: ReplayRequestDto,
    storedBody: ByteArray?,
): ReplayPlan {
    val request = transaction.request
    val added = overrides.headers.orEmpty()
    val skipped = DroppedHeaders + added.keys.map { it.lowercase() }
    val headers = request.headers.filterKeys { it.lowercase() !in skipped } + added.mapValues { listOf(it.value) }
    return ReplayPlan(
        method = request.method.uppercase(),
        url = overrides.url?.takeIf { it.isNotBlank() } ?: request.url,
        headers = headers,
        body = overrides.body?.toByteArray() ?: storedBody,
    )
}

/** Builds the OkHttp request; throws [IllegalArgumentException] for a bad URL or header. */
internal fun ReplayPlan.toOkHttpRequest(): Request {
    val contentType = headers.entries.firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }
        ?.value?.firstOrNull()?.toMediaTypeOrNull()
    val requestBody = when (method) {
        in NoBodyMethods -> null
        in BodyRequiredMethods -> (body ?: ByteArray(0)).toRequestBody(contentType)
        else -> body?.toRequestBody(contentType)
    }
    return Request.Builder().url(url).method(method, requestBody).apply {
        headers.forEach { (name, values) -> values.forEach { addHeader(name, it) } }
    }.build()
}

internal fun Routing.replayRoutes() {
    get("/api/transactions/{id}/curl") { call.exportCurl() }
    post("/api/transactions/{id}/replay") { call.replayTransaction() }
}

/** Responds with an error itself and returns null when the transaction can't be loaded. */
private suspend fun ApplicationCall.loadTransaction(): NetworkTransaction? {
    val queryEngine = CoreHolder.queryEngine
    if (queryEngine == null) {
        respond(ApiResponse(success = false, error = "Transaction engine not available"))
        return null
    }
    val transaction = parameters["id"]
        ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        ?.let { queryEngine.getDetails(it) }
    if (transaction == null) {
        respond(HttpStatusCode.NotFound, ApiResponse(success = false, error = "Transaction not found"))
    }
    return transaction
}

private suspend fun ApplicationCall.exportCurl() {
    val transaction = loadTransaction() ?: return
    val request = transaction.request
    val bytes = request.bodyRef?.let { CoreHolder.queryEngine?.getBodyBytes(it) }?.takeIf { it.isNotEmpty() }
    val contentType = request.headers.entries
        .firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }?.value?.firstOrNull()
    val text = bytes?.let { decodeTextBody(it, contentType) }
    // toDto() applies the server's header redaction, so the command never carries the stored secrets.
    val headers = request.toDto().headers
    val dto = CurlDto(
        command = buildCurl(request.method, request.url, headers, text),
        redactedHeaders = headers.filter { (name, values) -> values != request.headers[name] }.keys.toList(),
        bodyOmitted = bytes != null && text == null,
    )
    respond(ApiResponse(success = true, data = JsonConfig.instance.encodeToJsonElement(CurlDto.serializer(), dto)))
}

private suspend fun ApplicationCall.replayTransaction() {
    val overrides = receive<ReplayRequestDto>()
    val transaction = loadTransaction() ?: return
    val capture = CoreHolder.captureEngine
        ?: return respond(ApiResponse(success = false, error = "Capture engine not available"))
    val storedBody = transaction.request.bodyRef?.let { CoreHolder.queryEngine?.getBodyBytes(it) }
    val plan = buildReplayPlan(transaction, overrides, storedBody)
    val okRequest = try {
        plan.toOkHttpRequest()
    } catch (e: IllegalArgumentException) {
        respond(HttpStatusCode.BadRequest, ApiResponse(success = false, error = "Invalid replay: ${e.message}"))
        return
    }
    val result = withContext(Dispatchers.IO) { sendReplay(plan, okRequest, capture) }
    val data = JsonConfig.instance.encodeToJsonElement(ReplayResultDto.serializer(), result)
    respond(ApiResponse(success = true, data = data))
}

/** Sends [okRequest] and records it through [capture] as a new transaction, success or failure. */
internal suspend fun sendReplay(
    plan: ReplayPlan,
    okRequest: Request,
    capture: CaptureEngine,
    client: OkHttpClient = replayClient,
): ReplayResultDto {
    val id = capture.startTransaction(
        url = plan.url,
        method = plan.method,
        headers = plan.headers,
        bodyStream = plan.body?.inputStream(),
        bodySize = plan.body?.size?.toLong() ?: 0L,
    )
    val startedAt = System.currentTimeMillis()
    return try {
        client.newCall(okRequest).execute().use { response ->
            val durationMs = System.currentTimeMillis() - startedAt
            val bytes = response.peekBody(MaxCapturedBodyBytes).bytes()
            capture.completeTransaction(
                id = id,
                code = response.code,
                message = response.message,
                headers = response.headers.toMultimap(),
                bodyStream = bytes.inputStream(),
                bodySize = bytes.size.toLong(),
                protocol = response.protocol.toString(),
                tlsVersion = response.handshake?.tlsVersion?.javaName,
                durationMs = durationMs,
            )
            ReplayResultDto(id.toString(), plan.method, plan.url, response.code, response.message, durationMs, null)
        }
    } catch (e: IOException) {
        val durationMs = System.currentTimeMillis() - startedAt
        capture.completeTransaction(
            id = id,
            code = 0,
            message = "FAILED",
            headers = emptyMap(),
            bodyStream = null,
            error = e.toString(),
            durationMs = durationMs,
        )
        ReplayResultDto(id.toString(), plan.method, plan.url, null, null, durationMs, e.toString())
    }
}

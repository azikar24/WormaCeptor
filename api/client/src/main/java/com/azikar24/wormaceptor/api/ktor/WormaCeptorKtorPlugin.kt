@file:Suppress("TooGenericExceptionCaught")

package com.azikar24.wormaceptor.api.ktor

import android.util.Log
import com.azikar24.wormaceptor.api.CapturedBody
import com.azikar24.wormaceptor.api.OkHttpBodyCapture
import com.azikar24.wormaceptor.api.RedactionConfig
import com.azikar24.wormaceptor.api.ServiceProvider
import com.azikar24.wormaceptor.api.WormaCeptorApi
import io.ktor.client.call.save
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.utils.io.discard
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CancellationException
import kotlinx.io.readByteArray
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.util.UUID

private const val TAG = "WormaCeptorKtor"

/**
 * Ktor client plugin that captures HTTP network traffic for inspection in WormaCeptor.
 *
 * Usage:
 * ```kotlin
 * val client = HttpClient(CIO) {
 *     install(WormaCeptorKtorPlugin) {
 *         maxContentLength = 500_000L
 *         retainDataFor = WormaCeptorKtorConfig.RetentionPeriod.ONE_WEEK
 *         redactHeader("Authorization")
 *         redactJsonValue("password")
 *     }
 * }
 * ```
 */
val WormaCeptorKtorPlugin = createClientPlugin("WormaCeptor", ::WormaCeptorKtorConfig) {

    val maxContentLength = pluginConfig.maxContentLength

    // Apply retention cleanup on install
    pluginConfig.retainDataFor?.let { period ->
        val millis = WormaCeptorKtorConfig.retentionToMillis(period)
        if (millis > 0) {
            val threshold = System.currentTimeMillis() - millis
            WormaCeptorApi.provider?.cleanup(threshold)
        }
    }

    // Lazy-loaded rate limit engine for applying latency/packet loss simulation
    val rateLimitDelay: (suspend () -> Unit)? by lazy {
        loadRateLimitDelay()
    }

    val mockEngine: Any? by lazy { loadMockEngine() }

    on(Send) { request ->
        val provider = WormaCeptorApi.capturingProvider ?: return@on proceed(request)
        val redaction = WormaCeptorApi.redactionConfig
        val startedAt = System.currentTimeMillis()

        var transactionId: UUID? = null

        // 1. Capture Request
        try {
            transactionId = captureRequest(request, provider, redaction, maxContentLength, startedAt)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to capture request for ${request.url}", e)
        }

        // 2. Serve a matching mock rule instead of the network, skipping rate limiting like WormaCeptorInterceptor
        val mocked = mockEngine?.let { mockedCallOrNull(it, client, request, startedAt) }

        // 3. Apply rate limiting (coroutine-friendly delay)
        if (mocked == null) {
            try {
                rateLimitDelay?.invoke()
            } catch (e: Exception) {
                Log.d(TAG, "Rate limit delay failed: ${e.message}")
            }
        }

        // 4. Network Call
        val call = mocked ?: try {
            proceed(request)
        } catch (e: Exception) {
            if (transactionId != null) {
                provider.completeTransaction(
                    id = transactionId,
                    code = 0,
                    message = "FAILED",
                    headers = emptyMap(),
                    bodyStream = null,
                    bodySize = 0,
                    protocol = null,
                    tlsVersion = null,
                    error = e.toString(),
                    showNotification = true,
                    durationMs = System.currentTimeMillis() - startedAt,
                )
            }
            throw e
        }

        // 5. Save call so body can be consumed by both us and downstream
        val savedCall = call.save()

        // 6. Capture Response
        if (transactionId != null) {
            captureResponse(savedCall.response, transactionId, provider, maxContentLength, startedAt)
        }

        savedCall
    }
}

private fun captureRequest(
    request: HttpRequestBuilder,
    provider: ServiceProvider,
    redaction: RedactionConfig,
    maxContentLength: Long,
    startedAt: Long,
): UUID? {
    val cleanHeaders = request.headers.build().entries()
        .associate { (key, values) ->
            if (redaction.headersToRedact.contains(key.lowercase())) {
                key to listOf(redaction.replacementText)
            } else {
                key to values
            }
        }

    val content = request.body as? OutgoingContent
    val mediaType = content?.contentType?.toString()?.toMediaTypeOrNull()
    // The bytes Ktor puts on the wire: TextContent encodes with its declared charset.
    val bodyBytes = when (content) {
        is TextContent -> content.text.toByteArray(mediaType?.charset() ?: Charsets.UTF_8)
        is ByteArrayContent -> content.bytes()
        else -> null
    }
    val bodyStream = bodyBytes?.takeIf { it.isNotEmpty() }?.let { bytes ->
        val captured = if (bytes.size > maxContentLength) bytes.copyOf(maxContentLength.toInt()) else bytes
        val encoding = request.headers[HttpHeaders.ContentEncoding]
        OkHttpBodyCapture.encodeForStorage(captured, mediaType, encoding, redaction, maxContentLength)
    }

    return provider.startTransaction(
        url = request.url.buildString(),
        method = request.method.value,
        headers = cleanHeaders,
        bodyStream = bodyStream,
        bodySize = bodyBytes?.size?.toLong() ?: 0L,
        startedAtMillis = startedAt,
    )
}

/** Completes the transaction even when reading the saved body fails, recording the failure as its error. */
private suspend fun captureResponse(
    response: HttpResponse,
    transactionId: UUID,
    provider: ServiceProvider,
    maxContentLength: Long,
    startedAt: Long,
) {
    val redaction = WormaCeptorApi.redactionConfig
    val cleanHeaders = response.headers.entries()
        .associate { (key, values) ->
            if (redaction.headersToRedact.contains(key.lowercase())) {
                key to listOf(redaction.replacementText)
            } else {
                key to values
            }
        }

    val body = try {
        // Copy only the capped prefix of the saved body; count the rest without copying it.
        val channel = response.bodyAsChannel()
        val capturedBytes = channel.readRemaining(maxContentLength).readByteArray()
        val bodySize = capturedBytes.size + channel.discard()
        val mediaType = response.contentType()?.toString()?.toMediaTypeOrNull()
        val encoding = response.headers[HttpHeaders.ContentEncoding]
        val stream = capturedBytes.takeIf { it.isNotEmpty() }?.let {
            OkHttpBodyCapture.encodeForStorage(it, mediaType, encoding, redaction, maxContentLength)
        }
        CapturedBody(stream, bodySize)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Failed to capture response for ${response.call.request.url}", e)
        CapturedBody(null, 0, "WormaCeptor failed to capture the response: $e")
    }

    provider.completeTransaction(
        id = transactionId,
        code = response.status.value,
        message = response.status.description,
        headers = cleanHeaders,
        bodyStream = body.stream,
        bodySize = body.size,
        protocol = response.version.toString(),
        // Ktor does not expose TLS handshake info
        tlsVersion = null,
        error = body.error,
        showNotification = true,
        // Header arrival, so time spent buffering the body in save() is not counted. A cached response
        // keeps its original receive time, from before this call started.
        durationMs = (response.responseTime.timestamp.takeIf { it >= startedAt } ?: System.currentTimeMillis()) -
            startedAt,
    )
}

/**
 * Loads the rate limit delay function via reflection from the RateLimitEngine.
 * Returns a suspend function that applies configured latency, or null if unavailable.
 */
private fun loadRateLimitDelay(): (suspend () -> Unit)? {
    return try {
        val engineClass = Class.forName(
            "com.azikar24.wormaceptor.core.engine.RateLimitEngine",
        )
        val koinClass = Class.forName("com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin")
        val getMethod = koinClass.getMethod("get", Class::class.java)
        val engine = getMethod.invoke(null, engineClass)
        val getDelayMethod = engineClass.getMethod("getDelayMillis")
        val delayFn: suspend () -> Unit = {
            val delayMs = getDelayMethod.invoke(engine) as? Long ?: 0L
            if (delayMs > 0) {
                kotlinx.coroutines.delay(delayMs)
            }
        }
        delayFn
    } catch (e: Exception) {
        Log.d(TAG, "Rate limit engine not available: ${e.message}")
        null
    }
}

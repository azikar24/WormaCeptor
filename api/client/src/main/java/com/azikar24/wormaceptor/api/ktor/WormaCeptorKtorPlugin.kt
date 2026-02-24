@file:Suppress("TooGenericExceptionCaught")

package com.azikar24.wormaceptor.api.ktor

import android.util.Log
import com.azikar24.wormaceptor.api.BinaryContentDetector
import com.azikar24.wormaceptor.api.RedactionConfig
import com.azikar24.wormaceptor.api.ServiceProvider
import com.azikar24.wormaceptor.api.WormaCeptorApi
import io.ktor.client.call.save
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.charset
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import java.io.ByteArrayInputStream
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

    on(Send) { request ->
        val provider = WormaCeptorApi.provider ?: return@on proceed(request)
        val redaction = WormaCeptorApi.redactionConfig

        var transactionId: UUID? = null

        // 1. Capture Request
        try {
            transactionId = captureRequest(request, provider, redaction, maxContentLength)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to capture request for ${request.url}", e)
        }

        // 2. Apply rate limiting (coroutine-friendly delay)
        try {
            rateLimitDelay?.invoke()
        } catch (e: Exception) {
            Log.d(TAG, "Rate limit delay failed: ${e.message}")
        }

        // 3. Network Call
        val call = try {
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
                )
            }
            throw e
        }

        // 4. Save call so body can be consumed by both us and downstream
        val savedCall = call.save()

        // 5. Capture Response
        if (transactionId != null) {
            try {
                captureResponse(savedCall.response, transactionId, provider, redaction, maxContentLength)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to capture response for ${request.url}", e)
            }
        }

        savedCall
    }
}

private fun captureRequest(
    request: HttpRequestBuilder,
    provider: ServiceProvider,
    redaction: RedactionConfig,
    maxContentLength: Long,
): UUID? {
    val cleanHeaders = request.headers.build().entries()
        .associate { (key, values) ->
            if (redaction.headersToRedact.contains(key.lowercase())) {
                key to listOf(redaction.replacementText)
            } else {
                key to values
            }
        }

    val bodyBytes = (request.body as? OutgoingContent)?.let { content ->
        KtorBodyCapture.captureRequestBody(content, maxContentLength)
    }

    val bodyStream = if (bodyBytes != null && bodyBytes.isNotEmpty()) {
        val bodyText = String(bodyBytes, Charsets.UTF_8)
        val redactedBody = redaction.applyRedactions(bodyText)
        redactedBody.byteInputStream()
    } else {
        null
    }

    return provider.startTransaction(
        url = request.url.buildString(),
        method = request.method.value,
        headers = cleanHeaders,
        bodyStream = bodyStream,
        bodySize = bodyBytes?.size?.toLong() ?: 0L,
    )
}

private suspend fun captureResponse(
    response: HttpResponse,
    transactionId: UUID,
    provider: ServiceProvider,
    redaction: RedactionConfig,
    maxContentLength: Long,
) {
    val cleanHeaders = response.headers.entries()
        .associate { (key, values) ->
            if (redaction.headersToRedact.contains(key.lowercase())) {
                key to listOf(redaction.replacementText)
            } else {
                key to values
            }
        }

    val protocol = response.version.toString()

    // Ktor does not expose TLS handshake info
    val tlsVersion: String? = null

    // Capture response body
    val contentType = response.contentType()?.toString()
    val isBinaryByHeader = BinaryContentDetector.isBinaryContentType(contentType)

    val allBytes = response.bodyAsChannel().readRemaining().readByteArray()
    val rawBytes = if (allBytes.size > maxContentLength.toInt()) {
        allBytes.copyOf(maxContentLength.toInt())
    } else {
        allBytes
    }
    val isBinary = isBinaryByHeader || BinaryContentDetector.isBinaryByMagicBytes(rawBytes)

    val bodyStream: java.io.InputStream?
    val bodySize: Long

    if (isBinary) {
        bodyStream = ByteArrayInputStream(rawBytes)
        bodySize = rawBytes.size.toLong()
    } else {
        val charset = response.contentType()?.charset() ?: Charsets.UTF_8
        val bodyText = String(rawBytes, charset)
        val redactedBody = redaction.applyRedactions(bodyText)
        bodyStream = redactedBody.byteInputStream()
        bodySize = redactedBody.length.toLong()
    }

    provider.completeTransaction(
        id = transactionId,
        code = response.status.value,
        message = response.status.description,
        headers = cleanHeaders,
        bodyStream = bodyStream,
        bodySize = bodySize,
        protocol = protocol,
        tlsVersion = tlsVersion,
        error = null,
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
        val koinClass = Class.forName("org.koin.java.KoinJavaComponent")
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

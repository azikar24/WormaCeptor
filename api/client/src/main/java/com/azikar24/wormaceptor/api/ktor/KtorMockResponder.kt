package com.azikar24.wormaceptor.api.ktor

import android.util.Log
import com.azikar24.wormaceptor.domain.entities.mock.MockResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpResponseData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay

private const val TAG = "WormaCeptorKtor"

/**
 * Serves the mock rule matching [request] from [engine] (core's MockEngine, reached by reflection like
 * WormaCeptorInterceptor does) after the rule's delay. Null when no rule applies, so the request goes out.
 */
internal suspend fun mockedCallOrNull(
    engine: Any,
    client: HttpClient,
    request: HttpRequestBuilder,
    startedAt: Long,
): HttpClientCall? {
    val (response, delayMs) = findMock(engine, request) ?: return null
    if (delayMs > 0) delay(delayMs)
    return mockedCall(client, request, response, GMTDate(startedAt))
}

private fun findMock(
    engine: Any,
    request: HttpRequestBuilder,
): Pair<MockResponse, Long>? = try {
    val engineClass = engine.javaClass
    val headers = request.headers.build().entries().associate { (name, values) -> name to values }
    val rule = engineClass
        .getMethod("findMatchingRule", String::class.java, String::class.java, Map::class.java)
        .invoke(engine, request.url.buildString(), request.method.value, headers)
    val response = rule?.let { engineClass.getMethod("resolveResponse", it.javaClass).invoke(engine, it) }
    if (rule == null || response !is MockResponse) {
        null
    } else {
        val delayMs = engineClass.getMethod("computeDelayMs", rule.javaClass).invoke(engine, rule) as? Long
        response to (delayMs ?: 0L)
    }
} catch (e: ReflectiveOperationException) {
    Log.d(TAG, "Mock engine check failed: ${e.message}")
    null
}

@OptIn(InternalAPI::class)
private suspend fun mockedCall(
    client: HttpClient,
    request: HttpRequestBuilder,
    mock: MockResponse,
    requestTime: GMTDate,
): HttpClientCall {
    val headers = Headers.build {
        mock.headers.forEach { (name, value) -> append(name, value) }
        set(HttpHeaders.ContentType, mock.contentType)
    }
    val responseData = HttpResponseData(
        statusCode = HttpStatusCode(mock.statusCode, mock.statusMessage),
        requestTime = requestTime,
        headers = headers,
        version = HttpProtocolVersion.HTTP_1_1,
        body = ByteReadChannel(mock.body.orEmpty().toByteArray()),
        // Ktor completes and joins this job once the caller is done with the response.
        callContext = currentCoroutineContext() + Job(request.executionContext),
    )
    return HttpClientCall(client, request.build(), responseData)
}

/** Resolves core's MockEngine by reflection; null when WormaCeptor's engine module is not available. */
internal fun loadMockEngine(): Any? = try {
    val engineClass = Class.forName("com.azikar24.wormaceptor.core.engine.MockEngine")
    val koinClass = Class.forName("com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin")
    koinClass.getMethod("get", Class::class.java).invoke(null, engineClass)
} catch (e: ReflectiveOperationException) {
    Log.d(TAG, "Mock engine not available: ${e.message}")
    null
}

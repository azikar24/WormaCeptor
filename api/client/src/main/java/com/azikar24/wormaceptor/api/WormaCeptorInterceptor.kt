package com.azikar24.wormaceptor.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.UUID

private const val TAG = "WormaCeptorInterceptor"

/**
 * OkHttp interceptor that captures HTTP/HTTPS network traffic for inspection in WormaCeptor.
 *
 * Add this interceptor to your OkHttpClient to monitor network requests and responses.
 * Both request and response bodies are captured (up to [maxContentLength]) and can be
 * viewed in the WormaCeptor UI.
 *
 * Usage:
 * ```kotlin
 * val client = OkHttpClient.Builder()
 *     .addInterceptor(WormaCeptorInterceptor())
 *     .build()
 * ```
 *
 * For sensitive data, configure redaction:
 * ```kotlin
 * val interceptor = WormaCeptorInterceptor()
 *     .redactHeader("Authorization")
 *     .redactJsonValue("password")
 *     .maxContentLength(500_000L)
 * ```
 */
class WormaCeptorInterceptor : Interceptor {

    /**
     * Data retention period for captured network transactions.
     */
    enum class Period {
        /** Retains data for 1 hour. */
        ONE_HOUR,

        /** Retains data for 24 hours. */
        ONE_DAY,

        /** Retains data for 7 days. */
        ONE_WEEK,

        /** Retains data for 30 days. */
        ONE_MONTH,

        /** Retains data indefinitely. */
        FOREVER,
    }

    private var showNotification = true
    private var maxContentLength = 250_000L

    // Lazy-loaded rate limit interceptor from Koin via reflection
    private val rateLimitInterceptor: Interceptor? by lazy {
        try {
            val engineClass = Class.forName(
                "com.azikar24.wormaceptor.core.engine.RateLimitEngine",
            )
            val koinClass = Class.forName("org.koin.java.KoinJavaComponent")
            val getMethod = koinClass.getMethod("get", Class::class.java)
            val engine = getMethod.invoke(null, engineClass)
            val getInterceptorMethod = engineClass.getMethod("getInterceptor")
            getInterceptorMethod.invoke(engine) as Interceptor
        } catch (e: Exception) {
            Log.d(TAG, "Rate limit interceptor not available: ${e.message}")
            null
        }
    }

    // Lazy-loaded mock engine from Koin via reflection
    private val mockEngineInstance: Any? by lazy {
        try {
            val engineClass = Class.forName(
                "com.azikar24.wormaceptor.core.engine.MockEngine",
            )
            val koinClass = Class.forName("org.koin.java.KoinJavaComponent")
            val getMethod = koinClass.getMethod("get", Class::class.java)
            getMethod.invoke(null, engineClass)
        } catch (e: Exception) {
            Log.d(TAG, "Mock engine not available: ${e.message}")
            null
        }
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val provider = WormaCeptorApi.provider ?: return chain.proceed(chain.request())
        val redaction = WormaCeptorApi.redactionConfig

        val request = chain.request()
        var transactionId: UUID? = null

        // 1. Capture Request
        try {
            val buffer = okio.Buffer()
            request.body?.writeTo(buffer)
            val bodySize = buffer.size

            val cleanHeaders = request.headers.toMultimap().mapValues { (key, values) ->
                if (redaction.headersToRedact.contains(key.lowercase())) {
                    listOf(redaction.replacementText)
                } else {
                    values
                }
            }

            val bodyStream = if (bodySize > 0) {
                val bodyText = buffer.clone().readUtf8()
                val redactedBody = redaction.applyRedactions(bodyText)
                redactedBody.byteInputStream()
            } else {
                null
            }

            transactionId = provider.startTransaction(
                url = request.url.toString(),
                method = request.method,
                headers = cleanHeaders,
                bodyStream = bodyStream,
                bodySize = bodySize,
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to capture request for ${request.url}", e)
        }

        // 2. Check mock rules BEFORE making the network call
        val mockResponse = tryMockRequest(request)
        if (mockResponse != null) {
            // Capture the mocked response
            if (transactionId != null) {
                try {
                    val mockBody = mockResponse.peekBody(maxContentLength)
                    val mockHeaders = mockResponse.headers.toMultimap()
                    val bodyText = mockBody.string()
                    provider.completeTransaction(
                        id = transactionId,
                        code = mockResponse.code,
                        message = mockResponse.message.ifEmpty { "MOCKED" },
                        headers = mockHeaders,
                        bodyStream = bodyText.byteInputStream(),
                        bodySize = bodyText.length.toLong(),
                        protocol = mockResponse.protocol.toString(),
                        tlsVersion = null,
                        error = null,
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to capture mocked response for ${request.url}", e)
                }
            }
            return mockResponse
        }

        // 3. Network Call (with rate limiting if enabled)
        val response: Response
        try {
            // Apply rate limiting before proceeding with the actual network call
            response = rateLimitInterceptor?.intercept(chain) ?: chain.proceed(request)
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

        // 4. Capture Response
        if (transactionId != null) {
            try {
                val responseBody = response.peekBody(maxContentLength)
                val cleanHeaders = response.headers.toMultimap().mapValues { (key, values) ->
                    if (redaction.headersToRedact.contains(key.lowercase())) {
                        listOf(redaction.replacementText)
                    } else {
                        values
                    }
                }

                val protocol = response.protocol.toString()
                val tlsVersion = response.handshake?.tlsVersion?.javaName

                // Check if the response is binary content (images, PDFs, etc.)
                // First check Content-Type header, then fall back to magic byte detection
                val contentType = response.header("Content-Type")
                val isBinaryByHeader = BinaryContentDetector.isBinaryContentType(contentType)

                // Always read as bytes first to enable magic byte detection as fallback
                val rawBytes = responseBody.bytes()
                val isBinary = isBinaryByHeader || BinaryContentDetector.isBinaryByMagicBytes(rawBytes)

                val bodyStream: java.io.InputStream?
                val bodySize: Long

                if (isBinary) {
                    // For binary content, store raw bytes without converting to string
                    // Converting binary data to UTF-8 string corrupts the data
                    bodyStream = ByteArrayInputStream(rawBytes)
                    bodySize = rawBytes.size.toLong()
                } else {
                    // For text content, apply redaction patterns
                    val bodyText = String(rawBytes, Charsets.UTF_8)
                    val redactedBody = redaction.applyRedactions(bodyText)
                    bodyStream = redactedBody.byteInputStream()
                    bodySize = redactedBody.length.toLong()
                }

                provider.completeTransaction(
                    id = transactionId,
                    code = response.code,
                    message = response.message,
                    headers = cleanHeaders,
                    bodyStream = bodyStream,
                    bodySize = bodySize,
                    protocol = protocol,
                    tlsVersion = tlsVersion,
                    error = null,
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to capture response for ${request.url}", e)
            }
        }

        return response
    }

    /**
     * Enables or disables notification display when transactions are captured.
     *
     * @param show true to show notifications, false to hide them
     * @return this interceptor for chaining
     */
    fun showNotification(show: Boolean): WormaCeptorInterceptor {
        this.showNotification = show
        return this
    }

    /**
     * Sets the maximum content length to capture for request and response bodies.
     * Bodies larger than this limit will be truncated.
     *
     * @param length Maximum body size in bytes (default: 250,000)
     * @return this interceptor for chaining
     */
    fun maxContentLength(length: Long): WormaCeptorInterceptor {
        this.maxContentLength = length
        return this
    }

    /**
     * Configures data retention period. Transactions older than the specified period
     * will be deleted when this method is called.
     *
     * @param period The retention period for captured data
     * @return this interceptor for chaining
     */
    fun retainDataFor(period: Period): WormaCeptorInterceptor {
        val millis = when (period) {
            Period.ONE_HOUR -> 60 * 60 * 1000L
            Period.ONE_DAY -> 24 * 60 * 60 * 1000L
            Period.ONE_WEEK -> 7 * 24 * 60 * 60 * 1000L
            Period.ONE_MONTH -> 30 * 24 * 60 * 60 * 1000L
            Period.FOREVER -> 0L
        }

        if (millis > 0) {
            val threshold = System.currentTimeMillis() - millis
            WormaCeptorApi.provider?.cleanup(threshold)
        }
        return this
    }

    /**
     * Adds a header name to be redacted. The header value will be replaced with asterisks.
     * Comparison is case-insensitive.
     *
     * @param name The header name to redact (e.g., "Authorization", "Cookie")
     * @return this interceptor for chaining
     */
    fun redactHeader(name: String): WormaCeptorInterceptor {
        WormaCeptorApi.redactionConfig.redactHeader(name)
        return this
    }

    /**
     * Adds a regex pattern to redact in request and response bodies.
     * Matched content will be replaced with asterisks.
     *
     * @param pattern A regex pattern to match (e.g., "api_key=\\w+")
     * @return this interceptor for chaining
     */
    fun redactBody(pattern: String): WormaCeptorInterceptor {
        WormaCeptorApi.redactionConfig.redactBody(pattern)
        return this
    }

    /**
     * Redacts JSON values for the specified key in request and response bodies.
     *
     * @param key The JSON key whose value should be redacted (e.g., "password", "token")
     * @return this interceptor for chaining
     */
    fun redactJsonValue(key: String): WormaCeptorInterceptor {
        WormaCeptorApi.redactionConfig.redactJsonValue(key)
        return this
    }

    /**
     * Redacts XML element values for the specified tag in request and response bodies.
     *
     * @param tag The XML tag whose content should be redacted (e.g., "Password")
     * @return this interceptor for chaining
     */
    fun redactXmlValue(tag: String): WormaCeptorInterceptor {
        WormaCeptorApi.redactionConfig.redactXmlValue(tag)
        return this
    }

    /**
     * Checks if any mock rule matches the given request.
     * Returns a fully formed OkHttp [Response] if a mock applies, null otherwise.
     * Uses reflection to access MockEngine to avoid hard class-loading dependency.
     */
    @Suppress("TooGenericExceptionCaught")
    private fun tryMockRequest(request: okhttp3.Request): Response? {
        val engine = mockEngineInstance ?: return null
        try {
            val engineClass = engine.javaClass

            // Call findMatchingRule(url, method, headers)
            val findMethod = engineClass.getMethod(
                "findMatchingRule",
                String::class.java,
                String::class.java,
                Map::class.java,
            )
            val matchedRule = findMethod.invoke(
                engine,
                request.url.toString(),
                request.method,
                request.headers.toMultimap(),
            ) ?: return null

            // Call resolveResponse(rule) -> MockResponse?
            val resolveMethod = engineClass.getMethod("resolveResponse", matchedRule.javaClass)
            val mockResponse = resolveMethod.invoke(engine, matchedRule) ?: return null

            // Call computeDelayMs(rule) -> Long
            val delayMethod = engineClass.getMethod("computeDelayMs", matchedRule.javaClass)
            val delayMs = delayMethod.invoke(engine, matchedRule) as Long
            if (delayMs > 0) {
                Thread.sleep(delayMs)
            }

            // Call buildOkHttpResponse(mockResponse, request) -> okhttp3.Response
            val buildMethod = engineClass.getMethod(
                "buildOkHttpResponse",
                mockResponse.javaClass,
                okhttp3.Request::class.java,
            )
            return buildMethod.invoke(engine, mockResponse, request) as Response
        } catch (e: Exception) {
            Log.d(TAG, "Mock engine check failed: ${e.message}")
            return null
        }
    }
}

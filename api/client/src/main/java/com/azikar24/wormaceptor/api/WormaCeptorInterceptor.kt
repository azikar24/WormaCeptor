package com.azikar24.wormaceptor.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.UUID

private const val TAG = "WormaCeptorInterceptor"

/**
 * File signature (magic bytes) constants for binary content detection.
 * These are the first bytes of common binary file formats.
 */
private object MagicBytes {
    // PNG signature: 89 50 4E 47 0D 0A 1A 0A
    val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)

    // JPEG signature: FF D8 FF
    val JPEG_SIGNATURE = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())

    // GIF signature: 47 49 46 38 ("GIF8")
    val GIF_SIGNATURE = byteArrayOf(0x47, 0x49, 0x46, 0x38)

    // WebP container: RIFF....WEBP (52 49 46 46 ?? ?? ?? ?? 57 45 42 50)
    val WEBP_RIFF_HEADER = byteArrayOf(0x52, 0x49, 0x46, 0x46)
    val WEBP_SIGNATURE = byteArrayOf(0x57, 0x45, 0x42, 0x50)
    const val WEBP_SIGNATURE_OFFSET = 8

    // PDF signature: 25 50 44 46 ("%PDF")
    val PDF_SIGNATURE = byteArrayOf(0x25, 0x50, 0x44, 0x46)

    // ZIP signature: 50 4B 03 04 ("PK\x03\x04")
    val ZIP_SIGNATURE = byteArrayOf(0x50, 0x4B, 0x03, 0x04)

    // GZIP signature: 1F 8B
    val GZIP_SIGNATURE = byteArrayOf(0x1F, 0x8B.toByte())

    // BMP signature: 42 4D ("BM")
    val BMP_SIGNATURE = byteArrayOf(0x42, 0x4D)
}

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

    /**
     * Content types that should be treated as binary and stored without text conversion.
     * Converting binary data to UTF-8 string corrupts the data.
     */
    private val binaryContentTypes = setOf(
        "image/",
        "audio/",
        "video/",
        "application/octet-stream",
        "application/pdf",
        "application/zip",
        "application/gzip",
        "application/x-tar",
        "application/x-rar",
        "application/x-7z-compressed",
        "application/vnd.",
        "font/",
        "model/",
    )

    /**
     * Checks if the content type indicates binary data that should not be converted to string.
     */
    private fun isBinaryContentType(contentType: String?): Boolean {
        if (contentType == null) return false
        val normalized = contentType.lowercase().trim()
        return binaryContentTypes.any { normalized.startsWith(it) }
    }

    /**
     * Detects if raw bytes are binary content (like images) by checking magic bytes.
     * This is a fallback for when Content-Type header is missing or incorrect.
     */
    private fun isBinaryByMagicBytes(data: ByteArray): Boolean {
        if (data.size < 8) return false

        return when {
            data.startsWith(MagicBytes.PNG_SIGNATURE) -> true
            data.startsWith(MagicBytes.JPEG_SIGNATURE) -> true
            data.startsWith(MagicBytes.GIF_SIGNATURE) -> true
            data.size >= 12 &&
                data.startsWith(MagicBytes.WEBP_RIFF_HEADER) &&
                data.startsWith(MagicBytes.WEBP_SIGNATURE, MagicBytes.WEBP_SIGNATURE_OFFSET) -> true
            data.startsWith(MagicBytes.PDF_SIGNATURE) -> true
            data.startsWith(MagicBytes.ZIP_SIGNATURE) -> true
            data.startsWith(MagicBytes.GZIP_SIGNATURE) -> true
            data.startsWith(MagicBytes.BMP_SIGNATURE) -> true
            else -> false
        }
    }

    /**
     * Checks if the byte array starts with the given prefix at the specified offset.
     */
    private fun ByteArray.startsWith(prefix: ByteArray, offset: Int = 0): Boolean {
        if (this.size < offset + prefix.size) return false
        for (i in prefix.indices) {
            if (this[offset + i] != prefix[i]) return false
        }
        return true
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

        // 2. Network Call (with rate limiting if enabled)
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

        // 3. Capture Response
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
                val isBinaryByHeader = isBinaryContentType(contentType)

                // Always read as bytes first to enable magic byte detection as fallback
                val rawBytes = responseBody.bytes()
                val isBinary = isBinaryByHeader || isBinaryByMagicBytes(rawBytes)

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
}

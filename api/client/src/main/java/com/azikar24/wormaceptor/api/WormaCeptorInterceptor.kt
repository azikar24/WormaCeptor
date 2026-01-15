package com.azikar24.wormaceptor.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.UUID

class WormaCeptorInterceptor() : Interceptor {

    enum class Period { ONE_HOUR, ONE_DAY, ONE_WEEK, ONE_MONTH, FOREVER }

    private var showNotification = true
    private var maxContentLength = 250_000L

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
        "model/"
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
            // PNG: 89 50 4E 47 0D 0A 1A 0A
            data[0] == 0x89.toByte() && data[1] == 0x50.toByte() &&
                    data[2] == 0x4E.toByte() && data[3] == 0x47.toByte() -> true

            // JPEG: FF D8 FF
            data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte() &&
                    data[2] == 0xFF.toByte() -> true

            // GIF: 47 49 46 38
            data[0] == 0x47.toByte() && data[1] == 0x49.toByte() &&
                    data[2] == 0x46.toByte() && data[3] == 0x38.toByte() -> true

            // WebP: 52 49 46 46 ?? ?? ?? ?? 57 45 42 50
            data.size >= 12 && data[0] == 0x52.toByte() && data[1] == 0x49.toByte() &&
                    data[2] == 0x46.toByte() && data[3] == 0x46.toByte() &&
                    data[8] == 0x57.toByte() && data[9] == 0x45.toByte() &&
                    data[10] == 0x42.toByte() && data[11] == 0x50.toByte() -> true

            // PDF: 25 50 44 46 (%PDF)
            data[0] == 0x25.toByte() && data[1] == 0x50.toByte() &&
                    data[2] == 0x44.toByte() && data[3] == 0x46.toByte() -> true

            // ZIP: 50 4B 03 04
            data[0] == 0x50.toByte() && data[1] == 0x4B.toByte() &&
                    data[2] == 0x03.toByte() && data[3] == 0x04.toByte() -> true

            // GZIP: 1F 8B
            data[0] == 0x1F.toByte() && data[1] == 0x8B.toByte() -> true

            // BMP: 42 4D
            data[0] == 0x42.toByte() && data[1] == 0x4D.toByte() -> true

            else -> false
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
                var bodyText = buffer.clone().readUtf8()
                redaction.bodyPatternsToRedact.forEach { regex ->
                    bodyText = bodyText.replace(regex, redaction.replacementText)
                }
                bodyText.byteInputStream()
            } else null

            transactionId = provider.startTransaction(
                url = request.url.toString(),
                method = request.method,
                headers = cleanHeaders,
                bodyStream = bodyStream,
                bodySize = bodySize
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Network Call
        val response: Response
        try {
            response = chain.proceed(request)
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
                     error = e.toString()
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
                    var bodyText = String(rawBytes, Charsets.UTF_8)
                    redaction.bodyPatternsToRedact.forEach { regex ->
                        bodyText = bodyText.replace(regex, redaction.replacementText)
                    }
                    bodyStream = bodyText.byteInputStream()
                    bodySize = bodyText.length.toLong()
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
                    error = null
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return response
    }

    fun showNotification(show: Boolean): WormaCeptorInterceptor {
        this.showNotification = show
        return this
    }

    fun maxContentLength(length: Long): WormaCeptorInterceptor {
        this.maxContentLength = length
        return this
    }

    fun retainDataFor(period: Period): WormaCeptorInterceptor {
        val millis = when(period) {
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

    fun redactHeader(name: String): WormaCeptorInterceptor {
        WormaCeptorApi.redactionConfig.redactHeader(name)
        return this
    }
    fun redactBody(name: String): WormaCeptorInterceptor {
        WormaCeptorApi.redactionConfig.redactBody(name)
        return this
    }
}


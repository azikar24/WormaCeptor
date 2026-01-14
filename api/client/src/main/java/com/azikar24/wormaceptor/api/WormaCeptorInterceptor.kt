package com.azikar24.wormaceptor.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.UUID

class WormaCeptorInterceptor() : Interceptor {
    
    enum class Period { ONE_HOUR, ONE_DAY, ONE_WEEK, ONE_MONTH, FOREVER }

    private var showNotification = true
    private var maxContentLength = 250_000L

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
                
                var bodyText = responseBody.string()
                redaction.bodyPatternsToRedact.forEach { regex ->
                    bodyText = bodyText.replace(regex, redaction.replacementText)
                }
                
                val protocol = response.protocol.toString()
                val tlsVersion = response.handshake?.tlsVersion?.javaName

                provider.completeTransaction(
                    id = transactionId,
                    code = response.code,
                    message = response.message,
                    headers = cleanHeaders,
                    bodyStream = bodyText.byteInputStream(),
                    bodySize = bodyText.length.toLong(),
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


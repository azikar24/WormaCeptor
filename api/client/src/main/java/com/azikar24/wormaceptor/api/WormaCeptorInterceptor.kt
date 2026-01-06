package com.azikar24.wormaceptor.api

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import com.azikar24.wormaceptor.api.ServiceLocator
import kotlinx.coroutines.launch

class WormaCeptorInterceptor(context: Context) : Interceptor {
    
    enum class Period { ONE_HOUR, ONE_DAY, ONE_WEEK, ONE_MONTH, FOREVER }

    private val notificationHelper = WormaCeptorNotificationHelper(context)
    private var showNotification = true
    private var maxContentLength = 250_000L
    private var headersToRedact = mutableSetOf<String>()

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val engine = ServiceLocator.captureEngine
        
        // If engine is missing, fail safe to just proceed (No-Op mode)
        if (engine == null) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()
        var transactionId: java.util.UUID? = null
        
        // 1. Capture Request
        try {
            val buffer = okio.Buffer()
            request.body?.writeTo(buffer)
            val bodySize = buffer.size
            
            // Basic Redaction (Header removal)
            val cleanHeaders = request.headers.toMultimap().filterKeys { !headersToRedact.contains(it) }

            kotlinx.coroutines.runBlocking {
                transactionId = engine.startTransaction(
                    url = request.url.toString(),
                    method = request.method,
                    headers = cleanHeaders,
                    bodyStream = if (bodySize > 0) buffer.clone().inputStream() else null,
                    bodySize = bodySize
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Network Call
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            // Capture Failure?
             if (transactionId != null) {
                 kotlinx.coroutines.runBlocking {
                     engine.completeTransaction(
                         id = transactionId!!,
                         code = 0,
                         message = "FAILED",
                         headers = emptyMap(),
                         bodyStream = null,
                         error = e.toString()
                     )
                 }
             }
            throw e
        }

        // 3. Capture Response
        if (transactionId != null) {
            try {
                val responseBody = response.peekBody(maxContentLength)
                val cleanHeaders = response.headers.toMultimap().filterKeys { !headersToRedact.contains(it) }
                val protocol = response.protocol.toString()
                val tlsVersion = response.handshake?.tlsVersion?.javaName

                kotlinx.coroutines.runBlocking {
                    engine.completeTransaction(
                        id = transactionId!!,
                        code = response.code,
                        message = response.message,
                        headers = cleanHeaders,
                        bodyStream = responseBody.byteStream(),
                        bodySize = responseBody.contentLength(),
                        protocol = protocol,
                        tlsVersion = tlsVersion,
                        error = null
                    )
                    
                    // 4. Notify
                    if (showNotification) {
                        val transaction = ServiceLocator.queryEngine?.getDetails(transactionId!!)
                        if (transaction != null) {
                            notificationHelper.show(transaction)
                        }
                    }
                }
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
            val engine = ServiceLocator.captureEngine
            if (engine != null) {
                // Fire and forget cleanup
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    engine.cleanup(threshold)
                }
            }
        }
        return this
    }

    fun redactHeader(name: String): WormaCeptorInterceptor {
        headersToRedact.add(name)
        return this
    }
}

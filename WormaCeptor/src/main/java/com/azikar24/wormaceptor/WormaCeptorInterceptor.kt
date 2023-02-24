/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor

import android.content.Context
import com.azikar24.wormaceptor.internal.data.HttpHeader
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.support.HttpHeaders
import com.azikar24.wormaceptor.internal.support.NotificationHelper
import com.azikar24.wormaceptor.internal.support.RetentionManager
import okhttp3.*
import okio.Buffer
import okio.BufferedSource
import okio.GzipSource
import okio.buffer
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

class WormaCeptorInterceptor(private val context: Context) : Interceptor {
    private val UTF8 = Charset.forName("UTF-8")

    private val storage = WormaCeptor.storage

    private var stickyNotification = false
    private var mNotificationHelper: NotificationHelper? = null
    private var mMaxContentLength = 250000L
    private var mRetentionManager: RetentionManager = RetentionManager(context, Period.ONE_WEEK)

    @Volatile
    private var headersToRedact = emptySet<String>()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val transaction: NetworkTransaction = createTransactionFromRequest(request)
        val startNs = System.nanoTime()

        return try {
            val response = chain.proceed(request)
            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            updateTransactionFromResponse(transaction, response, tookMs)
            response
        } catch (e: Exception) {
            update(transaction.toBuilder().setError(e.toString()).build())
            throw e
        }
    }

    @Throws(IOException::class)
    private fun updateTransactionFromResponse(transaction: NetworkTransaction, response: Response, tookMs: Long) {
        val responseBody = response.body
        val newTransactionBuilder = transaction.toBuilder()

        if (response.cacheResponse != null) {
            newTransactionBuilder.setResponseDate(Date())
            newTransactionBuilder.setTookMs(tookMs)
        } else {
            newTransactionBuilder.setTookMs(response.receivedResponseAtMillis - response.sentRequestAtMillis)
            newTransactionBuilder.setRequestDate(Date(response.sentRequestAtMillis))
            newTransactionBuilder.setResponseDate(Date(response.receivedResponseAtMillis))
        }


        newTransactionBuilder.setRequestHeaders(toHttpHeaderList(response.request.headers))
        newTransactionBuilder.setProtocol(response.protocol.toString())
        newTransactionBuilder.setResponseCode(response.code)
        newTransactionBuilder.setResponseMessage(response.message)

        if (responseBody != null) {
            newTransactionBuilder.setResponseContentLength(responseBody.contentLength())
            val contentType = responseBody.contentType()
            if (contentType != null) {
                newTransactionBuilder.setResponseContentType(contentType.toString())
            }
        }

        newTransactionBuilder.setResponseHeaders(toHttpHeaderList(response.headers))

        val responseBodyIsPlainText = bodyHasSupportedEncoding(response.headers)
        newTransactionBuilder.setResponseBodyIsPlainText(responseBodyIsPlainText)
        if (HttpHeaders.hasBody(response) && responseBodyIsPlainText) {
            val source = getNativeSource(response)
            source?.request(Long.MAX_VALUE)
            val buffer = source?.buffer
            var charset = UTF8
            var contentType: MediaType? = null
            if (responseBody != null) {
                contentType = responseBody.contentType()
            }
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8)
                } catch (e: UnsupportedCharsetException) {
                    update(newTransactionBuilder.build())
                    return
                }
            }
            if (buffer != null) {
                if (isPlaintext(buffer)) {
                    newTransactionBuilder.setResponseBody(readFromBuffer(buffer.clone(), charset))
                } else {
                    newTransactionBuilder.setResponseBodyIsPlainText(false)
                }
                newTransactionBuilder.setResponseContentLength(buffer.size)
            }

        }
        update(newTransactionBuilder.build())
    }


    fun showNotification(sticky: Boolean): WormaCeptorInterceptor {
        stickyNotification = sticky
        mNotificationHelper = NotificationHelper(context)
        return this
    }

    fun maxContentLength(max: Long): WormaCeptorInterceptor {
        mMaxContentLength = min(max, 999999L)
        return this
    }

    fun retainDataFor(period: Period): WormaCeptorInterceptor {
        mRetentionManager = RetentionManager(context, period)
        return this
    }

    fun redactHeader(name: String): WormaCeptorInterceptor {
        val newHeadersToRedact: MutableSet<String> = TreeSet(java.lang.String.CASE_INSENSITIVE_ORDER)
        newHeadersToRedact.addAll(headersToRedact)
        newHeadersToRedact.add(name)
        headersToRedact = newHeadersToRedact
        return this
    }

    @Throws(IOException::class)
    fun createTransactionFromRequest(request: Request): NetworkTransaction {
        val requestBody = request.body

        val hasRequestBody = requestBody != null

        val transactionBuilder = NetworkTransaction.Builder()
            .setRequestDate(Date())
            .setMethod(request.method)
            .setRequestHeaders(toHttpHeaderList(request.headers))
            .apply {
                setUrlHostPathSchemeFromUrl(request.url.toString())
                if (hasRequestBody) {
                    val contentType = requestBody?.contentType()
                    if (contentType != null) {
                        setRequestContentType(contentType.toString())
                    }
                    if (requestBody?.contentLength() != -1L) {
                        requestBody?.contentLength()?.let { setRequestContentLength(it) }
                    }
                    if (requestBodyIsPlainText) {
                        val source: BufferedSource = getNativeSource(Buffer(), bodyGzipped(request.headers))
                        val buffer = source.buffer
                        requestBody?.writeTo(buffer)
                        var charset = UTF8
                        if (contentType != null) {
                            charset = contentType.charset(UTF8)
                        }
                        if (isPlaintext(buffer)) {
                            setRequestBody(readFromBuffer(buffer, charset))
                        } else {
                            setResponseBodyIsPlainText(false)
                        }
                    }
                }

            }
            .setRequestBodyIsPlainText(bodyHasSupportedEncoding(request.headers))

        return create(transactionBuilder.build())
    }

    private fun toHttpHeaderList(headers: Headers): List<HttpHeader> {
        val httpHeaders: MutableList<HttpHeader> = ArrayList<HttpHeader>()
        var i = 0
        val count = headers.size
        while (i < count) {
            if (headersToRedact.contains(headers.name(i))) {
                httpHeaders.add(HttpHeader(headers.name(i), "\u2588\u2588\u2588\u2588"))
            } else {
                httpHeaders.add(HttpHeader(headers.name(i), headers.value(i)))
            }
            i++
        }
        return httpHeaders
    }

    private fun bodyHasSupportedEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return contentEncoding == null ||
                contentEncoding.equals("identity", ignoreCase = true) ||
                contentEncoding.equals("gzip", ignoreCase = true)
    }


    @Throws(IOException::class)
    private fun getNativeSource(response: Response): BufferedSource? {
        if (bodyGzipped(response.headers)) {
            val source = response.peekBody(mMaxContentLength).source()
            if (source.buffer.size < mMaxContentLength) {
                return getNativeSource(source, true)
            }
        }
        return response.body?.source()
    }

    private fun getNativeSource(input: BufferedSource, isGzipped: Boolean): BufferedSource {
        return if (isGzipped) {
            val source = GzipSource(input)
            source.buffer()
        } else {
            input
        }
    }

    private fun bodyGzipped(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"]
        return "gzip".equals(contentEncoding, ignoreCase = true)
    }

    private fun isPlaintext(buffer: Buffer): Boolean {
        return try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64) buffer.size else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            true
        } catch (e: EOFException) {
            false // Truncated UTF-8 sequence.
        }
    }

    private fun readFromBuffer(buffer: Buffer, charset: Charset): String {
        val bufferSize = buffer.size
        val maxBytes = min(bufferSize, mMaxContentLength)
        var body = ""
        try {
            body = buffer.readString(maxBytes, charset)
        } catch (e: EOFException) {
            body += context.getString(R.string.body_unexpected_eof)
        }
        if (bufferSize > mMaxContentLength) {
            body += context.getString(R.string.body_content_truncated)
        }
        return body
    }


    private fun create(transaction: NetworkTransaction): NetworkTransaction {
        val transactionId: Long = storage?.transactionDao?.insertTransaction(transaction) ?: -1
        val newTransaction = transaction.toBuilder().setId(transactionId).build()
        mNotificationHelper?.show(newTransaction, stickyNotification)
        mRetentionManager.doMaintenance()
        return newTransaction
    }


    private fun update(transaction: NetworkTransaction) {
        val updatedTransactionCount: Int = storage?.transactionDao?.updateTransaction(transaction) ?: -1
        if (updatedTransactionCount <= 0) return
        mNotificationHelper?.show(transaction, stickyNotification)
    }

    enum class Period {
        /**
         * Retain data for the last hour.
         */
        ONE_HOUR,

        /**
         * Retain data for the last day.
         */
        ONE_DAY,

        /**
         * Retain data for the last week.
         */
        ONE_WEEK,

        /**
         * Retain data forever.
         */
        FOREVER
    }
}
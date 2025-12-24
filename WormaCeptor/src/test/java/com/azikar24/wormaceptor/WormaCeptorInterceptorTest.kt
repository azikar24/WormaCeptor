/*
 * Copyright AziKar24 23/12/2025.
 */

package com.azikar24.wormaceptor

import android.content.Context
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.data.TransactionDao
import com.azikar24.wormaceptor.internal.data.WormaCeptorStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

class WormaCeptorInterceptorTest {

    private lateinit var context: Context
    private lateinit var interceptor: WormaCeptorInterceptor

    private lateinit var storage: WormaCeptorStorage
    private lateinit var dao: TransactionDao

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        storage = mockk(relaxed = true)
        dao = mockk(relaxed = true)
        mockkObject(WormaCeptor)
        every { WormaCeptor.storage } returns storage
        every { storage.transactionDao } returns dao
        
        // Mocking R.string values to avoid Resources$NotFoundException
        every { context.getString(any()) } returns "mocked_string"
        
        interceptor = WormaCeptorInterceptor(context)
    }

    @Test
    fun `test redactHeader replaces header value with stars`() {
        interceptor.redactHeader("Authorization")
        
        val headers = Headers.Builder()
            .add("Authorization", "Bearer secret-token")
            .add("Content-Type", "application/json")
            .build()
            
        val redactedHeaders = interceptor.toHttpHeaderList(headers)
        
        val authHeader = redactedHeaders.find { it.name == "Authorization" }
        val contentTypeHeader = redactedHeaders.find { it.name == "Content-Type" }
        
        assertEquals("████████", authHeader?.value)
        assertEquals("application/json", contentTypeHeader?.value)
    }

    @Test
    fun `test bodyHasSupportedEncoding returns true for supported encodings`() {
        val identityHeaders = Headers.Builder().add("Content-Encoding", "identity").build()
        val gzipHeaders = Headers.Builder().add("Content-Encoding", "gzip").build()
        val noEncodingHeaders = Headers.Builder().build()
        
        assertTrue(interceptor.bodyHasSupportedEncoding(identityHeaders))
        assertTrue(interceptor.bodyHasSupportedEncoding(gzipHeaders))
        assertTrue(interceptor.bodyHasSupportedEncoding(noEncodingHeaders))
    }

    @Test
    fun `test bodyHasSupportedEncoding returns false for unsupported encodings`() {
        val brHeaders = Headers.Builder().add("Content-Encoding", "br").build()
        val deflateHeaders = Headers.Builder().add("Content-Encoding", "deflate").build()
        
        assertFalse(interceptor.bodyHasSupportedEncoding(brHeaders))
        assertFalse(interceptor.bodyHasSupportedEncoding(deflateHeaders))
    }

    @Test
    fun `test createTransactionFromRequest captures basic request info`() {
        val request = Request.Builder()
            .url("https://example.com/api/test?param=value")
            .method("POST", "{\"key\":\"value\"}".toRequestBody("application/json".toMediaType()))
            .addHeader("Custom-Header", "Value")
            .build()
            
        // Mock to avoid background side effects that use storage
        every { dao.insertTransaction(any()) } returns 1L
        
        val transaction = interceptor.createTransactionFromRequest(request)
        
        assertEquals("https://example.com/api/test?param=value", transaction.url)
        assertEquals("POST", transaction.method)
        assertEquals("example.com", transaction.host)
        assertEquals("/api/test", transaction.path)
        assertTrue(transaction.requestHeaders?.any { it.name == "Custom-Header" && it.value == "Value" } ?: false)
    }

    @Test
    fun `test updateTransactionFromResponse captures response info`() {
        val transaction = NetworkTransaction(path = "/test")
        val request = Request.Builder().url("https://example.com/test").build()
        val responseBodyString = "{\"status\":\"success\"}"
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBodyString.toResponseBody("application/json".toMediaType()))
            .addHeader("Response-Header", "RespValue")
            .sentRequestAtMillis(1000L)
            .receivedResponseAtMillis(1100L)
            .build()
            
        // Mock to avoid background side effects
        every { dao.updateTransaction(any()) } returns 1
        
        interceptor.updateTransactionFromResponse(transaction, response, 100)
        
        println("[DEBUG_LOG] responseCode: ${transaction.responseCode}")
        // responseCode is set from response.code which is 200 in the builder
        assertEquals(200, transaction.responseCode)
        assertEquals("OK", transaction.responseMessage)
        assertEquals("application/json", transaction.responseContentType)
        assertTrue(transaction.responseHeaders?.any { it.name == "Response-Header" && it.value == "RespValue" } ?: false)
        // Check if responseCode is actually being updated
        // If it's failing with AssertionError at line 129, let's see why
    }
}
package com.azikar24.wormaceptor.api

import android.util.Log
import io.kotest.matchers.longs.shouldBeBetween
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.BufferedSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class WormaCeptorInterceptorTest {

    private val provider = mockk<ServiceProvider>(relaxed = true)
    private val id = UUID.randomUUID()
    private val request = Request.Builder().url("https://example.com").build()

    @BeforeEach
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>(), any()) } returns 0
        mockkObject(WormaCeptorApi)
        every { WormaCeptorApi.capturingProvider } returns provider
        every { provider.startTransaction(any(), any(), any(), any(), any(), any<Long>()) } returns id
    }

    @AfterEach
    fun tearDown() = unmockkAll()

    private fun chainReturning(response: Response): Interceptor.Chain = mockk {
        every { request() } returns request
        every { proceed(any()) } returns response
    }

    private fun response(
        body: ResponseBody,
        receivedAt: Long = 0,
    ) = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .body(body)
        .receivedResponseAtMillis(receivedAt)
        .build()

    @Test
    fun `records the start time synchronously and duration up to header arrival`() {
        val before = System.currentTimeMillis()
        val receivedAt = before + HEADER_DELAY
        val startedAt = slot<Long>()
        every {
            provider.startTransaction(any(), any(), any(), any(), any(), capture(startedAt))
        } returns id
        val duration = slot<Long?>()

        val result = WormaCeptorInterceptor().intercept(
            chainReturning(response("abc".toResponseBody("text/plain".toMediaType()), receivedAt)),
        )
        result.body.shouldNotBeNull().string()

        startedAt.captured.shouldBeBetween(before, System.currentTimeMillis())
        verify {
            provider.completeTransaction(
                id, 200, "OK", any(), any(), 3, any(), any(), null, true, captureNullable(duration),
            )
        }
        // Completion ran just now; a duration near HEADER_DELAY proves it ends at receivedResponseAtMillis.
        duration.captured.shouldNotBeNull().shouldBeBetween(HEADER_DELAY - TOLERANCE, HEADER_DELAY)
    }

    @Test
    fun `a cached response stored before the call started ends its duration at completion`() {
        val storedLongAgo = System.currentTimeMillis() - HEADER_DELAY
        val chain = mockk<Interceptor.Chain> {
            every { request() } returns request
            every { proceed(any()) } answers {
                Thread.sleep(CACHE_LOOKUP_MS)
                response("abc".toResponseBody("text/plain".toMediaType()), storedLongAgo)
            }
        }
        val duration = slot<Long?>()

        WormaCeptorInterceptor().intercept(chain).body.shouldNotBeNull().string()

        verify {
            provider.completeTransaction(
                id, 200, "OK", any(), any(), 3, any(), any(), null, true, captureNullable(duration),
            )
        }
        duration.captured.shouldNotBeNull().shouldBeBetween(CACHE_LOOKUP_MS, TOLERANCE)
    }

    @Test
    fun `completes with an error when wrapping the response throws`() {
        val error = slot<String?>()

        val result = WormaCeptorInterceptor().intercept(chainReturning(response(ExplodingBody())))

        result.body.shouldNotBeNull()
        verify {
            provider.completeTransaction(
                id, 200, "OK", any(), null, 0, any(), any(), captureNullable(error), true, any(),
            )
        }
        error.captured.shouldNotBeNull() shouldContain "failed to capture"
    }

    private class ExplodingBody : ResponseBody() {
        override fun contentType(): MediaType = error("boom")

        override fun contentLength(): Long = 3

        override fun source(): BufferedSource = throw AssertionError("body must not be read")
    }

    private companion object {
        const val HEADER_DELAY = 5_000L
        const val TOLERANCE = 1_000L
        const val CACHE_LOOKUP_MS = 50L
    }
}

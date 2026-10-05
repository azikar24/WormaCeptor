package com.azikar24.wormaceptor.api

import android.util.Log
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import okio.GzipSink
import okio.Source
import okio.Timeout
import okio.buffer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class OkHttpBodyCaptureTest {

    private val redaction = RedactionConfig()

    @Nested
    inner class RequestHeaders {

        @Test
        fun `adds content type and length from the body when headers are absent`() {
            val request = Request.Builder()
                .url("https://example.com")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()

            val headers = OkHttpBodyCapture.requestHeaders(request)

            headers["content-type"] shouldBe listOf("application/json; charset=utf-8")
            headers["content-length"] shouldBe listOf("2")
        }

        @Test
        fun `body content type wins over explicit header like BridgeInterceptor`() {
            val request = Request.Builder()
                .url("https://example.com")
                .header("Content-Type", "text/custom")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()

            val headers = OkHttpBodyCapture.requestHeaders(request)

            headers["content-type"] shouldBe listOf("application/json; charset=utf-8")
            headers["Content-Type"].shouldBeNull()
        }

        @Test
        fun `records chunked transfer encoding when length is unknown`() {
            val request = Request.Builder()
                .url("https://example.com")
                .header("Content-Length", "99")
                .post(UnknownLengthBody())
                .build()

            val headers = OkHttpBodyCapture.requestHeaders(request)

            headers["transfer-encoding"] shouldBe listOf("chunked")
            headers.keys.none { it.equals("content-length", ignoreCase = true) } shouldBe true
        }
    }

    @Nested
    inner class CaptureRequest {

        @Test
        fun `captures text body`() {
            val body = "hello".toRequestBody("text/plain".toMediaType())

            val captured = OkHttpBodyCapture.captureRequest(body, null, MAX, redaction)

            captured.size shouldBe 5
            captured.stream.shouldNotBeNull().readBytes().decodeToString() shouldBe "hello"
        }

        @Test
        fun `skips one-shot body without writing it`() {
            val captured = OkHttpBodyCapture.captureRequest(UnwritableBody(oneShot = true), null, MAX, redaction)

            captured.stream.shouldBeNull()
            captured.size shouldBe 0
        }

        @Test
        fun `skips duplex body without writing it`() {
            val captured = OkHttpBodyCapture.captureRequest(UnwritableBody(duplex = true), null, MAX, redaction)

            captured.stream.shouldBeNull()
        }

        @Test
        fun `caps captured bytes but reports full size`() {
            val body = "abcdefghij".toRequestBody("text/plain".toMediaType())

            val captured = OkHttpBodyCapture.captureRequest(body, null, 4, redaction)

            captured.size shouldBe 10
            captured.stream.shouldNotBeNull().readBytes().decodeToString() shouldBe "abcd"
        }

        @Test
        fun `decodes with declared charset and reports byte size`() {
            val body = "café".toRequestBody("text/plain; charset=ISO-8859-1".toMediaType())

            val captured = OkHttpBodyCapture.captureRequest(body, null, MAX, redaction)

            captured.size shouldBe 4
            captured.stream.shouldNotBeNull().readBytes().decodeToString() shouldBe "café"
        }

        @Test
        fun `redacts vendor json body even though vnd types look binary`() {
            val body = """{"password":"hunter2"}""".toRequestBody("application/vnd.api+json".toMediaType())

            val captured = OkHttpBodyCapture.captureRequest(
                body,
                null,
                MAX,
                RedactionConfig().redactJsonValue("password"),
            )

            captured.stream.shouldNotBeNull().readBytes().decodeToString().contains("hunter2") shouldBe false
        }

        @Test
        fun `captures body that closes the sink itself`() {
            val body = object : RequestBody() {
                override fun contentType(): MediaType = "text/plain".toMediaType()

                override fun writeTo(sink: BufferedSink) {
                    sink.outputStream().use { it.write("hello".toByteArray()) }
                }
            }

            val captured = OkHttpBodyCapture.captureRequest(body, null, MAX, redaction)

            captured.stream.shouldNotBeNull().readBytes().decodeToString() shouldBe "hello"
        }

        @Test
        fun `stores binary content type bytes unchanged`() {
            val bytes = byteArrayOf(0x00, 0xFF.toByte(), 0x10, 0x80.toByte())
            val body = bytes.toRequestBody("application/octet-stream".toMediaType())

            val captured = OkHttpBodyCapture.captureRequest(body, null, MAX, redaction)

            captured.stream.shouldNotBeNull().readBytes().toList() shouldBe bytes.toList()
        }
    }

    @Nested
    inner class CaptureResponse {

        private val captures = mutableListOf<CapturedBody>()

        private fun capture(
            response: Response,
            max: Long = MAX,
        ): Response = OkHttpBodyCapture.captureResponse(response, max, redaction) { captures += it }

        @Test
        fun `skips event stream body without reading it`() {
            val response = responseWith(UnreadableBody("text/event-stream".toMediaType()))

            capture(response) shouldBeSameInstanceAs response

            captures.single().stream.shouldBeNull()
            captures.single().size shouldBe 0
        }

        @Test
        fun `returns streaming body without reading it`() {
            val response = responseWith(UnreadableBody("application/json".toMediaType()))

            val wrapped = capture(response)

            captures.shouldBeEmpty()
            wrapped.body.shouldNotBeNull().contentType() shouldBe "application/json".toMediaType()
            wrapped.body.shouldNotBeNull().contentLength() shouldBe -1
        }

        @Test
        fun `completes empty body immediately`() {
            capture(responseWith(ByteArray(0).toResponseBody("text/plain".toMediaType())))

            captures.single().stream.shouldBeNull()
            captures.single().size shouldBe 0
        }

        @Test
        fun `decodes with declared charset and reports byte size`() {
            val bytes = "café".toByteArray(Charsets.ISO_8859_1)
            val wrapped = capture(responseWith(bytes.toResponseBody("text/plain; charset=ISO-8859-1".toMediaType())))

            wrapped.body.shouldNotBeNull().bytes()

            captures.single().size shouldBe 4
            captures.single().stream.shouldNotBeNull().readBytes().decodeToString() shouldBe "café"
        }

        @Test
        fun `caller reads identical bytes while capture is capped`() {
            val bytes = ByteArray(20_000) { it.toByte() }
            val wrapped = capture(responseWith(bytes.toResponseBody("application/octet-stream".toMediaType())), 100)

            wrapped.body.shouldNotBeNull().bytes().toList() shouldBe bytes.toList()

            captures.single().size shouldBe 20_000
            captures.single().stream.shouldNotBeNull().readBytes().toList() shouldBe bytes.take(100)
        }

        @Test
        fun `completes once when read to end and then closed`() {
            val wrapped = capture(responseWith("abcdefghij".toResponseBody("text/plain".toMediaType())), 4)

            val body = wrapped.body.shouldNotBeNull()
            body.source().readUtf8() shouldBe "abcdefghij"
            body.close()

            captures.single().size shouldBe 10
            captures.single().stream.shouldNotBeNull().readBytes().decodeToString() shouldBe "abcd"
        }

        @Test
        fun `close without reading drains a body that fits the cap`() {
            val wrapped = capture(responseWith("abcdefghij".toResponseBody("text/plain".toMediaType())))

            wrapped.close()
            wrapped.close()

            captures.single().size shouldBe 10
            captures.single().error.shouldBeNull()
            captures.single().stream.shouldNotBeNull().readBytes().decodeToString() shouldBe "abcdefghij"
        }

        @Test
        fun `close after partial read drains the rest`() {
            val wrapped = capture(responseWith("abcdefghij".toResponseBody("text/plain".toMediaType())))

            val body = wrapped.body.shouldNotBeNull()
            body.source().readUtf8(3) shouldBe "abc"
            body.close()

            captures.single().stream.shouldNotBeNull().readBytes().decodeToString() shouldBe "abcdefghij"
        }

        @Test
        fun `close without reading leaves a body over the cap undrained and stores no blob`() {
            val source = Buffer().writeUtf8("abcdefghij")
            val wrapped = capture(responseWith(FixedLengthBody(source, 10)), 4)

            wrapped.close()

            source.size shouldBe 10
            captures.single().stream.shouldBeNull()
            captures.single().size shouldBe 10
        }

        @Test
        fun `completes on early close with bytes read and no blob when length is unknown`() {
            val source = Buffer().writeUtf8("abcdefghij")
            val wrapped = capture(responseWith(source.asResponseBody("text/plain".toMediaType())))

            wrapped.close()

            captures.single().size shouldBe 0
            captures.single().stream.shouldBeNull()
        }

        @Test
        fun `read failure reaches the caller and is recorded with the bytes actually read`() {
            val wrapped = capture(responseWith(FailingBody("abcd", declaredLength = 100)))

            val body = wrapped.body.shouldNotBeNull()
            shouldThrow<IOException> { body.string() }

            val captured = captures.single()
            captured.error.shouldNotBeNull() shouldContain "connection reset"
            captured.size shouldBe 4
            captured.stream.shouldNotBeNull().readBytes().decodeToString() shouldBe "abcd"
        }

        @Test
        fun `close on another thread during a read neither drains concurrently nor breaks the read`() {
            val body = BlockingBody()
            val wrapped = capture(responseWith(body))
            val source = wrapped.body.shouldNotBeNull().source()

            val reader = thread { source.request(1) }
            body.readEntered.await(5, TimeUnit.SECONDS) shouldBe true
            wrapped.close()
            body.releaseRead.countDown()
            reader.join(5_000)

            body.reads.get() shouldBe 1
            captures.single().stream.shouldBeNull()
        }

        @Test
        fun `stores gzip body under a json type decompressed and redacted`() {
            val json = """{"password":"hunter2","name":"a"}"""
            val response = responseWith(gzip(json).toResponseBody("application/json".toMediaType()))
                .newBuilder().header("Content-Encoding", "gzip").build()
            val wrapped = OkHttpBodyCapture.captureResponse(
                response,
                MAX,
                RedactionConfig().redactJsonValue("password"),
            ) {
                captures += it
            }

            wrapped.body.shouldNotBeNull().bytes().toList() shouldBe gzip(json).toList()

            val stored = captures.single().stream.shouldNotBeNull().readBytes().decodeToString()
            stored shouldContain "\"name\":\"a\""
            stored shouldNotContain "hunter2"
        }

        @Test
        fun `defers decoding and redaction until the stored stream is read`() {
            val spied = spyk(RedactionConfig())
            val wrapped = OkHttpBodyCapture.captureResponse(
                responseWith("abc".toResponseBody("text/plain".toMediaType())),
                MAX,
                spied,
            ) { captures += it }

            wrapped.body.shouldNotBeNull().string()
            verify(exactly = 0) { spied.applyRedactions(any()) }

            captures.single().stream.shouldNotBeNull().readBytes()
            verify(exactly = 1) { spied.applyRedactions(any()) }
        }

        @Test
        fun `capture failure does not break the caller's read`() {
            mockkStatic(Log::class)
            every { Log.w(any<String>(), any<String>(), any()) } returns 0
            try {
                val response = responseWith("abcdefghij".toResponseBody("text/plain".toMediaType()))
                val wrapped = OkHttpBodyCapture.captureResponse(response, MAX, redaction) {
                    throw IllegalStateException("storage failed")
                }

                wrapped.body.shouldNotBeNull().string() shouldBe "abcdefghij"
                verify(exactly = 1) { Log.w(any<String>(), any<String>(), any()) }
            } finally {
                unmockkStatic(Log::class)
            }
        }
    }

    @Nested
    inner class EncodeForStorage {

        private val passwordRedaction = RedactionConfig().redactJsonValue("password")
        private val json = """{"password":"hunter2"}"""

        private fun stored(
            bytes: ByteArray,
            type: String,
            encoding: String?,
            max: Long = MAX,
        ) = OkHttpBodyCapture.encodeForStorage(bytes, type.toMediaType(), encoding, passwordRedaction, max).readBytes()

        @Test
        fun `inflates gzip and redacts the text`() {
            stored(gzip(json), "application/json", "gzip").decodeToString() shouldNotContain "hunter2"
        }

        @Test
        fun `inflates at most max bytes`() {
            stored(gzip("a".repeat(1_000)), "text/plain", "gzip", max = 10).decodeToString() shouldBe "a".repeat(10)
        }

        @Test
        fun `keeps what inflated from a truncated gzip capture`() {
            val compressed = gzip("abcdefghij".repeat(100))
            val truncated = compressed.copyOf(compressed.size - 8)

            stored(truncated, "text/plain", "gzip").decodeToString() shouldStartWith "abcdefghij"
        }

        @Test
        fun `stores other encodings raw`() {
            val bytes = byteArrayOf(0x0B, 0x80.toByte(), 0xFF.toByte(), 0x00, 0x13)

            stored(bytes, "application/json", "br").toList() shouldBe bytes.toList()
        }

        @Test
        fun `still redacts plain text under a stale encoding header`() {
            stored(json.toByteArray(), "application/json", "br").decodeToString() shouldNotContain "hunter2"
        }

        @Test
        fun `ignores gzip header when bytes are not gzip`() {
            stored(json.toByteArray(), "application/json", "gzip").decodeToString() shouldNotContain "hunter2"
        }
    }

    private fun gzip(text: String): ByteArray {
        val buffer = Buffer()
        GzipSink(buffer).buffer().use { it.writeUtf8(text) }
        return buffer.readByteArray()
    }

    private fun responseWith(body: ResponseBody) = Response.Builder()
        .request(Request.Builder().url("https://example.com").build())
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .body(body)
        .build()

    private class UnwritableBody(
        private val oneShot: Boolean = false,
        private val duplex: Boolean = false,
    ) : RequestBody() {
        override fun contentType(): MediaType = "text/plain".toMediaType()

        override fun contentLength(): Long = 10

        override fun isOneShot(): Boolean = oneShot

        override fun isDuplex(): Boolean = duplex

        override fun writeTo(sink: BufferedSink) {
            throw AssertionError("body must not be written")
        }
    }

    private class UnknownLengthBody : RequestBody() {
        override fun contentType(): MediaType = "text/plain".toMediaType()

        override fun writeTo(sink: BufferedSink) {
            sink.writeUtf8("x")
        }
    }

    private class FixedLengthBody(
        private val source: Buffer,
        private val length: Long,
    ) : ResponseBody() {
        override fun contentType(): MediaType = "text/plain".toMediaType()

        override fun contentLength(): Long = length

        override fun source(): BufferedSource = source
    }

    /** Yields [prefix] then fails, like a connection reset mid-body. */
    private class FailingBody(
        private val prefix: String,
        private val declaredLength: Long,
    ) : ResponseBody() {
        private val source = object : Source {
            private val data = Buffer().writeUtf8(prefix)

            override fun read(
                sink: Buffer,
                byteCount: Long,
            ): Long {
                if (data.size == 0L) throw IOException("connection reset")
                return data.read(sink, byteCount)
            }

            override fun timeout(): Timeout = Timeout.NONE

            override fun close() = Unit
        }.buffer()

        override fun contentType(): MediaType = "text/plain".toMediaType()

        override fun contentLength(): Long = declaredLength

        override fun source(): BufferedSource = source
    }

    /** A body whose first read blocks until [releaseRead], counting reads to detect a concurrent drain. */
    private class BlockingBody : ResponseBody() {
        val readEntered = CountDownLatch(1)
        val releaseRead = CountDownLatch(1)
        val reads = AtomicInteger()

        private val source = object : Source {
            override fun read(
                sink: Buffer,
                byteCount: Long,
            ): Long {
                reads.incrementAndGet()
                readEntered.countDown()
                releaseRead.await(5, TimeUnit.SECONDS)
                sink.writeUtf8("abc")
                return 3
            }

            override fun timeout(): Timeout = Timeout.NONE

            override fun close() = Unit
        }.buffer()

        override fun contentType(): MediaType = "text/plain".toMediaType()

        override fun contentLength(): Long = 10

        override fun source(): BufferedSource = source
    }

    private class UnreadableBody(private val type: MediaType) : ResponseBody() {
        override fun contentType(): MediaType = type

        override fun contentLength(): Long = -1

        override fun source(): BufferedSource = throw AssertionError("body must not be read")
    }

    private companion object {
        const val MAX = 250_000L
    }
}

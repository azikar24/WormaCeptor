package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.core.engine.CaptureEngine
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.mcp.protocol.ReplayRequestDto
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okio.Buffer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.ServerSocket
import java.util.UUID
import kotlin.concurrent.thread

class ReplayTest {

    private val post = NetworkTransaction(
        request = Request(
            url = "https://api.test/login",
            method = "post",
            headers = mapOf(
                "Content-Type" to listOf("application/json"),
                "Content-Length" to listOf("17"),
                "Authorization" to listOf("Bearer secret"),
                "X-Trace" to listOf("a", "b"),
            ),
            bodyRef = "blob",
        ),
    )

    @Test
    fun `shellQuote survives single quotes and newlines`() {
        shellQuote("it's\nok") shouldBe "'it'\\''s\nok'"
    }

    @Test
    fun `curl for GET has no method flag and drops Content-Length`() {
        val curl = buildCurl(
            "GET",
            "https://api.test/users?q=a&b=1",
            mapOf("Accept" to listOf("*/*"), "Content-Length" to listOf("0")),
            null,
        )
        curl shouldBe "curl \\\n  'https://api.test/users?q=a&b=1' \\\n  -H 'Accept: */*'"
    }

    @Test
    fun `curl for POST carries method, every header value and a quoted body`() {
        val curl = buildCurl("post", "https://api.test/login", post.request.headers, """{"user":"o'neil"}""")

        curl shouldBe listOf(
            "curl",
            "-X POST",
            "'https://api.test/login'",
            "-H 'Content-Type: application/json'",
            "-H 'Authorization: Bearer secret'",
            "-H 'X-Trace: a'",
            "-H 'X-Trace: b'",
            """--data-binary '{"user":"o'\''neil"}'""",
        ).joinToString(" \\\n  ")
    }

    @Test
    fun `curl for HEAD uses --head`() {
        buildCurl("HEAD", "https://api.test", emptyMap(), null) shouldBe "curl \\\n  --head \\\n  'https://api.test'"
    }

    @Test
    fun `replay plan keeps the stored request and applies overrides by header name`() {
        val plan = buildReplayPlan(
            post,
            ReplayRequestDto(
                url = "https://staging.test/login",
                headers = mapOf("authorization" to "Bearer other"),
                body = "{}",
            ),
            storedBody = "stored".toByteArray(),
        )

        plan.method shouldBe "POST"
        plan.url shouldBe "https://staging.test/login"
        plan.headers shouldBe mapOf(
            "Content-Type" to listOf("application/json"),
            "X-Trace" to listOf("a", "b"),
            "authorization" to listOf("Bearer other"),
        )
        plan.body?.decodeToString() shouldBe "{}"
    }

    @Test
    fun `replay plan without overrides sends the stored body`() {
        val plan = buildReplayPlan(post, ReplayRequestDto(), storedBody = "stored".toByteArray())

        plan.url shouldBe "https://api.test/login"
        plan.body?.decodeToString() shouldBe "stored"
    }

    @Test
    fun `OkHttp request drops a body on GET and sends an empty one on POST`() {
        ReplayPlan("GET", "https://api.test/x", emptyMap(), "x".toByteArray()).toOkHttpRequest().body shouldBe null
        ReplayPlan("POST", "https://api.test/x", emptyMap(), null).toOkHttpRequest().body?.contentLength() shouldBe 0L
    }

    @Test
    fun `a bad URL is an IllegalArgumentException`() {
        assertThrows<IllegalArgumentException> {
            ReplayPlan("GET", "not a url", emptyMap(), null).toOkHttpRequest()
        }
    }

    @Test
    fun `sendReplay sends the request and records it as a new transaction`() = runTest {
        val server = ServerSocket(0)
        var received = ""
        val serverThread = thread {
            server.accept().use { socket ->
                val input = socket.getInputStream().bufferedReader()
                val head = generateSequence { input.readLine() }.takeWhile { it.isNotEmpty() }.toList()
                val length = head.first { it.startsWith("Content-Length:", ignoreCase = true) }
                    .substringAfter(':').trim().toInt()
                val body = CharArray(length).also { input.read(it) }.concatToString()
                received = (head + body).joinToString("\n")
                val response = "HTTP/1.1 201 Created\r\nContent-Type: application/json\r\n" +
                    "Content-Length: 2\r\nConnection: close\r\n\r\n{}"
                socket.getOutputStream().write(response.toByteArray())
            }
        }
        val capture = mockk<CaptureEngine>(relaxed = true)
        val id = UUID.randomUUID()
        coEvery { capture.startTransaction(any(), any(), any(), any(), any(), any(), any()) } returns id
        val plan = ReplayPlan(
            "POST",
            "http://127.0.0.1:${server.localPort}/login",
            mapOf("Content-Type" to listOf("application/json"), "X-Replay" to listOf("1")),
            """{"a":1}""".toByteArray(),
        )

        val result = sendReplay(plan, plan.toOkHttpRequest(), capture)
        serverThread.join()
        server.close()

        received shouldContain "POST /login HTTP/1.1"
        received shouldContain "X-Replay: 1"
        received shouldContain """{"a":1}"""
        result.transactionId shouldBe id.toString()
        result.code shouldBe 201
        result.error shouldBe null
        coVerify {
            capture.completeTransaction(
                id = id,
                code = 201,
                message = "Created",
                headers = any(),
                bodyStream = match { Buffer().readFrom(it).readUtf8() == "{}" },
                bodySize = 2,
                protocol = "http/1.1",
                tlsVersion = null,
                error = null,
                durationMs = any(),
            )
        }
    }

    @Test
    fun `sendReplay records a connection failure on the new transaction`() = runTest {
        val port = ServerSocket(0).use { it.localPort }
        val capture = mockk<CaptureEngine>(relaxed = true)
        val id = UUID.randomUUID()
        coEvery { capture.startTransaction(any(), any(), any(), any(), any(), any(), any()) } returns id
        val plan = ReplayPlan("GET", "http://127.0.0.1:$port/", emptyMap(), null)

        val result = sendReplay(plan, plan.toOkHttpRequest(), capture)

        result.code shouldBe null
        result.error?.isNotEmpty() shouldBe true
        coVerify { capture.completeTransaction(id, 0, "FAILED", emptyMap(), null, 0, null, null, any(), any()) }
    }
}

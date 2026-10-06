package com.azikar24.wormaceptor.feature.viewer.export

import android.util.Base64
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HarExporterTest {

    @BeforeEach
    fun setUp() {
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), any()) } answers {
            java.util.Base64.getEncoder().encodeToString(firstArg<ByteArray>())
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Base64::class)
    }

    @Test
    fun `binary response body round-trips through base64 unchanged`() {
        // 0x89 and 0xFF are invalid UTF-8 sequences and would become U+FFFD if decoded as text
        val pngBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0xFF.toByte(), 0x00)
        val tx = transaction(contentType = "image/png", responseBodyRef = "res-blob")

        val log = HarExporter.toHarLog(listOf(tx), version = "test") { ref ->
            if (ref == "res-blob") pngBytes else null
        }

        val content = log.entries.single().response.content
        content.encoding shouldBe "base64"
        java.util.Base64.getDecoder().decode(content.text).toList() shouldBe pngBytes.toList()
    }

    @Test
    fun `text response body is exported as plain text`() {
        val json = """{"name":"Zoë"}"""
        val tx = transaction(contentType = "application/json", responseBodyRef = "res-blob")

        val log = HarExporter.toHarLog(listOf(tx), version = "test") { json.toByteArray(Charsets.UTF_8) }

        val content = log.entries.single().response.content
        content.encoding shouldBe null
        content.text shouldBe json
    }

    private fun transaction(
        contentType: String,
        responseBodyRef: String,
    ) = NetworkTransaction(
        timestamp = 1_700_000_000_000L,
        status = TransactionStatus.COMPLETED,
        durationMs = 10,
        request = Request(
            url = "https://api.example.com/image",
            method = "GET",
            headers = emptyMap(),
            bodyRef = null,
            bodySize = 0,
        ),
        response = Response(
            code = 200,
            message = "OK",
            headers = mapOf("Content-Type" to listOf(contentType)),
            bodyRef = responseBodyRef,
            bodySize = 10,
        ),
    )
}

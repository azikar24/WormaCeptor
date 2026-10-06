package com.azikar24.wormaceptor.feature.mockrules.vm

import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.mock.UrlMatchType
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import org.junit.jupiter.api.Test

class TransactionPrefillTest {

    @Test
    fun `maps method, exact full url, status, content type and text body`() {
        val tx = transaction(
            url = "https://api.example.com/v1/users/42?expand=true",
            method = "post",
            response = response(code = 201, message = "Created", contentType = "application/json; charset=utf-8"),
        )

        val prefill = buildTransactionPrefill(tx, """{"id":42}""".toByteArray())

        with(prefill.editor) {
            name shouldBe "POST /v1/users/42"
            urlPattern shouldBe "https://api.example.com/v1/users/42?expand=true"
            matchType shouldBe UrlMatchType.EXACT
            method shouldBe "POST"
            statusCodeText shouldBe "201"
            statusMessage shouldBe "Created"
            contentType shouldBe "application/json; charset=utf-8"
            responseBody shouldBe """{"id":42}"""
            delayType shouldBe DelayType.NONE
            isEditing shouldBe false
            isLoaded shouldBe true
        }
        prefill.bodyOmitted shouldBe false
    }

    @Test
    fun `binary body is left empty but content type is kept`() {
        val tx = transaction(response = response(contentType = "image/png"))

        val prefill = buildTransactionPrefill(tx, byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x00))

        prefill.editor.responseBody shouldBe ""
        prefill.editor.contentType shouldBe "image/png"
        prefill.bodyOmitted shouldBe true
    }

    @Test
    fun `invalid utf-8 without content type is treated as binary`() {
        val tx = transaction(response = response(contentType = null))

        val prefill = buildTransactionPrefill(tx, byteArrayOf(0xC3.toByte(), 0x28))

        prefill.editor.responseBody shouldBe ""
        prefill.bodyOmitted shouldBe true
    }

    @Test
    fun `body over the size cap is omitted`() {
        val tx = transaction(response = response(contentType = "text/plain"))

        val prefill = buildTransactionPrefill(tx, ByteArray(100_001) { 'a'.code.toByte() })

        prefill.editor.responseBody shouldBe ""
        prefill.bodyOmitted shouldBe true
    }

    @Test
    fun `in-flight transaction prefills 200 with empty body`() {
        val tx = transaction(response = null)

        val prefill = buildTransactionPrefill(tx, null)

        prefill.editor.statusCodeText shouldBe "200"
        prefill.editor.statusMessage shouldBe "OK"
        prefill.editor.responseBody shouldBe ""
        prefill.editor.isValid shouldBe true
        prefill.bodyOmitted shouldBe false
    }

    @Test
    fun `url without path is named with root and long paths are trimmed`() {
        buildTransactionPrefill(transaction(url = "https://api.example.com"), null)
            .editor.name shouldBe "GET /"

        val longName = buildTransactionPrefill(
            transaction(url = "https://api.example.com/" + "segment/".repeat(20)),
            null,
        ).editor.name
        longName.length shouldBe 80
        longName shouldEndWith "…"
    }

    private fun transaction(
        url: String = "https://api.example.com/items",
        method: String = "GET",
        response: Response? = response(),
    ) = NetworkTransaction(
        request = Request(url = url, method = method, headers = emptyMap(), bodyRef = null),
        response = response,
    )

    private fun response(
        code: Int = 200,
        message: String = "OK",
        contentType: String? = "application/json",
    ) = Response(
        code = code,
        message = message,
        headers = contentType?.let { mapOf("content-type" to listOf(it)) }.orEmpty(),
        bodyRef = "res",
    )
}

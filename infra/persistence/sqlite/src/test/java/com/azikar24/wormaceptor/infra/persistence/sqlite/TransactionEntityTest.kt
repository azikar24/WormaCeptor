package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class TransactionEntityTest {

    private val testId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

    private fun fullTransaction() = NetworkTransaction(
        id = testId,
        timestamp = 1_700_000_000_000L,
        durationMs = 250L,
        status = TransactionStatus.COMPLETED,
        request = Request(
            url = "https://api.example.com/users/42",
            method = "POST",
            headers = mapOf(
                "Content-Type" to listOf("application/json"),
                "Authorization" to listOf("Bearer abc123"),
            ),
            bodyRef = "blob_req_001",
            bodySize = 512,
        ),
        response = Response(
            code = 201,
            message = "Created",
            headers = mapOf("Location" to listOf("/users/42")),
            bodyRef = "blob_res_001",
            error = null,
            protocol = "h2",
            tlsVersion = "TLSv1.3",
            bodySize = 1024,
        ),
        extensions = mapOf("tag" to "test", "source" to "interceptor"),
    )

    private fun fullEntity() = TransactionEntity(
        id = testId,
        timestamp = 1_700_000_000_000L,
        durationMs = 250L,
        status = TransactionStatus.COMPLETED,
        reqUrl = "https://api.example.com/users/42",
        reqMethod = "POST",
        reqHeaders = mapOf(
            "Content-Type" to listOf("application/json"),
            "Authorization" to listOf("Bearer abc123"),
        ),
        reqBodyRef = "blob_req_001",
        reqBodySize = 512,
        resCode = 201,
        resMessage = "Created",
        resHeaders = mapOf("Location" to listOf("/users/42")),
        resBodyRef = "blob_res_001",
        resError = null,
        resProtocol = "h2",
        resTlsVersion = "TLSv1.3",
        resBodySize = 1024,
        extensions = mapOf("tag" to "test", "source" to "interceptor"),
    )

    @Nested
    inner class `toDomain` {

        @Test
        fun `maps all fields correctly for complete transaction`() {
            val entity = fullEntity()

            val domain = entity.toDomain()

            domain.id shouldBe testId
            domain.timestamp shouldBe 1_700_000_000_000L
            domain.durationMs shouldBe 250L
            domain.status shouldBe TransactionStatus.COMPLETED
            domain.request.url shouldBe "https://api.example.com/users/42"
            domain.request.method shouldBe "POST"
            domain.request.headers shouldBe mapOf(
                "Content-Type" to listOf("application/json"),
                "Authorization" to listOf("Bearer abc123"),
            )
            domain.request.bodyRef shouldBe "blob_req_001"
            domain.request.bodySize shouldBe 512
        }

        @Test
        fun `maps response fields correctly`() {
            val domain = fullEntity().toDomain()

            val response = domain.response
            response.shouldNotBeNull()
            response.code shouldBe 201
            response.message shouldBe "Created"
            response.headers shouldBe mapOf("Location" to listOf("/users/42"))
            response.bodyRef shouldBe "blob_res_001"
            response.error.shouldBeNull()
            response.protocol shouldBe "h2"
            response.tlsVersion shouldBe "TLSv1.3"
            response.bodySize shouldBe 1024
        }

        @Test
        fun `maps extensions correctly`() {
            val domain = fullEntity().toDomain()

            domain.extensions shouldBe mapOf("tag" to "test", "source" to "interceptor")
        }

        @Test
        fun `returns null response when resCode is null`() {
            val entity = fullEntity().copy(resCode = null)

            val domain = entity.toDomain()

            domain.response.shouldBeNull()
        }

        @Test
        fun `returns null response when resMessage is null`() {
            val entity = fullEntity().copy(resMessage = null)

            val domain = entity.toDomain()

            domain.response.shouldBeNull()
        }

        @Test
        fun `returns null response when resHeaders is null`() {
            val entity = fullEntity().copy(resHeaders = null)

            val domain = entity.toDomain()

            domain.response.shouldBeNull()
        }

        @Test
        fun `handles null durationMs`() {
            val entity = fullEntity().copy(durationMs = null)

            val domain = entity.toDomain()

            domain.durationMs.shouldBeNull()
        }

        @Test
        fun `handles null request body ref`() {
            val entity = fullEntity().copy(reqBodyRef = null)

            val domain = entity.toDomain()

            domain.request.bodyRef.shouldBeNull()
        }

        @Test
        fun `handles ACTIVE status`() {
            val entity = fullEntity().copy(
                status = TransactionStatus.ACTIVE,
                resCode = null,
                resMessage = null,
                resHeaders = null,
            )

            val domain = entity.toDomain()

            domain.status shouldBe TransactionStatus.ACTIVE
            domain.response.shouldBeNull()
        }

        @Test
        fun `handles FAILED status`() {
            val entity = fullEntity().copy(
                status = TransactionStatus.FAILED,
                resError = "Connection timeout",
                resCode = null,
                resMessage = null,
                resHeaders = null,
            )

            val domain = entity.toDomain()

            domain.status shouldBe TransactionStatus.FAILED
            domain.response.shouldBeNull()
        }

        @Test
        fun `preserves response error when present`() {
            val entity = fullEntity().copy(resError = "SSL Handshake Failed")

            val domain = entity.toDomain()

            domain.response.shouldNotBeNull()
            domain.response!!.error shouldBe "SSL Handshake Failed"
        }

        @Test
        fun `handles empty extensions`() {
            val entity = fullEntity().copy(extensions = emptyMap())

            val domain = entity.toDomain()

            domain.extensions shouldBe emptyMap()
        }

        @Test
        fun `handles empty request headers`() {
            val entity = fullEntity().copy(reqHeaders = emptyMap())

            val domain = entity.toDomain()

            domain.request.headers shouldBe emptyMap()
        }
    }

    @Nested
    inner class `fromDomain` {

        @Test
        fun `maps all request fields correctly`() {
            val domain = fullTransaction()

            val entity = TransactionEntity.fromDomain(domain)

            entity.id shouldBe testId
            entity.timestamp shouldBe 1_700_000_000_000L
            entity.durationMs shouldBe 250L
            entity.status shouldBe TransactionStatus.COMPLETED
            entity.reqUrl shouldBe "https://api.example.com/users/42"
            entity.reqMethod shouldBe "POST"
            entity.reqHeaders shouldBe mapOf(
                "Content-Type" to listOf("application/json"),
                "Authorization" to listOf("Bearer abc123"),
            )
            entity.reqBodyRef shouldBe "blob_req_001"
            entity.reqBodySize shouldBe 512
        }

        @Test
        fun `maps all response fields correctly`() {
            val entity = TransactionEntity.fromDomain(fullTransaction())

            entity.resCode shouldBe 201
            entity.resMessage shouldBe "Created"
            entity.resHeaders shouldBe mapOf("Location" to listOf("/users/42"))
            entity.resBodyRef shouldBe "blob_res_001"
            entity.resError.shouldBeNull()
            entity.resProtocol shouldBe "h2"
            entity.resTlsVersion shouldBe "TLSv1.3"
            entity.resBodySize shouldBe 1024
        }

        @Test
        fun `handles transaction with no response`() {
            val domain = fullTransaction().copy(response = null)

            val entity = TransactionEntity.fromDomain(domain)

            entity.resCode.shouldBeNull()
            entity.resMessage.shouldBeNull()
            entity.resHeaders.shouldBeNull()
            entity.resBodyRef.shouldBeNull()
            entity.resError.shouldBeNull()
            entity.resProtocol.shouldBeNull()
            entity.resTlsVersion.shouldBeNull()
            entity.resBodySize shouldBe 0
        }

        @Test
        fun `maps extensions correctly`() {
            val entity = TransactionEntity.fromDomain(fullTransaction())

            entity.extensions shouldBe mapOf("tag" to "test", "source" to "interceptor")
        }
    }

    @Nested
    inner class `round-trip` {

        @Test
        fun `fromDomain then toDomain preserves all fields`() {
            val original = fullTransaction()

            val entity = TransactionEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }

        @Test
        fun `round-trips transaction without response`() {
            val original = fullTransaction().copy(
                response = null,
                status = TransactionStatus.ACTIVE,
                durationMs = null,
            )

            val entity = TransactionEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }

        @Test
        fun `round-trips transaction with empty maps`() {
            val original = fullTransaction().copy(
                request = Request(
                    url = "https://example.com",
                    method = "GET",
                    headers = emptyMap(),
                    bodyRef = null,
                    bodySize = 0,
                ),
                extensions = emptyMap(),
            )

            val entity = TransactionEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }
    }
}

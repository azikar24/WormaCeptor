package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.contracts.BlobStorage
import com.azikar24.wormaceptor.domain.contracts.ExtensionContext
import com.azikar24.wormaceptor.domain.contracts.TransactionRepository
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.util.UUID

class CaptureEngineTest {

    private lateinit var repository: TransactionRepository
    private lateinit var blobStorage: BlobStorage
    private lateinit var extensionRegistry: ExtensionRegistry
    private lateinit var engine: CaptureEngine

    @BeforeEach
    fun setUp() {
        repository = mockk(relaxed = true)
        blobStorage = mockk(relaxed = true)
        extensionRegistry = mockk(relaxed = true)
        engine = CaptureEngine(repository, blobStorage, extensionRegistry)
    }

    @Nested
    inner class StartTransaction {

        @Test
        fun `should save transaction to repository`() = runTest {
            val transactionSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(transactionSlot)) } returns Unit

            engine.startTransaction(
                url = "https://example.com/api",
                method = "GET",
                headers = mapOf("Accept" to listOf("application/json")),
                bodyStream = null,
            )

            transactionSlot.isCaptured shouldBe true
            val saved = transactionSlot.captured
            saved.request.url shouldBe "https://example.com/api"
            saved.request.method shouldBe "GET"
            saved.request.headers shouldBe mapOf("Accept" to listOf("application/json"))
            saved.status shouldBe TransactionStatus.ACTIVE
        }

        @Test
        fun `should return a UUID`() = runTest {
            val id = engine.startTransaction(
                url = "https://example.com",
                method = "POST",
                headers = emptyMap(),
                bodyStream = null,
            )

            id shouldNotBe null
        }

        @Test
        fun `should save body blob when bodyStream is provided`() = runTest {
            val bodyContent = "request body"
            val bodyStream = ByteArrayInputStream(bodyContent.toByteArray())
            coEvery { blobStorage.saveBlob(any()) } returns "blob-123"

            val transactionSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(transactionSlot)) } returns Unit

            engine.startTransaction(
                url = "https://example.com",
                method = "POST",
                headers = emptyMap(),
                bodyStream = bodyStream,
                bodySize = bodyContent.length.toLong(),
            )

            coVerify { blobStorage.saveBlob(any()) }
            transactionSlot.captured.request.bodyRef shouldBe "blob-123"
            transactionSlot.captured.request.bodySize shouldBe bodyContent.length.toLong()
        }

        @Test
        fun `should not save blob when bodyStream is null`() = runTest {
            val transactionSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(transactionSlot)) } returns Unit

            engine.startTransaction(
                url = "https://example.com",
                method = "GET",
                headers = emptyMap(),
                bodyStream = null,
            )

            coVerify(exactly = 0) { blobStorage.saveBlob(any()) }
            transactionSlot.captured.request.bodyRef shouldBe null
        }

        @Test
        fun `should pass body size to request`() = runTest {
            val transactionSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(transactionSlot)) } returns Unit

            engine.startTransaction(
                url = "https://example.com",
                method = "POST",
                headers = emptyMap(),
                bodyStream = null,
                bodySize = 42,
            )

            transactionSlot.captured.request.bodySize shouldBe 42
        }
    }

    @Nested
    inner class CompleteTransaction {

        private val transactionId = UUID.randomUUID()
        private val originalRequest = Request(
            url = "https://example.com/api",
            method = "GET",
            headers = emptyMap(),
            bodyRef = null,
        )
        private val originalTransaction = NetworkTransaction(
            id = transactionId,
            request = originalRequest,
            timestamp = 1000L,
        )

        @Test
        fun `should update transaction with response data`() = runTest {
            coEvery { repository.getTransactionById(transactionId) } returns originalTransaction

            val updatedSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(updatedSlot)) } returns Unit
            every { extensionRegistry.extractAll(any()) } returns emptyMap()

            engine.completeTransaction(
                id = transactionId,
                code = 200,
                message = "OK",
                headers = mapOf("Content-Type" to listOf("application/json")),
                bodyStream = null,
            )

            val updated = updatedSlot.captured
            updated.response shouldNotBe null
            updated.response!!.code shouldBe 200
            updated.response!!.message shouldBe "OK"
        }

        @Test
        fun `should set COMPLETED status for successful response`() = runTest {
            coEvery { repository.getTransactionById(transactionId) } returns originalTransaction

            val updatedSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(updatedSlot)) } returns Unit
            every { extensionRegistry.extractAll(any()) } returns emptyMap()

            engine.completeTransaction(
                id = transactionId,
                code = 200,
                message = "OK",
                headers = emptyMap(),
                bodyStream = null,
            )

            updatedSlot.captured.status shouldBe TransactionStatus.COMPLETED
        }

        @Test
        fun `should set FAILED status when error is present`() = runTest {
            coEvery { repository.getTransactionById(transactionId) } returns originalTransaction

            val updatedSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(updatedSlot)) } returns Unit
            every { extensionRegistry.extractAll(any()) } returns emptyMap()

            engine.completeTransaction(
                id = transactionId,
                code = 200,
                message = "OK",
                headers = emptyMap(),
                bodyStream = null,
                error = "Connection reset",
            )

            updatedSlot.captured.status shouldBe TransactionStatus.FAILED
        }

        @Test
        fun `should set FAILED status for 4xx status codes`() = runTest {
            coEvery { repository.getTransactionById(transactionId) } returns originalTransaction

            val updatedSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(updatedSlot)) } returns Unit
            every { extensionRegistry.extractAll(any()) } returns emptyMap()

            engine.completeTransaction(
                id = transactionId,
                code = 404,
                message = "Not Found",
                headers = emptyMap(),
                bodyStream = null,
            )

            updatedSlot.captured.status shouldBe TransactionStatus.FAILED
        }

        @Test
        fun `should set FAILED status for 5xx status codes`() = runTest {
            coEvery { repository.getTransactionById(transactionId) } returns originalTransaction

            val updatedSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(updatedSlot)) } returns Unit
            every { extensionRegistry.extractAll(any()) } returns emptyMap()

            engine.completeTransaction(
                id = transactionId,
                code = 500,
                message = "Internal Server Error",
                headers = emptyMap(),
                bodyStream = null,
            )

            updatedSlot.captured.status shouldBe TransactionStatus.FAILED
        }

        @Test
        fun `should set COMPLETED status for 3xx status codes`() = runTest {
            coEvery { repository.getTransactionById(transactionId) } returns originalTransaction

            val updatedSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(updatedSlot)) } returns Unit
            every { extensionRegistry.extractAll(any()) } returns emptyMap()

            engine.completeTransaction(
                id = transactionId,
                code = 301,
                message = "Moved Permanently",
                headers = emptyMap(),
                bodyStream = null,
            )

            updatedSlot.captured.status shouldBe TransactionStatus.COMPLETED
        }

        @Test
        fun `should save response body blob when bodyStream is provided`() = runTest {
            coEvery { repository.getTransactionById(transactionId) } returns originalTransaction
            coEvery { blobStorage.saveBlob(any()) } returns "response-blob-456"
            every { extensionRegistry.extractAll(any()) } returns emptyMap()

            val updatedSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(updatedSlot)) } returns Unit

            val bodyStream = ByteArrayInputStream("response body".toByteArray())
            engine.completeTransaction(
                id = transactionId,
                code = 200,
                message = "OK",
                headers = emptyMap(),
                bodyStream = bodyStream,
                bodySize = 13,
            )

            updatedSlot.captured.response!!.bodyRef shouldBe "response-blob-456"
            updatedSlot.captured.response!!.bodySize shouldBe 13
        }

        @Test
        fun `should not save transaction when original not found`() = runTest {
            coEvery { repository.getTransactionById(any()) } returns null

            engine.completeTransaction(
                id = UUID.randomUUID(),
                code = 200,
                message = "OK",
                headers = emptyMap(),
                bodyStream = null,
            )

            coVerify(exactly = 0) { repository.saveTransaction(any()) }
        }

        @Test
        fun `should calculate duration from original timestamp`() = runTest {
            coEvery { repository.getTransactionById(transactionId) } returns originalTransaction

            val updatedSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(updatedSlot)) } returns Unit
            every { extensionRegistry.extractAll(any()) } returns emptyMap()

            engine.completeTransaction(
                id = transactionId,
                code = 200,
                message = "OK",
                headers = emptyMap(),
                bodyStream = null,
            )

            val duration = updatedSlot.captured.durationMs
            duration shouldNotBe null
            // Duration should be approximately now - 1000L (the original timestamp)
            // It should be a positive number
            (duration!! > 0) shouldBe true
        }

        @Test
        fun `should store protocol and tls version in response`() = runTest {
            coEvery { repository.getTransactionById(transactionId) } returns originalTransaction

            val updatedSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(updatedSlot)) } returns Unit
            every { extensionRegistry.extractAll(any()) } returns emptyMap()

            engine.completeTransaction(
                id = transactionId,
                code = 200,
                message = "OK",
                headers = emptyMap(),
                bodyStream = null,
                protocol = "h2",
                tlsVersion = "TLSv1.3",
            )

            updatedSlot.captured.response!!.protocol shouldBe "h2"
            updatedSlot.captured.response!!.tlsVersion shouldBe "TLSv1.3"
        }

        @Test
        fun `should extract extensions from registry`() = runTest {
            coEvery { repository.getTransactionById(transactionId) } returns originalTransaction

            val extensionData = mapOf("auth_type" to "Bearer", "api_version" to "v2")
            every { extensionRegistry.extractAll(any()) } returns extensionData

            val updatedSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(updatedSlot)) } returns Unit

            engine.completeTransaction(
                id = transactionId,
                code = 200,
                message = "OK",
                headers = emptyMap(),
                bodyStream = null,
            )

            updatedSlot.captured.extensions shouldBe extensionData
        }

        @Test
        fun `should pass correct context to extension registry`() = runTest {
            coEvery { repository.getTransactionById(transactionId) } returns originalTransaction

            val contextSlot = slot<ExtensionContext>()
            every { extensionRegistry.extractAll(capture(contextSlot)) } returns emptyMap()

            val updatedSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(updatedSlot)) } returns Unit

            engine.completeTransaction(
                id = transactionId,
                code = 200,
                message = "OK",
                headers = emptyMap(),
                bodyStream = null,
            )

            contextSlot.isCaptured shouldBe true
            contextSlot.captured.request shouldBe originalRequest
            contextSlot.captured.response shouldNotBe null
            contextSlot.captured.timestamp shouldBe 1000L
        }
    }

    @Nested
    inner class CompleteTransactionWithoutExtensionRegistry {

        @Test
        fun `should set empty extensions when registry is null`() = runTest {
            val engineWithoutRegistry = CaptureEngine(repository, blobStorage, null)
            val txId = UUID.randomUUID()
            val original = NetworkTransaction(
                id = txId,
                request = Request("https://example.com", "GET", emptyMap(), null),
                timestamp = 1000L,
            )
            coEvery { repository.getTransactionById(txId) } returns original

            val updatedSlot = slot<NetworkTransaction>()
            coEvery { repository.saveTransaction(capture(updatedSlot)) } returns Unit

            engineWithoutRegistry.completeTransaction(
                id = txId,
                code = 200,
                message = "OK",
                headers = emptyMap(),
                bodyStream = null,
            )

            updatedSlot.captured.extensions shouldBe emptyMap()
        }
    }

    @Nested
    inner class Cleanup {

        @Test
        fun `should delegate to repository deleteTransactionsBefore`() = runTest {
            val threshold = 1_234_567_890L

            engine.cleanup(threshold)

            coVerify { repository.deleteTransactionsBefore(threshold) }
        }
    }
}

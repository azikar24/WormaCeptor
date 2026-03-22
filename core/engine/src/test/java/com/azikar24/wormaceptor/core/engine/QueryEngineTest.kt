package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.contracts.BlobStorage
import com.azikar24.wormaceptor.domain.contracts.CrashRepository
import com.azikar24.wormaceptor.domain.contracts.TransactionRepository
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.util.UUID

class QueryEngineTest {

    private lateinit var repository: TransactionRepository
    private lateinit var blobStorage: BlobStorage
    private lateinit var crashRepository: CrashRepository
    private lateinit var engine: QueryEngine

    @BeforeEach
    fun setUp() {
        repository = mockk(relaxed = true)
        blobStorage = mockk(relaxed = true)
        crashRepository = mockk(relaxed = true)
        engine = QueryEngine(repository, blobStorage, crashRepository)
    }

    @Nested
    inner class ObserveTransactions {

        @Test
        fun `should delegate to repository getAllTransactions`() = runTest {
            val summaries = listOf(createTransactionSummary())
            every { repository.getAllTransactions() } returns flowOf(summaries)

            val result = engine.observeTransactions().first()

            result shouldBe summaries
        }

        @Test
        fun `should return empty list when no transactions`() = runTest {
            every { repository.getAllTransactions() } returns flowOf(emptyList())

            val result = engine.observeTransactions().first()

            result shouldBe emptyList()
        }
    }

    @Nested
    inner class ObserveCrashes {

        @Test
        fun `should delegate to crashRepository observeCrashes`() = runTest {
            val crashes = listOf(
                Crash(
                    id = 1,
                    timestamp = System.currentTimeMillis(),
                    exceptionType = "NullPointerException",
                    message = "null reference",
                    stackTrace = "at com.example.Main.run(Main.kt:10)",
                ),
            )
            every { crashRepository.observeCrashes() } returns flowOf(crashes)

            val result = engine.observeCrashes().first()

            result shouldBe crashes
        }

        @Test
        fun `should return empty flow when crashRepository is null`() = runTest {
            val engineWithoutCrashes = QueryEngine(repository, blobStorage, null)

            val result = engineWithoutCrashes.observeCrashes().first()

            result shouldBe emptyList()
        }
    }

    @Nested
    inner class Search {

        @Test
        fun `should delegate to repository searchTransactions for non-blank query`() = runTest {
            val summaries = listOf(createTransactionSummary())
            every { repository.searchTransactions("example") } returns flowOf(summaries)

            val result = engine.search("example").first()

            result shouldBe summaries
        }

        @Test
        fun `should return all transactions for blank query`() = runTest {
            val summaries = listOf(createTransactionSummary())
            every { repository.getAllTransactions() } returns flowOf(summaries)

            val result = engine.search("").first()

            result shouldBe summaries
        }

        @Test
        fun `should return all transactions for whitespace-only query`() = runTest {
            val summaries = listOf(createTransactionSummary())
            every { repository.getAllTransactions() } returns flowOf(summaries)

            val result = engine.search("   ").first()

            result shouldBe summaries
        }
    }

    @Nested
    inner class GetTransactionCount {

        @Test
        fun `should delegate to repository getTransactionCount`() = runTest {
            coEvery { repository.getTransactionCount("test") } returns 42

            val result = engine.getTransactionCount("test")

            result shouldBe 42
        }

        @Test
        fun `should pass null search query`() = runTest {
            coEvery { repository.getTransactionCount(null) } returns 100

            val result = engine.getTransactionCount(null)

            result shouldBe 100
        }
    }

    @Nested
    inner class GetDetails {

        @Test
        fun `should delegate to repository getTransactionById`() = runTest {
            val id = UUID.randomUUID()
            val transaction = NetworkTransaction(
                id = id,
                request = Request("https://example.com", "GET", emptyMap(), null),
            )
            coEvery { repository.getTransactionById(id) } returns transaction

            val result = engine.getDetails(id)

            result shouldBe transaction
        }

        @Test
        fun `should return null when transaction not found`() = runTest {
            val id = UUID.randomUUID()
            coEvery { repository.getTransactionById(id) } returns null

            val result = engine.getDetails(id)

            result shouldBe null
        }
    }

    @Nested
    inner class GetBody {

        @Test
        fun `should read blob and return string content`() = runTest {
            val blobId = "blob-123"
            val content = "response body content"
            coEvery { blobStorage.readBlob(blobId) } returns ByteArrayInputStream(content.toByteArray())

            val result = engine.getBody(blobId)

            result shouldBe content
        }

        @Test
        fun `should return null when blob not found`() = runTest {
            coEvery { blobStorage.readBlob("missing") } returns null

            val result = engine.getBody("missing")

            result shouldBe null
        }
    }

    @Nested
    inner class GetBodyBytes {

        @Test
        fun `should return raw bytes from blob storage`() = runTest {
            val blobId = "blob-456"
            val bytes = byteArrayOf(0x50, 0x44, 0x46) // "PDF"
            coEvery { blobStorage.readBlob(blobId) } returns ByteArrayInputStream(bytes)

            val result = engine.getBodyBytes(blobId)

            result shouldNotBe null
            result!!.toList() shouldBe bytes.toList()
        }

        @Test
        fun `should return null when blob not found`() = runTest {
            coEvery { blobStorage.readBlob("missing") } returns null

            val result = engine.getBodyBytes("missing")

            result shouldBe null
        }
    }

    @Nested
    inner class Clear {

        @Test
        fun `should delegate to repository clearAll`() = runTest {
            engine.clear()

            coVerify { repository.clearAll() }
        }
    }

    @Nested
    inner class ClearCrashes {

        @Test
        fun `should delegate to crashRepository clearCrashes`() = runTest {
            engine.clearCrashes()

            coVerify { crashRepository.clearCrashes() }
        }

        @Test
        fun `should not fail when crashRepository is null`() = runTest {
            val engineWithoutCrashes = QueryEngine(repository, blobStorage, null)

            engineWithoutCrashes.clearCrashes()
            // No exception thrown
        }
    }

    @Nested
    inner class GetAllTransactionsForExport {

        @Test
        fun `should delegate to repository getAllTransactionsAsList`() = runTest {
            val transactions = listOf(
                NetworkTransaction(
                    request = Request("https://example.com", "GET", emptyMap(), null),
                ),
            )
            coEvery { repository.getAllTransactionsAsList() } returns transactions

            val result = engine.getAllTransactionsForExport()

            result shouldBe transactions
        }
    }

    @Nested
    inner class DeleteTransactions {

        @Test
        fun `should delegate to repository deleteTransactions`() = runTest {
            val ids = listOf(UUID.randomUUID(), UUID.randomUUID())

            engine.deleteTransactions(ids)

            coVerify { repository.deleteTransactions(ids) }
        }
    }

    private fun createTransactionSummary(
        id: UUID = UUID.randomUUID(),
        method: String = "GET",
    ): TransactionSummary {
        return TransactionSummary(
            id = id,
            method = method,
            host = "example.com",
            path = "/api",
            code = 200,
            tookMs = 150,
            hasRequestBody = false,
            hasResponseBody = true,
            status = TransactionStatus.COMPLETED,
            timestamp = System.currentTimeMillis(),
        )
    }
}

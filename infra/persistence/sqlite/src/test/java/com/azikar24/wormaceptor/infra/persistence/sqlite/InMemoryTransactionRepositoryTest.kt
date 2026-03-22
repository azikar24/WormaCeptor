package com.azikar24.wormaceptor.infra.persistence.sqlite

import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryTransactionRepositoryTest {

    private val repository = InMemoryTransactionRepository()

    private fun createTransaction(
        id: UUID = UUID.randomUUID(),
        timestamp: Long = System.currentTimeMillis(),
        durationMs: Long? = 100L,
        status: TransactionStatus = TransactionStatus.COMPLETED,
        url: String = "https://api.example.com/users",
        method: String = "GET",
        headers: Map<String, List<String>> = mapOf("Accept" to listOf("application/json")),
        bodyRef: String? = null,
        bodySize: Long = 0,
        responseCode: Int? = 200,
        responseMessage: String? = "OK",
        responseHeaders: Map<String, List<String>>? = mapOf("Content-Type" to listOf("application/json")),
        responseBodyRef: String? = null,
        responseBodySize: Long = 0,
        extensions: Map<String, String> = emptyMap(),
    ): NetworkTransaction {
        val response = if (responseCode != null && responseMessage != null && responseHeaders != null) {
            Response(
                code = responseCode,
                message = responseMessage,
                headers = responseHeaders,
                bodyRef = responseBodyRef,
                bodySize = responseBodySize,
            )
        } else {
            null
        }
        return NetworkTransaction(
            id = id,
            timestamp = timestamp,
            durationMs = durationMs,
            status = status,
            request = Request(
                url = url,
                method = method,
                headers = headers,
                bodyRef = bodyRef,
                bodySize = bodySize,
            ),
            response = response,
            extensions = extensions,
        )
    }

    @Nested
    inner class `saveTransaction` {

        @Test
        fun `stores a transaction that can be retrieved by ID`() = runTest {
            val id = UUID.randomUUID()
            val tx = createTransaction(id = id, url = "https://api.example.com/save-test")

            repository.saveTransaction(tx)

            val result = repository.getTransactionById(id)
            result.shouldNotBeNull()
            result.request.url shouldBe "https://api.example.com/save-test"
        }

        @Test
        fun `overwrites existing transaction with same ID`() = runTest {
            val id = UUID.randomUUID()
            val original = createTransaction(id = id, url = "https://original.com")
            val updated = createTransaction(id = id, url = "https://updated.com")

            repository.saveTransaction(original)
            repository.saveTransaction(updated)

            val result = repository.getTransactionById(id)
            result.shouldNotBeNull()
            result.request.url shouldBe "https://updated.com"
        }

        @Test
        fun `preserves all fields`() = runTest {
            val id = UUID.randomUUID()
            val tx = NetworkTransaction(
                id = id,
                timestamp = 5000L,
                durationMs = 250L,
                status = TransactionStatus.COMPLETED,
                request = Request(
                    url = "https://api.example.com/data",
                    method = "POST",
                    headers = mapOf("Authorization" to listOf("Bearer token123")),
                    bodyRef = "blob_req_1",
                    bodySize = 512,
                ),
                response = Response(
                    code = 201,
                    message = "Created",
                    headers = mapOf("Location" to listOf("/data/42")),
                    bodyRef = "blob_res_1",
                    error = null,
                    protocol = "h2",
                    tlsVersion = "TLSv1.3",
                    bodySize = 1024,
                ),
                extensions = mapOf("tag" to "important"),
            )

            repository.saveTransaction(tx)

            val result = repository.getTransactionById(id)
            result.shouldNotBeNull()
            result.timestamp shouldBe 5000L
            result.durationMs shouldBe 250L
            result.status shouldBe TransactionStatus.COMPLETED
            result.request.url shouldBe "https://api.example.com/data"
            result.request.method shouldBe "POST"
            result.request.headers shouldBe mapOf("Authorization" to listOf("Bearer token123"))
            result.request.bodyRef shouldBe "blob_req_1"
            result.request.bodySize shouldBe 512
            result.response.shouldNotBeNull()
            result.response!!.code shouldBe 201
            result.response!!.message shouldBe "Created"
            result.response!!.protocol shouldBe "h2"
            result.response!!.tlsVersion shouldBe "TLSv1.3"
            result.response!!.bodySize shouldBe 1024
            result.extensions shouldBe mapOf("tag" to "important")
        }
    }

    @Nested
    inner class `getTransactionById` {

        @Test
        fun `returns null for non-existent ID`() = runTest {
            val result = repository.getTransactionById(UUID.randomUUID())

            result.shouldBeNull()
        }

        @Test
        fun `returns the stored transaction`() = runTest {
            val id = UUID.randomUUID()
            repository.saveTransaction(createTransaction(id = id, method = "DELETE"))

            val result = repository.getTransactionById(id)
            result.shouldNotBeNull()
            result.request.method shouldBe "DELETE"
        }
    }

    @Nested
    inner class `getAllTransactions` {

        @Test
        fun `emits empty list initially`() = runTest {
            repository.getAllTransactions().test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `returns summaries in reverse insertion order`() = runTest {
            repository.saveTransaction(createTransaction(url = "https://first.com/path", method = "GET"))
            repository.saveTransaction(createTransaction(url = "https://second.com/path", method = "POST"))
            repository.saveTransaction(createTransaction(url = "https://third.com/path", method = "PUT"))

            repository.getAllTransactions().test {
                val items = awaitItem()
                items shouldHaveSize 3
                items[0].method shouldBe "PUT"
                items[1].method shouldBe "POST"
                items[2].method shouldBe "GET"
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `summary contains correct host and path`() = runTest {
            repository.saveTransaction(createTransaction(url = "https://api.example.com/users/42"))

            repository.getAllTransactions().test {
                val summary = awaitItem().first()
                summary.host shouldBe "api.example.com"
                summary.path shouldBe "/users/42"
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `summary contains response code when available`() = runTest {
            repository.saveTransaction(createTransaction(responseCode = 404, responseMessage = "Not Found"))

            repository.getAllTransactions().test {
                val summary = awaitItem().first()
                summary.code shouldBe 404
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `summary has null code when no response`() = runTest {
            repository.saveTransaction(
                createTransaction(
                    responseCode = null,
                    responseMessage = null,
                    responseHeaders = null,
                    status = TransactionStatus.ACTIVE,
                ),
            )

            repository.getAllTransactions().test {
                val summary = awaitItem().first()
                summary.code.shouldBeNull()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `summary reflects hasRequestBody correctly`() = runTest {
            repository.saveTransaction(createTransaction(bodySize = 100))

            repository.getAllTransactions().test {
                awaitItem().first().hasRequestBody shouldBe true
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `summary reflects hasResponseBody correctly`() = runTest {
            repository.saveTransaction(createTransaction(responseBodySize = 500))

            repository.getAllTransactions().test {
                awaitItem().first().hasResponseBody shouldBe true
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `summary reflects transaction status`() = runTest {
            repository.saveTransaction(createTransaction(status = TransactionStatus.FAILED))

            repository.getAllTransactions().test {
                awaitItem().first().status shouldBe TransactionStatus.FAILED
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `getAllTransactionsAsList` {

        @Test
        fun `returns empty list initially`() = runTest {
            val result = repository.getAllTransactionsAsList()

            result.shouldBeEmpty()
        }

        @Test
        fun `returns all saved transactions`() = runTest {
            repository.saveTransaction(createTransaction())
            repository.saveTransaction(createTransaction())
            repository.saveTransaction(createTransaction())

            val result = repository.getAllTransactionsAsList()

            result shouldHaveSize 3
        }
    }

    @Nested
    inner class `clearAll` {

        @Test
        fun `removes all transactions`() = runTest {
            repository.saveTransaction(createTransaction())
            repository.saveTransaction(createTransaction())

            repository.clearAll()

            repository.getAllTransactionsAsList().shouldBeEmpty()
        }

        @Test
        fun `is safe to call on empty repository`() = runTest {
            repository.clearAll()

            repository.getAllTransactionsAsList().shouldBeEmpty()
        }

        @Test
        fun `getAllTransactions flow reflects cleared state`() = runTest {
            repository.saveTransaction(createTransaction())
            repository.clearAll()

            repository.getAllTransactions().test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `deleteTransactionsBefore` {

        @Test
        fun `removes transactions older than the given timestamp`() = runTest {
            repository.saveTransaction(createTransaction(timestamp = 1000L, method = "GET"))
            repository.saveTransaction(createTransaction(timestamp = 2000L, method = "POST"))
            repository.saveTransaction(createTransaction(timestamp = 3000L, method = "PUT"))

            repository.deleteTransactionsBefore(2000L)

            val remaining = repository.getAllTransactionsAsList()
            remaining shouldHaveSize 2
            remaining.all { it.timestamp >= 2000L } shouldBe true
        }

        @Test
        fun `keeps transactions at the exact threshold`() = runTest {
            repository.saveTransaction(createTransaction(timestamp = 5000L))

            repository.deleteTransactionsBefore(5000L)

            repository.getAllTransactionsAsList() shouldHaveSize 1
        }

        @Test
        fun `removes all when threshold is in the future`() = runTest {
            repository.saveTransaction(createTransaction(timestamp = 1000L))
            repository.saveTransaction(createTransaction(timestamp = 2000L))

            repository.deleteTransactionsBefore(Long.MAX_VALUE)

            repository.getAllTransactionsAsList().shouldBeEmpty()
        }

        @Test
        fun `removes nothing when threshold is zero`() = runTest {
            repository.saveTransaction(createTransaction(timestamp = 1000L))

            repository.deleteTransactionsBefore(0L)

            repository.getAllTransactionsAsList() shouldHaveSize 1
        }
    }

    @Nested
    inner class `deleteTransactions` {

        @Test
        fun `removes only the specified transactions`() = runTest {
            val id1 = UUID.randomUUID()
            val id2 = UUID.randomUUID()
            val id3 = UUID.randomUUID()
            repository.saveTransaction(createTransaction(id = id1))
            repository.saveTransaction(createTransaction(id = id2))
            repository.saveTransaction(createTransaction(id = id3))

            repository.deleteTransactions(listOf(id1, id3))

            val remaining = repository.getAllTransactionsAsList()
            remaining shouldHaveSize 1
            remaining.first().id shouldBe id2
        }

        @Test
        fun `does nothing with empty list`() = runTest {
            repository.saveTransaction(createTransaction())

            repository.deleteTransactions(emptyList())

            repository.getAllTransactionsAsList() shouldHaveSize 1
        }

        @Test
        fun `ignores non-existent IDs`() = runTest {
            val id = UUID.randomUUID()
            repository.saveTransaction(createTransaction(id = id))

            repository.deleteTransactions(listOf(UUID.randomUUID()))

            repository.getAllTransactionsAsList() shouldHaveSize 1
        }
    }

    @Nested
    inner class `searchTransactions` {

        @Test
        fun `finds transactions matching path`() = runTest {
            repository.saveTransaction(createTransaction(url = "https://api.example.com/users"))
            repository.saveTransaction(createTransaction(url = "https://api.example.com/products"))

            repository.searchTransactions("users").test {
                val results = awaitItem()
                results shouldHaveSize 1
                results.first().path shouldBe "/users"
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `finds transactions matching method`() = runTest {
            repository.saveTransaction(createTransaction(method = "GET", url = "https://a.com/x"))
            repository.saveTransaction(createTransaction(method = "POST", url = "https://b.com/y"))
            repository.saveTransaction(createTransaction(method = "GET", url = "https://c.com/z"))

            repository.searchTransactions("POST").test {
                val results = awaitItem()
                results shouldHaveSize 1
                results.first().method shouldBe "POST"
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `is case insensitive`() = runTest {
            repository.saveTransaction(createTransaction(url = "https://api.example.com/Users"))

            repository.searchTransactions("users").test {
                awaitItem() shouldHaveSize 1
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `returns empty when no match`() = runTest {
            repository.saveTransaction(createTransaction(url = "https://api.example.com/users"))

            repository.searchTransactions("nonexistent").test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `getTransactionCount` {

        @Test
        fun `returns zero when empty`() = runTest {
            val count = repository.getTransactionCount(null)

            count shouldBe 0
        }

        @Test
        fun `returns total count with null query`() = runTest {
            repository.saveTransaction(createTransaction())
            repository.saveTransaction(createTransaction())
            repository.saveTransaction(createTransaction())

            val count = repository.getTransactionCount(null)

            count shouldBe 3
        }

        @Test
        fun `returns total count with blank query`() = runTest {
            repository.saveTransaction(createTransaction())
            repository.saveTransaction(createTransaction())

            val count = repository.getTransactionCount("  ")

            count shouldBe 2
        }

        @Test
        fun `returns filtered count with search query matching URL`() = runTest {
            repository.saveTransaction(createTransaction(url = "https://api.example.com/users"))
            repository.saveTransaction(createTransaction(url = "https://api.example.com/products"))
            repository.saveTransaction(createTransaction(url = "https://api.example.com/users/42"))

            val count = repository.getTransactionCount("users")

            count shouldBe 2
        }

        @Test
        fun `returns filtered count with search query matching method`() = runTest {
            repository.saveTransaction(createTransaction(method = "GET"))
            repository.saveTransaction(createTransaction(method = "POST"))
            repository.saveTransaction(createTransaction(method = "GET"))

            val count = repository.getTransactionCount("POST")

            count shouldBe 1
        }

        @Test
        fun `search is case insensitive`() = runTest {
            repository.saveTransaction(createTransaction(url = "https://api.example.com/Users"))

            val count = repository.getTransactionCount("users")

            count shouldBe 1
        }
    }

    @Nested
    inner class `toSummary edge cases` {

        @Test
        fun `handles invalid URL gracefully for host`() = runTest {
            repository.saveTransaction(createTransaction(url = "not-a-valid-url"))

            repository.getAllTransactions().test {
                val summary = awaitItem().first()
                summary.host shouldBe ""
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `handles URL with no path`() = runTest {
            repository.saveTransaction(createTransaction(url = "https://example.com"))

            repository.getAllTransactions().test {
                val summary = awaitItem().first()
                summary.host shouldBe "example.com"
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `includes durationMs as tookMs in summary`() = runTest {
            repository.saveTransaction(createTransaction(durationMs = 350L))

            repository.getAllTransactions().test {
                awaitItem().first().tookMs shouldBe 350L
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `null durationMs results in null tookMs`() = runTest {
            repository.saveTransaction(createTransaction(durationMs = null))

            repository.getAllTransactions().test {
                awaitItem().first().tookMs.shouldBeNull()
                cancelAndConsumeRemainingEvents()
            }
        }
    }
}

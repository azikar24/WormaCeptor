package com.azikar24.wormaceptor.mcp.server.routes

import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.util.UUID

class WaitTest {

    private val existing = tx("https://api.test/login", "POST", 200, timestamp = 1)

    private fun tx(
        url: String,
        method: String = "GET",
        code: Int? = 200,
        status: TransactionStatus = TransactionStatus.COMPLETED,
        timestamp: Long = 10,
        id: UUID = UUID.randomUUID(),
    ) = TransactionSummary(
        id = id,
        method = method,
        host = "",
        path = "",
        code = code,
        tookMs = 5,
        hasRequestBody = false,
        hasResponseBody = false,
        status = status,
        timestamp = timestamp,
        url = url,
    )

    @Test
    fun `existing transactions never match`() {
        val pick = pickNewTransaction(listOf(existing), setOf(existing.id), TransactionFilter(urlContains = "login"))
        pick shouldBe null
    }

    @Test
    fun `in-flight transactions are skipped until they finish`() {
        val id = UUID.randomUUID()
        val active = tx("https://api.test/users", code = null, status = TransactionStatus.ACTIVE, id = id)

        pickNewTransaction(listOf(active), emptySet(), TransactionFilter()) shouldBe null
        val done = active.copy(code = 200, status = TransactionStatus.COMPLETED)
        pickNewTransaction(listOf(done), emptySet(), TransactionFilter()) shouldBe done
    }

    @Test
    fun `filters match url substring case-insensitively, method and exact status`() {
        val users = tx("https://API.test/users?page=2", "get", 500)
        val filter = TransactionFilter(urlContains = "api.test/USERS", method = "GET", status = 500)

        pickNewTransaction(listOf(users), emptySet(), filter) shouldBe users
        pickNewTransaction(listOf(users), emptySet(), filter.copy(status = 200)) shouldBe null
        pickNewTransaction(listOf(users), emptySet(), filter.copy(method = "POST")) shouldBe null
        pickNewTransaction(listOf(users), emptySet(), filter.copy(urlContains = "orders")) shouldBe null
    }

    @Test
    fun `the earliest new match wins`() {
        val later = tx("https://api.test/a", timestamp = 20)
        val earlier = tx("https://api.test/b", timestamp = 15)
        pickNewTransaction(listOf(later, earlier), emptySet(), TransactionFilter()) shouldBe earlier
    }

    @Test
    fun `awaitNew ignores the baseline and returns the next new item`() = runTest {
        val flow = MutableStateFlow(listOf(existing))
        val result = async {
            flow.awaitNew(
                30_000,
                key = { it.id },
            ) { items, seen -> pickNewTransaction(items, seen, TransactionFilter()) }
        }
        runCurrent()
        // The pre-existing transaction changing does not count as new.
        flow.value = listOf(existing.copy(tookMs = 99))
        runCurrent()
        result.isCompleted shouldBe false

        val fresh = tx("https://api.test/users")
        flow.value = listOf(fresh, existing)
        result.await() shouldBe fresh
    }

    @Test
    fun `awaitNew returns null when nothing new arrives in time`() = runTest {
        val flow = MutableStateFlow(listOf(existing))
        val result = async {
            flow.awaitNew(
                1_000,
                key = { it.id },
            ) { items, seen -> pickNewTransaction(items, seen, TransactionFilter()) }
        }
        advanceTimeBy(1_001)
        result.await() shouldBe null
    }

    @Test
    fun `only crashes recorded after the baseline are new`() = runTest {
        val old = Crash(id = 1, timestamp = 1, exceptionType = "Old", message = null, stackTrace = "")
        val new = Crash(id = 2, timestamp = 2, exceptionType = "IllegalStateException", message = "x", stackTrace = "")
        val flow = MutableStateFlow(listOf(old))
        val result = async { flow.awaitNew(30_000, key = { it.id }, pick = ::pickNewCrash) }
        runCurrent()
        flow.value = listOf(new, old)
        result.await() shouldBe new
    }
}

package com.azikar24.wormaceptor.api.internal

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.util.UUID

class TransactionWriteSequencerTest {

    private val errors = mutableListOf<Throwable>()

    private fun TestScope.sequencer() = TransactionWriteSequencer(
        CoroutineScope(
            SupervisorJob() + StandardTestDispatcher(testScheduler) +
                CoroutineExceptionHandler { _, throwable -> errors += throwable },
        ),
    )

    @Test
    fun `start returns the id before the insert runs`() = runTest {
        val sequencer = sequencer()
        val events = mutableListOf<String>()
        var insertedId: UUID? = null

        val id = sequencer.start {
            insertedId = it
            events += "insert"
        }

        events.shouldBeEmpty()
        advanceUntilIdle()
        events shouldContainExactly listOf("insert")
        insertedId shouldBe id
    }

    @Test
    fun `complete runs after a slow insert`() = runTest {
        val sequencer = sequencer()
        val events = mutableListOf<String>()

        val id = sequencer.start {
            delay(1_000)
            events += "insert"
        }
        sequencer.complete(id) { events += "update" }
        advanceUntilIdle()

        events shouldContainExactly listOf("insert", "update")
    }

    @Test
    fun `complete for an unknown id still runs`() = runTest {
        val sequencer = sequencer()
        var updated = false

        sequencer.complete(UUID.randomUUID()) { updated = true }
        advanceUntilIdle()

        updated shouldBe true
    }

    @Test
    fun `complete is skipped and reported when the insert fails`() = runTest {
        val sequencer = sequencer()
        var updated = false

        val id = sequencer.start { throw IllegalStateException("insert failed") }
        sequencer.complete(id) { updated = true }
        advanceUntilIdle()

        updated shouldBe false
        errors.single().shouldBeInstanceOf<IllegalStateException>()
    }

    @Test
    fun `finished inserts are no longer tracked even if never completed`() = runTest {
        val sequencer = sequencer()

        repeat(3) { sequencer.start { } }
        sequencer.start { throw IllegalStateException("insert failed") }
        sequencer.pendingCount shouldBe 4
        advanceUntilIdle()

        sequencer.pendingCount shouldBe 0
    }

    @Test
    fun `complete after the insert finished runs the update`() = runTest {
        val sequencer = sequencer()
        val events = mutableListOf<String>()

        val id = sequencer.start { events += "insert" }
        advanceUntilIdle()
        sequencer.complete(id) { events += "update" }
        advanceUntilIdle()

        events shouldContainExactly listOf("insert", "update")
    }
}

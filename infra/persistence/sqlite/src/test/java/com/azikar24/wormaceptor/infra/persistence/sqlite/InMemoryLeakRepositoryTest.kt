package com.azikar24.wormaceptor.infra.persistence.sqlite

import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryLeakRepositoryTest {

    private val repository = InMemoryLeakRepository()

    private fun createLeak(
        timestamp: Long = System.currentTimeMillis(),
        objectClass: String = "com.example.LeakedActivity",
        leakDescription: String = "Activity was not garbage collected",
        retainedSize: Long = 1024L,
        referencePath: List<String> = listOf("GCRoot", "Handler", "Activity"),
        severity: LeakSeverity = LeakSeverity.HIGH,
    ) = LeakInfo(
        timestamp = timestamp,
        objectClass = objectClass,
        leakDescription = leakDescription,
        retainedSize = retainedSize,
        referencePath = referencePath,
        severity = severity,
    )

    @Nested
    inner class `saveLeak` {

        @Test
        fun `adds a leak that can be observed`() = runTest {
            val leak = createLeak(objectClass = "com.example.MyActivity")

            repository.saveLeak(leak)

            repository.observeLeaks().test {
                val items = awaitItem()
                items shouldHaveSize 1
                items.first().objectClass shouldBe "com.example.MyActivity"
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `can save multiple leaks`() = runTest {
            repository.saveLeak(createLeak(objectClass = "Leak1"))
            repository.saveLeak(createLeak(objectClass = "Leak2"))
            repository.saveLeak(createLeak(objectClass = "Leak3"))

            repository.observeLeaks().test {
                awaitItem() shouldHaveSize 3
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `observeLeaks` {

        @Test
        fun `emits empty list initially`() = runTest {
            repository.observeLeaks().test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `returns leaks in reverse insertion order`() = runTest {
            repository.saveLeak(createLeak(objectClass = "First"))
            repository.saveLeak(createLeak(objectClass = "Second"))
            repository.saveLeak(createLeak(objectClass = "Third"))

            repository.observeLeaks().test {
                val items = awaitItem()
                items[0].objectClass shouldBe "Third"
                items[1].objectClass shouldBe "Second"
                items[2].objectClass shouldBe "First"
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `preserves all leak fields`() = runTest {
            val leak = LeakInfo(
                timestamp = 555L,
                objectClass = "com.example.Fragment",
                leakDescription = "Fragment retained after onDestroy",
                retainedSize = 2048L,
                referencePath = listOf("static field", "handler", "fragment"),
                severity = LeakSeverity.CRITICAL,
            )
            repository.saveLeak(leak)

            repository.observeLeaks().test {
                val result = awaitItem().first()
                result.timestamp shouldBe 555L
                result.objectClass shouldBe "com.example.Fragment"
                result.leakDescription shouldBe "Fragment retained after onDestroy"
                result.retainedSize shouldBe 2048L
                result.referencePath shouldBe listOf("static field", "handler", "fragment")
                result.severity shouldBe LeakSeverity.CRITICAL
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `handles all severity levels`() = runTest {
            repository.saveLeak(createLeak(severity = LeakSeverity.LOW))
            repository.saveLeak(createLeak(severity = LeakSeverity.MEDIUM))
            repository.saveLeak(createLeak(severity = LeakSeverity.HIGH))
            repository.saveLeak(createLeak(severity = LeakSeverity.CRITICAL))

            repository.observeLeaks().test {
                val items = awaitItem()
                items shouldHaveSize 4
                items.map { it.severity } shouldBe listOf(
                    LeakSeverity.CRITICAL,
                    LeakSeverity.HIGH,
                    LeakSeverity.MEDIUM,
                    LeakSeverity.LOW,
                )
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `handles empty reference path`() = runTest {
            repository.saveLeak(createLeak(referencePath = emptyList()))

            repository.observeLeaks().test {
                awaitItem().first().referencePath.shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `clearLeaks` {

        @Test
        fun `removes all leaks`() = runTest {
            repository.saveLeak(createLeak())
            repository.saveLeak(createLeak())

            repository.clearLeaks()

            repository.observeLeaks().test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `is safe to call on empty repository`() = runTest {
            repository.clearLeaks()

            repository.observeLeaks().test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `allows new leaks to be added after clearing`() = runTest {
            repository.saveLeak(createLeak(objectClass = "Old"))
            repository.clearLeaks()
            repository.saveLeak(createLeak(objectClass = "New"))

            repository.observeLeaks().test {
                val items = awaitItem()
                items shouldHaveSize 1
                items.first().objectClass shouldBe "New"
                cancelAndConsumeRemainingEvents()
            }
        }
    }
}

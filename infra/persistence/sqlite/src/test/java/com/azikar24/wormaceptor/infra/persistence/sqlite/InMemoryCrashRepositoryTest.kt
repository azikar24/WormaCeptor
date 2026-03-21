package com.azikar24.wormaceptor.infra.persistence.sqlite

import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.entities.Crash
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InMemoryCrashRepositoryTest {

    private val repository = InMemoryCrashRepository()

    private fun createCrash(
        id: Long = 0,
        timestamp: Long = System.currentTimeMillis(),
        exceptionType: String = "java.lang.RuntimeException",
        message: String? = "Something went wrong",
        stackTrace: String = "at com.example.Test.method(Test.kt:42)",
    ) = Crash(
        id = id,
        timestamp = timestamp,
        exceptionType = exceptionType,
        message = message,
        stackTrace = stackTrace,
    )

    @Nested
    inner class `saveCrash` {

        @Test
        fun `adds a crash that can be observed`() = runTest {
            val crash = createCrash(timestamp = 1000L)

            repository.saveCrash(crash)

            repository.observeCrashes().test {
                val items = awaitItem()
                items shouldHaveSize 1
                items.first().timestamp shouldBe 1000L
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `can save multiple crashes`() = runTest {
            repository.saveCrash(createCrash(timestamp = 1000L))
            repository.saveCrash(createCrash(timestamp = 2000L))
            repository.saveCrash(createCrash(timestamp = 3000L))

            repository.observeCrashes().test {
                awaitItem() shouldHaveSize 3
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `observeCrashes` {

        @Test
        fun `emits empty list initially`() = runTest {
            repository.observeCrashes().test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `returns crashes in reverse insertion order`() = runTest {
            repository.saveCrash(createCrash(id = 1, timestamp = 1000L, exceptionType = "First"))
            repository.saveCrash(createCrash(id = 2, timestamp = 2000L, exceptionType = "Second"))
            repository.saveCrash(createCrash(id = 3, timestamp = 3000L, exceptionType = "Third"))

            repository.observeCrashes().test {
                val items = awaitItem()
                items[0].exceptionType shouldBe "Third"
                items[1].exceptionType shouldBe "Second"
                items[2].exceptionType shouldBe "First"
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `preserves all crash fields`() = runTest {
            val crash = Crash(
                id = 42,
                timestamp = 999L,
                exceptionType = "java.lang.NullPointerException",
                message = "null reference",
                stackTrace = "at com.example.Foo.bar(Foo.kt:10)\nat com.example.Main.run(Main.kt:5)",
            )
            repository.saveCrash(crash)

            repository.observeCrashes().test {
                val result = awaitItem().first()
                result.id shouldBe 42
                result.timestamp shouldBe 999L
                result.exceptionType shouldBe "java.lang.NullPointerException"
                result.message shouldBe "null reference"
                result.stackTrace shouldBe "at com.example.Foo.bar(Foo.kt:10)\nat com.example.Main.run(Main.kt:5)"
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `handles crash with null message`() = runTest {
            repository.saveCrash(createCrash(message = null))

            repository.observeCrashes().test {
                val items = awaitItem()
                items.first().message shouldBe null
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    @Nested
    inner class `clearCrashes` {

        @Test
        fun `removes all crashes`() = runTest {
            repository.saveCrash(createCrash(timestamp = 1000L))
            repository.saveCrash(createCrash(timestamp = 2000L))

            repository.clearCrashes()

            repository.observeCrashes().test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `is safe to call on empty repository`() = runTest {
            repository.clearCrashes()

            repository.observeCrashes().test {
                awaitItem().shouldBeEmpty()
                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `allows new crashes to be added after clearing`() = runTest {
            repository.saveCrash(createCrash(timestamp = 1000L))
            repository.clearCrashes()
            repository.saveCrash(createCrash(timestamp = 2000L))

            repository.observeCrashes().test {
                val items = awaitItem()
                items shouldHaveSize 1
                items.first().timestamp shouldBe 2000L
                cancelAndConsumeRemainingEvents()
            }
        }
    }
}

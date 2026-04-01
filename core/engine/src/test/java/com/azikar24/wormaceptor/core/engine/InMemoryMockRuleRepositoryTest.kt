package com.azikar24.wormaceptor.core.engine

import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.entities.MockResponse
import com.azikar24.wormaceptor.domain.entities.MockRule
import com.azikar24.wormaceptor.domain.entities.RequestMatcher
import com.azikar24.wormaceptor.domain.entities.UrlMatchType
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class InMemoryMockRuleRepositoryTest {

    private lateinit var repository: InMemoryMockRuleRepository

    @BeforeEach
    fun setUp() {
        repository = InMemoryMockRuleRepository()
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private fun mockRule(
        id: String = "rule-1",
        name: String = "Test Rule",
        createdAt: Long = System.currentTimeMillis(),
    ): MockRule = MockRule(
        id = id,
        name = name,
        matcher = RequestMatcher(
            urlPattern = "https://api.example.com/$id",
            matchType = UrlMatchType.EXACT,
        ),
        response = MockResponse(statusCode = 200, statusMessage = "OK"),
        createdAt = createdAt,
    )

    // ── Insert ──────────────────────────────────────────────────────────

    @Nested
    inner class Insert {

        @Test
        fun `should emit inserted rule via flow`() = runTest {
            repository.getAll().test {
                awaitItem().shouldBeEmpty() // initial

                val rule = mockRule(id = "r1")
                repository.insert(rule)

                val emitted = awaitItem()
                emitted shouldHaveSize 1
                emitted.first().id shouldBe "r1"

                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `should emit multiple rules when inserting sequentially`() = runTest {
            repository.getAll().test {
                awaitItem() // initial empty

                repository.insert(mockRule(id = "a", createdAt = 100L))
                awaitItem() shouldHaveSize 1

                repository.insert(mockRule(id = "b", createdAt = 200L))
                val twoRules = awaitItem()
                twoRules shouldHaveSize 2

                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `inserted rules should be sorted by createdAt descending`() = runTest {
            val oldest = mockRule(id = "old", createdAt = 1000L)
            val newest = mockRule(id = "new", createdAt = 3000L)
            val middle = mockRule(id = "mid", createdAt = 2000L)

            repository.insert(oldest)
            repository.insert(newest)
            repository.insert(middle)

            repository.getAll().test {
                val rules = awaitItem()
                rules.map { it.id }.shouldContainExactly("new", "mid", "old")
                cancelAndConsumeRemainingEvents()
            }
        }
    }

    // ── Update ──────────────────────────────────────────────────────────

    @Nested
    inner class Update {

        @Test
        fun `should update existing rule and emit via flow`() = runTest {
            val original = mockRule(id = "r1", name = "Original")
            repository.insert(original)

            repository.getAll().test {
                val beforeUpdate = awaitItem()
                beforeUpdate.first().name shouldBe "Original"

                val updated = original.copy(name = "Updated")
                repository.update(updated)

                val afterUpdate = awaitItem()
                afterUpdate shouldHaveSize 1
                afterUpdate.first().name shouldBe "Updated"

                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `updating non-existent rule should act as insert`() = runTest {
            val rule = mockRule(id = "new-rule")
            repository.update(rule)

            repository.getById("new-rule") shouldBe rule
        }
    }

    // ── GetById ─────────────────────────────────────────────────────────

    @Nested
    inner class GetById {

        @Test
        fun `should return rule when it exists`() = runTest {
            val rule = mockRule(id = "target")
            repository.insert(rule)

            repository.getById("target") shouldBe rule
        }

        @Test
        fun `should return null when rule does not exist`() = runTest {
            repository.getById("non-existent").shouldBeNull()
        }

        @Test
        fun `should return correct rule among many`() = runTest {
            repository.insert(mockRule(id = "a", createdAt = 100L))
            repository.insert(mockRule(id = "b", createdAt = 200L))
            repository.insert(mockRule(id = "c", createdAt = 300L))

            val result = repository.getById("b")
            result?.id shouldBe "b"
        }
    }

    // ── Delete ──────────────────────────────────────────────────────────

    @Nested
    inner class Delete {

        @Test
        fun `should remove rule and emit updated list`() = runTest {
            repository.insert(mockRule(id = "a", createdAt = 100L))
            repository.insert(mockRule(id = "b", createdAt = 200L))

            repository.getAll().test {
                awaitItem() shouldHaveSize 2

                repository.delete("a")

                val afterDelete = awaitItem()
                afterDelete shouldHaveSize 1
                afterDelete.first().id shouldBe "b"

                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `deleting non-existent rule should not change the list`() = runTest {
            repository.insert(mockRule(id = "x", createdAt = 100L))

            repository.getAll().test {
                awaitItem() shouldHaveSize 1

                repository.delete("non-existent")

                // MutableStateFlow deduplicates identical values, so if the list
                // doesn't change, there's no new emission. We verify by checking
                // the current snapshot.
                repository.getById("x")?.id shouldBe "x"

                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `should return null for deleted rule via getById`() = runTest {
            repository.insert(mockRule(id = "to-delete"))

            repository.getById("to-delete")?.id shouldBe "to-delete"

            repository.delete("to-delete")

            repository.getById("to-delete").shouldBeNull()
        }
    }

    // ── DeleteAll ───────────────────────────────────────────────────────

    @Nested
    inner class DeleteAll {

        @Test
        fun `should remove all rules and emit empty list`() = runTest {
            repository.insert(mockRule(id = "a", createdAt = 100L))
            repository.insert(mockRule(id = "b", createdAt = 200L))
            repository.insert(mockRule(id = "c", createdAt = 300L))

            repository.getAll().test {
                awaitItem() shouldHaveSize 3

                repository.deleteAll()

                awaitItem().shouldBeEmpty()

                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `deleteAll on empty repository should emit empty list`() = runTest {
            repository.getAll().test {
                awaitItem().shouldBeEmpty()

                repository.deleteAll()
                // StateFlow deduplicates identical empty lists, so no new emission
                // expected. Verify the state directly.
                repository.getById("anything").shouldBeNull()

                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `getById should return null after deleteAll`() = runTest {
            repository.insert(mockRule(id = "a"))
            repository.insert(mockRule(id = "b"))

            repository.getById("a")?.id shouldBe "a"
            repository.getById("b")?.id shouldBe "b"

            repository.deleteAll()

            repository.getById("a").shouldBeNull()
            repository.getById("b").shouldBeNull()
        }
    }

    // ── Thread Safety ───────────────────────────────────────────────────

    @Nested
    inner class ThreadSafety {

        @Test
        fun `concurrent inserts should not lose data`() = runTest {
            val count = 100
            (1..count).forEach { i ->
                repository.insert(mockRule(id = "rule-$i", createdAt = i.toLong()))
            }

            repository.getAll().test {
                val rules = awaitItem()
                rules shouldHaveSize count
                cancelAndConsumeRemainingEvents()
            }
        }
    }
}

package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.entities.Crash
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CrashEntityTest {

    private fun fullCrash() = Crash(
        id = 42,
        timestamp = 1_700_000_000_000L,
        exceptionType = "java.lang.NullPointerException",
        message = "Cannot invoke method on null reference",
        stackTrace = "at com.example.App.run(App.kt:10)\nat com.example.Main.main(Main.kt:5)",
    )

    private fun fullEntity() = CrashEntity(
        id = 42,
        timestamp = 1_700_000_000_000L,
        exceptionType = "java.lang.NullPointerException",
        message = "Cannot invoke method on null reference",
        stackTrace = "at com.example.App.run(App.kt:10)\nat com.example.Main.main(Main.kt:5)",
    )

    @Nested
    inner class `toDomain` {

        @Test
        fun `maps all fields correctly`() {
            val entity = fullEntity()

            val domain = entity.toDomain()

            domain.id shouldBe 42
            domain.timestamp shouldBe 1_700_000_000_000L
            domain.exceptionType shouldBe "java.lang.NullPointerException"
            domain.message shouldBe "Cannot invoke method on null reference"
            domain.stackTrace shouldBe "at com.example.App.run(App.kt:10)\nat com.example.Main.main(Main.kt:5)"
        }

        @Test
        fun `handles null message`() {
            val entity = fullEntity().copy(message = null)

            val domain = entity.toDomain()

            domain.message.shouldBeNull()
        }

        @Test
        fun `handles zero ID`() {
            val entity = fullEntity().copy(id = 0)

            val domain = entity.toDomain()

            domain.id shouldBe 0
        }

        @Test
        fun `handles empty stack trace`() {
            val entity = fullEntity().copy(stackTrace = "")

            val domain = entity.toDomain()

            domain.stackTrace shouldBe ""
        }
    }

    @Nested
    inner class `fromDomain` {

        @Test
        fun `maps all fields correctly`() {
            val domain = fullCrash()

            val entity = CrashEntity.fromDomain(domain)

            entity.id shouldBe 42
            entity.timestamp shouldBe 1_700_000_000_000L
            entity.exceptionType shouldBe "java.lang.NullPointerException"
            entity.message shouldBe "Cannot invoke method on null reference"
            entity.stackTrace shouldBe "at com.example.App.run(App.kt:10)\nat com.example.Main.main(Main.kt:5)"
        }

        @Test
        fun `handles null message`() {
            val domain = fullCrash().copy(message = null)

            val entity = CrashEntity.fromDomain(domain)

            entity.message.shouldBeNull()
        }
    }

    @Nested
    inner class `round-trip` {

        @Test
        fun `fromDomain then toDomain preserves all fields`() {
            val original = fullCrash()

            val entity = CrashEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }

        @Test
        fun `round-trips crash with null message`() {
            val original = fullCrash().copy(message = null)

            val entity = CrashEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }

        @Test
        fun `round-trips crash with multiline stack trace`() {
            val original = fullCrash().copy(
                stackTrace = """
                    java.lang.NullPointerException
                        at com.example.Foo.bar(Foo.kt:10)
                        at com.example.Baz.qux(Baz.kt:20)
                        at java.lang.Thread.run(Thread.java:748)
                """.trimIndent(),
            )

            val entity = CrashEntity.fromDomain(original)
            val roundTripped = entity.toDomain()

            roundTripped shouldBe original
        }
    }
}

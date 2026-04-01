package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TransactionStatusTest {

    @Nested
    inner class EnumValues {

        @Test
        fun `should have exactly three values`() {
            TransactionStatus.entries.size shouldBe 3
        }

        @Test
        fun `should contain ACTIVE, COMPLETED, and FAILED`() {
            TransactionStatus.entries.map { it.name } shouldContainExactly listOf(
                "ACTIVE",
                "COMPLETED",
                "FAILED",
            )
        }

        @Test
        fun `valueOf should resolve ACTIVE`() {
            TransactionStatus.valueOf("ACTIVE") shouldBe TransactionStatus.ACTIVE
        }

        @Test
        fun `valueOf should resolve COMPLETED`() {
            TransactionStatus.valueOf("COMPLETED") shouldBe TransactionStatus.COMPLETED
        }

        @Test
        fun `valueOf should resolve FAILED`() {
            TransactionStatus.valueOf("FAILED") shouldBe TransactionStatus.FAILED
        }
    }
}

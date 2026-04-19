package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StorageModeTest {

    @Nested
    inner class EnumValues {

        @Test
        fun `should have exactly three values`() {
            StorageMode.entries.size shouldBe 3
        }

        @Test
        fun `should contain PERSISTENT, MEMORY, and NO_OP in order`() {
            StorageMode.entries.map { it.name } shouldContainExactly listOf(
                "PERSISTENT",
                "MEMORY",
                "NO_OP",
            )
        }

        @Test
        fun `valueOf should resolve PERSISTENT`() {
            StorageMode.valueOf("PERSISTENT") shouldBe StorageMode.PERSISTENT
        }

        @Test
        fun `valueOf should resolve MEMORY`() {
            StorageMode.valueOf("MEMORY") shouldBe StorageMode.MEMORY
        }

        @Test
        fun `valueOf should resolve NO_OP`() {
            StorageMode.valueOf("NO_OP") shouldBe StorageMode.NO_OP
        }
    }
}

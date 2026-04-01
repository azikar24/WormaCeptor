package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TypesTest {

    @Nested
    inner class TypeAliases {

        @Test
        fun `BlobID is an alias for String`() {
            val blobId: BlobID = "blob_001"

            blobId shouldBe "blob_001"
        }

        @Test
        fun `EpochMillis is an alias for Long`() {
            val timestamp: EpochMillis = 1_700_000_000_000L

            timestamp shouldBe 1_700_000_000_000L
        }

        @Test
        fun `BlobID can be used where String is expected`() {
            val id: BlobID = "test"
            val asString: String = id

            asString shouldBe "test"
        }

        @Test
        fun `EpochMillis can be used where Long is expected`() {
            val millis: EpochMillis = 42L
            val asLong: Long = millis

            asLong shouldBe 42L
        }
    }
}

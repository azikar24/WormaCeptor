package com.azikar24.wormaceptor.domain.entities

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExportFormatTest {

    @Nested
    inner class EnumValues {

        @Test
        fun `should have exactly three values`() {
            ExportFormat.entries.size shouldBe 3
        }

        @Test
        fun `should contain JSON, HAR, and CURL in order`() {
            ExportFormat.entries.map { it.name } shouldContainExactly listOf(
                "JSON",
                "HAR",
                "CURL",
            )
        }

        @Test
        fun `valueOf should resolve JSON`() {
            ExportFormat.valueOf("JSON") shouldBe ExportFormat.JSON
        }

        @Test
        fun `valueOf should resolve HAR`() {
            ExportFormat.valueOf("HAR") shouldBe ExportFormat.HAR
        }

        @Test
        fun `valueOf should resolve CURL`() {
            ExportFormat.valueOf("CURL") shouldBe ExportFormat.CURL
        }
    }
}

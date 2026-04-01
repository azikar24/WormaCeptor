package com.azikar24.wormaceptor.api

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FeatureTest {

    @Nested
    inner class `ALL set` {

        @Test
        fun `contains every enum entry`() {
            Feature.ALL shouldBe Feature.entries.toSet()
        }

        @Test
        fun `size matches entries count`() {
            Feature.ALL shouldHaveSize Feature.entries.size
        }
    }

    @Nested
    inner class `DEFAULT set` {

        @Test
        fun `DEFAULT equals ALL`() {
            Feature.DEFAULT shouldBe Feature.ALL
        }

        @Test
        fun `DEFAULT contains all entries`() {
            Feature.DEFAULT shouldContainAll Feature.entries
        }
    }

    @Nested
    inner class `CORE set` {

        @Test
        fun `contains CONSOLE_LOGS and DEVICE_INFO`() {
            Feature.CORE shouldContainAll listOf(Feature.CONSOLE_LOGS, Feature.DEVICE_INFO)
        }

        @Test
        fun `has exactly two features`() {
            Feature.CORE shouldHaveSize 2
        }

        @Test
        fun `is a subset of ALL`() {
            Feature.ALL.containsAll(Feature.CORE) shouldBe true
        }
    }

    @Nested
    inner class `enum entries` {

        @Test
        fun `MOCK_RULES feature exists`() {
            Feature.ALL shouldContain Feature.MOCK_RULES
        }

        @Test
        fun `COMPOSE_RECOMPOSITION_INSPECTOR feature exists`() {
            Feature.ALL shouldContain Feature.COMPOSE_RECOMPOSITION_INSPECTOR
        }

        @Test
        fun `inspection features exist`() {
            Feature.ALL shouldContainAll listOf(
                Feature.SHARED_PREFERENCES,
                Feature.DATABASE_BROWSER,
                Feature.FILE_BROWSER,
                Feature.LOADED_LIBRARIES,
                Feature.DEPENDENCIES_INSPECTOR,
                Feature.SECURE_STORAGE,
                Feature.WEBVIEW_MONITOR,
            )
        }

        @Test
        fun `performance features exist`() {
            Feature.ALL shouldContainAll listOf(
                Feature.MEMORY_MONITOR,
                Feature.FPS_MONITOR,
                Feature.CPU_MONITOR,
                Feature.LEAK_DETECTION,
                Feature.THREAD_VIOLATIONS,
                Feature.COMPOSE_RECOMPOSITION_INSPECTOR,
            )
        }

        @Test
        fun `network features exist`() {
            Feature.ALL shouldContainAll listOf(
                Feature.WEBSOCKET_MONITOR,
                Feature.RATE_LIMITER,
                Feature.MOCK_RULES,
            )
        }

        @Test
        fun `simulation features exist`() {
            Feature.ALL shouldContainAll listOf(
                Feature.LOCATION_SIMULATOR,
                Feature.PUSH_SIMULATOR,
                Feature.PUSH_TOKEN_MANAGER,
                Feature.CRYPTO_TOOL,
            )
        }

        @Test
        fun `core features exist`() {
            Feature.ALL shouldContainAll listOf(
                Feature.CONSOLE_LOGS,
                Feature.DEVICE_INFO,
            )
        }

        @Test
        fun `total enum count is 22`() {
            Feature.entries shouldHaveSize 22
        }
    }
}

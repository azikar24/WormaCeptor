package com.azikar24.wormaceptor.core.engine

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicReference

class CoreHolderTest {

    @BeforeEach
    fun setUp() {
        // Reset CoreHolder's internal state via reflection since it's an object singleton
        val enginesField = CoreHolder::class.java.getDeclaredField("engines")
        enginesField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val atomicRef = enginesField.get(CoreHolder) as AtomicReference<Any?>
        atomicRef.set(null)
    }

    @Nested
    inner class InitialState {

        @Test
        fun `captureEngine should be null before initialization`() {
            CoreHolder.captureEngine shouldBe null
        }

        @Test
        fun `queryEngine should be null before initialization`() {
            CoreHolder.queryEngine shouldBe null
        }

        @Test
        fun `extensionRegistry should be null before initialization`() {
            CoreHolder.extensionRegistry shouldBe null
        }
    }

    @Nested
    inner class Initialize {

        @Test
        fun `should return true on first initialization`() {
            val capture = mockk<CaptureEngine>()
            val query = mockk<QueryEngine>()

            val result = CoreHolder.initialize(capture, query)

            result shouldBe true
        }

        @Test
        fun `should set captureEngine after initialization`() {
            val capture = mockk<CaptureEngine>()
            val query = mockk<QueryEngine>()

            CoreHolder.initialize(capture, query)

            CoreHolder.captureEngine shouldBe capture
        }

        @Test
        fun `should set queryEngine after initialization`() {
            val capture = mockk<CaptureEngine>()
            val query = mockk<QueryEngine>()

            CoreHolder.initialize(capture, query)

            CoreHolder.queryEngine shouldBe query
        }

        @Test
        fun `should set extensionRegistry when provided`() {
            val capture = mockk<CaptureEngine>()
            val query = mockk<QueryEngine>()
            val extensions = mockk<ExtensionRegistry>()

            CoreHolder.initialize(capture, query, extensions)

            CoreHolder.extensionRegistry shouldBe extensions
        }

        @Test
        fun `extensionRegistry should be null when not provided`() {
            val capture = mockk<CaptureEngine>()
            val query = mockk<QueryEngine>()

            CoreHolder.initialize(capture, query)

            CoreHolder.extensionRegistry shouldBe null
        }

        @Test
        fun `should return false on second initialization`() {
            val capture1 = mockk<CaptureEngine>()
            val query1 = mockk<QueryEngine>()
            val capture2 = mockk<CaptureEngine>()
            val query2 = mockk<QueryEngine>()

            CoreHolder.initialize(capture1, query1)
            val result = CoreHolder.initialize(capture2, query2)

            result shouldBe false
        }

        @Test
        fun `should keep first engines after failed second initialization`() {
            val capture1 = mockk<CaptureEngine>()
            val query1 = mockk<QueryEngine>()
            val capture2 = mockk<CaptureEngine>()
            val query2 = mockk<QueryEngine>()

            CoreHolder.initialize(capture1, query1)
            CoreHolder.initialize(capture2, query2)

            CoreHolder.captureEngine shouldBe capture1
            CoreHolder.queryEngine shouldBe query1
        }
    }

    @Nested
    inner class ThreadSafety {

        @Test
        fun `concurrent initialization should only succeed once`() {
            val capture = mockk<CaptureEngine>()
            val query = mockk<QueryEngine>()

            val results = (1..10).map { index ->
                val threadCapture = mockk<CaptureEngine>()
                val threadQuery = mockk<QueryEngine>()
                Thread {
                    CoreHolder.initialize(threadCapture, threadQuery)
                }.also { it.start() }
            }

            results.forEach { it.join() }

            // Only one should have succeeded, so engines should not be null
            CoreHolder.captureEngine shouldNotBe null
            CoreHolder.queryEngine shouldNotBe null
        }
    }
}

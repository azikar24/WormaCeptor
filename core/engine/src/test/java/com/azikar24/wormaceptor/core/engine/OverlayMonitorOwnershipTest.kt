package com.azikar24.wormaceptor.core.engine

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class OverlayMonitorOwnershipTest {

    private var running = false
    private var stopCalls = 0
    private val ownership = OverlayMonitorOwnership(
        isRunning = { running },
        start = { running = true },
        stop = {
            running = false
            stopCalls++
        },
    )

    @Test
    fun `release stops an engine the overlay started`() {
        ownership.acquire()
        ownership.release()

        running shouldBe false
        stopCalls shouldBe 1
    }

    @Test
    fun `release leaves an engine running that a feature screen started`() {
        running = true

        ownership.acquire()
        ownership.release()

        running shouldBe true
        stopCalls shouldBe 0
    }

    @Test
    fun `release after an external stop and restart does not stop the new owner`() {
        ownership.acquire()
        running = false
        ownership.onEngineStopped()
        running = true

        ownership.release()

        running shouldBe true
        stopCalls shouldBe 0
    }

    @Test
    fun `second release is a no-op`() {
        ownership.acquire()
        ownership.release()
        running = true

        ownership.release()

        running shouldBe true
        stopCalls shouldBe 1
    }
}

package com.azikar24.wormaceptor.feature.recomposition

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SlidingWindowRateTest {

    private val rate = SlidingWindowRate(windowMs = 2_000L, capacity = 8)

    @Test
    fun `single event right after start is averaged over the window`() {
        rate.record(nowMs = 10_000L)

        rate.ratePerSecond(nowMs = 10_001L) shouldBe 0.5f
    }

    @Test
    fun `events older than the window no longer count`() {
        repeat(4) { rate.record(nowMs = 10_000L + it * 100) }

        rate.ratePerSecond(nowMs = 10_500L) shouldBe 2f
        rate.ratePerSecond(nowMs = 13_000L) shouldBe 0f
    }

    @Test
    fun `rate saturates at capacity`() {
        repeat(20) { rate.record(nowMs = 10_000L + it) }

        rate.ratePerSecond(nowMs = 10_100L) shouldBe 4f
    }

    @Test
    fun `no events gives zero`() {
        rate.ratePerSecond(nowMs = 10_000L) shouldBe 0f
    }
}

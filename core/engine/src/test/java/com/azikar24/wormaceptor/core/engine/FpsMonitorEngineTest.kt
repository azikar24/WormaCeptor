package com.azikar24.wormaceptor.core.engine

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FpsMonitorEngineTest {

    private val period60Hz = FpsMonitorEngine.framePeriodNanos(60f)
    private val period120Hz = FpsMonitorEngine.framePeriodNanos(120f)

    @Test
    fun `frame period follows refresh rate`() {
        period60Hz shouldBe 16_666_666L
        period120Hz shouldBe 8_333_333L
    }

    @Test
    fun `unknown refresh rate falls back to 60Hz`() {
        FpsMonitorEngine.framePeriodNanos(0f) shouldBe period60Hz
    }

    @Test
    fun `frame slightly over one period at 60Hz is not dropped`() {
        FpsMonitorEngine.isDroppedFrame(17_000_000L, period60Hz) shouldBe false
        FpsMonitorEngine.isDroppedFrame(24_000_000L, period60Hz) shouldBe false
    }

    @Test
    fun `frame over one and a half periods at 60Hz is dropped`() {
        FpsMonitorEngine.isDroppedFrame(25_100_000L, period60Hz) shouldBe true
    }

    @Test
    fun `120Hz frame missing its deadline is dropped below the 60Hz budget`() {
        FpsMonitorEngine.isDroppedFrame(13_000_000L, period120Hz) shouldBe true
        FpsMonitorEngine.isDroppedFrame(12_000_000L, period120Hz) shouldBe false
    }
}

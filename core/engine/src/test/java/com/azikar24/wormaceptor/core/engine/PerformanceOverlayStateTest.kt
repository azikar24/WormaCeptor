package com.azikar24.wormaceptor.core.engine

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PerformanceOverlayStateTest {

    @Nested
    inner class MetricStatusFromFps {

        @Test
        fun `should return GOOD for fps at or above 55`() {
            MetricStatus.fromFps(55) shouldBe MetricStatus.GOOD
            MetricStatus.fromFps(60) shouldBe MetricStatus.GOOD
            MetricStatus.fromFps(120) shouldBe MetricStatus.GOOD
        }

        @Test
        fun `should return WARNING for fps between 30 and 54`() {
            MetricStatus.fromFps(30) shouldBe MetricStatus.WARNING
            MetricStatus.fromFps(40) shouldBe MetricStatus.WARNING
            MetricStatus.fromFps(54) shouldBe MetricStatus.WARNING
        }

        @Test
        fun `should return CRITICAL for fps below 30`() {
            MetricStatus.fromFps(0) shouldBe MetricStatus.CRITICAL
            MetricStatus.fromFps(15) shouldBe MetricStatus.CRITICAL
            MetricStatus.fromFps(29) shouldBe MetricStatus.CRITICAL
        }

        @Test
        fun `should return INACTIVE when not monitoring`() {
            MetricStatus.fromFps(60, isMonitoring = false) shouldBe MetricStatus.INACTIVE
            MetricStatus.fromFps(0, isMonitoring = false) shouldBe MetricStatus.INACTIVE
        }
    }

    @Nested
    inner class MetricStatusFromMemoryPercent {

        @Test
        fun `should return GOOD for memory below 60`() {
            MetricStatus.fromMemoryPercent(0) shouldBe MetricStatus.GOOD
            MetricStatus.fromMemoryPercent(30) shouldBe MetricStatus.GOOD
            MetricStatus.fromMemoryPercent(59) shouldBe MetricStatus.GOOD
        }

        @Test
        fun `should return WARNING for memory between 60 and 80`() {
            MetricStatus.fromMemoryPercent(60) shouldBe MetricStatus.WARNING
            MetricStatus.fromMemoryPercent(70) shouldBe MetricStatus.WARNING
            MetricStatus.fromMemoryPercent(80) shouldBe MetricStatus.WARNING
        }

        @Test
        fun `should return CRITICAL for memory above 80`() {
            MetricStatus.fromMemoryPercent(81) shouldBe MetricStatus.CRITICAL
            MetricStatus.fromMemoryPercent(95) shouldBe MetricStatus.CRITICAL
            MetricStatus.fromMemoryPercent(100) shouldBe MetricStatus.CRITICAL
        }

        @Test
        fun `should return INACTIVE when not monitoring`() {
            MetricStatus.fromMemoryPercent(50, isMonitoring = false) shouldBe MetricStatus.INACTIVE
        }
    }

    @Nested
    inner class MetricStatusFromCpuPercent {

        @Test
        fun `should return GOOD for cpu below 50`() {
            MetricStatus.fromCpuPercent(0) shouldBe MetricStatus.GOOD
            MetricStatus.fromCpuPercent(25) shouldBe MetricStatus.GOOD
            MetricStatus.fromCpuPercent(49) shouldBe MetricStatus.GOOD
        }

        @Test
        fun `should return WARNING for cpu between 50 and 80`() {
            MetricStatus.fromCpuPercent(50) shouldBe MetricStatus.WARNING
            MetricStatus.fromCpuPercent(65) shouldBe MetricStatus.WARNING
            MetricStatus.fromCpuPercent(80) shouldBe MetricStatus.WARNING
        }

        @Test
        fun `should return CRITICAL for cpu above 80`() {
            MetricStatus.fromCpuPercent(81) shouldBe MetricStatus.CRITICAL
            MetricStatus.fromCpuPercent(95) shouldBe MetricStatus.CRITICAL
            MetricStatus.fromCpuPercent(100) shouldBe MetricStatus.CRITICAL
        }

        @Test
        fun `should return INACTIVE when not monitoring`() {
            MetricStatus.fromCpuPercent(50, isMonitoring = false) shouldBe MetricStatus.INACTIVE
        }
    }

    @Nested
    inner class PerformanceThresholdsConstants {

        @Test
        fun `FPS_GOOD should be 55`() {
            PerformanceThresholds.FPS_GOOD shouldBe 55
        }

        @Test
        fun `FPS_WARNING should be 30`() {
            PerformanceThresholds.FPS_WARNING shouldBe 30
        }

        @Test
        fun `MEMORY_GOOD should be 60`() {
            PerformanceThresholds.MEMORY_GOOD shouldBe 60
        }

        @Test
        fun `MEMORY_WARNING should be 80`() {
            PerformanceThresholds.MEMORY_WARNING shouldBe 80
        }

        @Test
        fun `CPU_GOOD should be 50`() {
            PerformanceThresholds.CPU_GOOD shouldBe 50
        }

        @Test
        fun `CPU_WARNING should be 80`() {
            PerformanceThresholds.CPU_WARNING shouldBe 80
        }
    }

    @Nested
    inner class HasAnyMetricEnabled {

        @Test
        fun `should return false when no metrics enabled`() {
            val state = PerformanceOverlayState()

            state.hasAnyMetricEnabled() shouldBe false
        }

        @Test
        fun `should return true when fps enabled`() {
            val state = PerformanceOverlayState(fpsEnabled = true)

            state.hasAnyMetricEnabled() shouldBe true
        }

        @Test
        fun `should return true when memory enabled`() {
            val state = PerformanceOverlayState(memoryEnabled = true)

            state.hasAnyMetricEnabled() shouldBe true
        }

        @Test
        fun `should return true when cpu enabled`() {
            val state = PerformanceOverlayState(cpuEnabled = true)

            state.hasAnyMetricEnabled() shouldBe true
        }

        @Test
        fun `should return true when all metrics enabled`() {
            val state = PerformanceOverlayState(
                fpsEnabled = true,
                memoryEnabled = true,
                cpuEnabled = true,
            )

            state.hasAnyMetricEnabled() shouldBe true
        }
    }

    @Nested
    inner class IsInDismissZone {

        @Test
        fun `should return true when position is at dismiss threshold`() {
            val state = PerformanceOverlayState(
                positionPercent = androidx.compose.ui.geometry.Offset(0.5f, 0.85f),
            )

            state.isInDismissZone() shouldBe true
        }

        @Test
        fun `should return true when position is below dismiss threshold`() {
            val state = PerformanceOverlayState(
                positionPercent = androidx.compose.ui.geometry.Offset(0.5f, 0.95f),
            )

            state.isInDismissZone() shouldBe true
        }

        @Test
        fun `should return false when position is above dismiss threshold`() {
            val state = PerformanceOverlayState(
                positionPercent = androidx.compose.ui.geometry.Offset(0.5f, 0.50f),
            )

            state.isInDismissZone() shouldBe false
        }

        @Test
        fun `should return false at default position`() {
            val state = PerformanceOverlayState()

            state.isInDismissZone() shouldBe false
        }
    }

    @Nested
    inner class CompanionDefaults {

        @Test
        fun `DEFAULT_POSITION_PERCENT should be at top-right`() {
            PerformanceOverlayState.DEFAULT_POSITION_PERCENT.x shouldBe 0.80f
            PerformanceOverlayState.DEFAULT_POSITION_PERCENT.y shouldBe 0.05f
        }

        @Test
        fun `DISMISS_ZONE_THRESHOLD should be 0_85f`() {
            PerformanceOverlayState.DISMISS_ZONE_THRESHOLD shouldBe 0.85f
        }

        @Test
        fun `EMPTY should have default values`() {
            val empty = PerformanceOverlayState.EMPTY
            empty.isOverlayEnabled shouldBe false
            empty.fpsEnabled shouldBe false
            empty.memoryEnabled shouldBe false
            empty.cpuEnabled shouldBe false
            empty.fpsValue shouldBe 0
            empty.memoryPercent shouldBe 0
            empty.cpuPercent shouldBe 0
        }
    }
}

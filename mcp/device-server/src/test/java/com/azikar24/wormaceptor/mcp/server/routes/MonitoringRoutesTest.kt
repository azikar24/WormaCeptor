package com.azikar24.wormaceptor.mcp.server.routes

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MonitoringRoutesTest {

    @Test
    fun `starts a stopped monitor and stops a running one`() {
        monitoringChange(enabled = true, running = false, overlayUses = false) shouldBe MonitoringChange.Start
        monitoringChange(enabled = false, running = true, overlayUses = false) shouldBe MonitoringChange.Stop
    }

    @Test
    fun `no-ops say so`() {
        monitoringChange(enabled = true, running = true, overlayUses = false) shouldBe MonitoringChange.AlreadyRunning
        monitoringChange(enabled = false, running = false, overlayUses = false) shouldBe
            MonitoringChange.AlreadyStopped
    }

    @Test
    fun `never stops a monitor the overlay is showing`() {
        monitoringChange(enabled = false, running = true, overlayUses = true) shouldBe MonitoringChange.KeptForOverlay
        monitoringChange(enabled = true, running = true, overlayUses = true) shouldBe MonitoringChange.AlreadyRunning
    }

    @Test
    fun `target names are case-insensitive`() {
        MonitorTarget.from("FPS") shouldBe MonitorTarget.FPS
        MonitorTarget.from("memory") shouldBe MonitorTarget.MEMORY
        MonitorTarget.from("gpu") shouldBe null
    }
}

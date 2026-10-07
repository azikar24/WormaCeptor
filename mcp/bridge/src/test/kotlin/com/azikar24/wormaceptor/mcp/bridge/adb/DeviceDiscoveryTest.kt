package com.azikar24.wormaceptor.mcp.bridge.adb

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceDiscoveryTest {

    private val adb = mockk<AdbClient>()
    private val discovery = DeviceDiscovery(adb)

    private val emulator = DeviceInfo("emulator-5554", "device", "sdk_gphone64", "sdk_gphone64_arm64")
    private val phone = DeviceInfo("R5CT123", "device", "SM_S928B", "e3qxxx")

    @Test
    fun `parses adb devices -l output and skips daemon lines`() {
        val output = """
            List of devices attached
            * daemon started successfully
            emulator-5554          device product:sdk_gphone64_arm64 model:sdk_gphone64 device:emu64a transport_id:1
            R5CT123                unauthorized usb:1-1 transport_id:2

        """.trimIndent()

        assertEquals(
            listOf(
                emulator,
                DeviceInfo("R5CT123", "unauthorized", null, null),
            ),
            parseDevices(output),
        )
    }

    @Test
    fun `auto-selects the only ready device`() {
        every { adb.listDevices() } returns listOf(emulator, DeviceInfo("R5CT9", "offline", null, null))
        assertEquals(emulator, discovery.findDevice(null))
    }

    @Test
    fun `requires a serial when several devices are ready`() {
        every { adb.listDevices() } returns listOf(emulator, phone)
        val error = assertThrows(IllegalStateException::class.java) { discovery.findDevice(null) }
        assertTrue(error.message!!.contains("--device"))
        assertEquals(phone, discovery.findDevice("R5CT123"))
    }

    @Test
    fun `fails clearly when no device or an unknown serial is given`() {
        every { adb.listDevices() } returns emptyList()
        assertThrows(IllegalStateException::class.java) { discovery.findDevice(null) }

        every { adb.listDevices() } returns listOf(emulator)
        val error = assertThrows(IllegalStateException::class.java) { discovery.findDevice("nope") }
        assertTrue(error.message!!.contains("emulator-5554"))
    }
}

package com.azikar24.wormaceptor.mcp.bridge.adb

internal class DeviceDiscovery(private val adbClient: AdbClient) {

    fun findDevice(requestedSerial: String?): DeviceInfo {
        val devices = adbClient.listDevices().filter { it.state == "device" }

        if (devices.isEmpty()) {
            throw IllegalStateException(
                "No Android devices found. Connect a device or start an emulator.",
            )
        }

        if (requestedSerial != null) {
            return devices.find { it.serial == requestedSerial }
                ?: throw IllegalStateException(
                    "Device '$requestedSerial' not found. Available: ${devices.map { it.serial }}",
                )
        }

        if (devices.size > 1) {
            val deviceList = devices.joinToString("\n") { "  ${it.serial} (${it.model ?: "unknown"})" }
            throw IllegalStateException(
                "Multiple devices connected. Use --device to select one:\n$deviceList",
            )
        }

        return devices.first()
    }
}

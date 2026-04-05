package com.azikar24.wormaceptor.feature.deviceinfo.vm

/** User-initiated events on the device info screen. */
sealed class DeviceInfoViewEvent {
    /** Requests initial device information load. */
    data object LoadDeviceInfo : DeviceInfoViewEvent()

    /** Pull-to-refresh triggered. */
    data object Refresh : DeviceInfoViewEvent()

    /** Copies all device information to clipboard. */
    data object CopyAll : DeviceInfoViewEvent()

    /** Shares the full device report via system share sheet. */
    data object ShareReport : DeviceInfoViewEvent()

    /** Copies a single [section] to the clipboard. */
    data class CopySection(val section: DeviceInfoSection) : DeviceInfoViewEvent()
}

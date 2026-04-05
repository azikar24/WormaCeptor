package com.azikar24.wormaceptor.feature.deviceinfo.vm

sealed class DeviceInfoViewEvent {
    data object LoadDeviceInfo : DeviceInfoViewEvent()
    data object Refresh : DeviceInfoViewEvent()
    data object CopyAll : DeviceInfoViewEvent()
    data object ShareReport : DeviceInfoViewEvent()
    data class CopySection(val section: DeviceInfoSection) : DeviceInfoViewEvent()
}

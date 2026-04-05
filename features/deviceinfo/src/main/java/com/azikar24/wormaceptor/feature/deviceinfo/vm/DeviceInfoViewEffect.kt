package com.azikar24.wormaceptor.feature.deviceinfo.vm

sealed class DeviceInfoViewEffect {
    data class CopyToClipboard(val label: String, val text: String) : DeviceInfoViewEffect()
    data class ShareText(val text: String, val subject: String) : DeviceInfoViewEffect()
}

package com.azikar24.wormaceptor.feature.deviceinfo.vm

/** One-shot side-effects emitted by [DeviceInfoViewModel]. */
sealed class DeviceInfoViewEffect {
    /** Copies [text] to the clipboard with the given [label]. */
    data class CopyToClipboard(val label: String, val text: String) : DeviceInfoViewEffect()

    /** Shares [text] via the system share sheet with the given [subject]. */
    data class ShareText(val text: String, val subject: String) : DeviceInfoViewEffect()
}

package com.azikar24.wormaceptor.feature.deviceinfo.vm

/** Represents a copyable section of the device info report. */
enum class DeviceInfoSection {
    /** Hardware device information. */
    DEVICE,

    /** Operating system details. */
    OS,

    /** Display and screen metrics. */
    DISPLAY,

    /** RAM and memory usage. */
    MEMORY,

    /** Internal and external storage. */
    STORAGE,

    /** Host application metadata. */
    APPLICATION,

    /** Network connectivity status. */
    NETWORK,
}

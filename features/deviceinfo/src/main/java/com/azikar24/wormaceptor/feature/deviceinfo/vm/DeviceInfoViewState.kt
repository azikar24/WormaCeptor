package com.azikar24.wormaceptor.feature.deviceinfo.vm

import com.azikar24.wormaceptor.domain.entities.DeviceInfo

/** UI state for the device info screen. */
data class DeviceInfoViewState(
    /** Collected device information, null until first load completes. */
    val deviceInfo: DeviceInfo? = null,
    /** True during the first load before any data is available. */
    val isInitialLoading: Boolean = true,
    /** True while a pull-to-refresh is in progress. */
    val isRefreshing: Boolean = false,
    /** Error message when loading fails. */
    val error: String? = null,
)

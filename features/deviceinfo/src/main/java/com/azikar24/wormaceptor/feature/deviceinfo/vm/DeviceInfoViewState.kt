package com.azikar24.wormaceptor.feature.deviceinfo.vm

import com.azikar24.wormaceptor.domain.entities.DeviceInfo

data class DeviceInfoViewState(
    val deviceInfo: DeviceInfo? = null,
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
)

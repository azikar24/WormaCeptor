package com.azikar24.wormaceptor.mcp.protocol

import kotlinx.serialization.Serializable

@Serializable
internal data class DeviceInfoDto(
    val device: DeviceDetailsDto,
    val os: OsDetailsDto,
    val screen: ScreenDetailsDto,
    val memory: MemoryDetailsDto,
    val storage: StorageDetailsDto,
    val app: AppDetailsDto,
    val network: NetworkDetailsDto,
    val timestamp: Long,
)

@Serializable
internal data class DeviceDetailsDto(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val device: String,
    val hardware: String,
    val board: String,
    val product: String,
    val isEmulator: Boolean,
)

@Serializable
internal data class OsDetailsDto(
    val androidVersion: String,
    val sdkLevel: Int,
    val buildId: String,
    val securityPatch: String?,
    val bootloader: String,
    val fingerprint: String,
    val incremental: String,
)

@Serializable
internal data class ScreenDetailsDto(
    val widthPixels: Int,
    val heightPixels: Int,
    val densityDpi: Int,
    val density: Float,
    val scaledDensity: Float,
    val sizeCategory: String,
    val orientation: String,
    val refreshRate: Float,
)

@Serializable
internal data class MemoryDetailsDto(
    val totalRam: Long,
    val availableRam: Long,
    val lowMemoryThreshold: Long,
    val isLowMemory: Boolean,
    val usedRam: Long,
    val usagePercentage: Float,
)

@Serializable
internal data class StorageDetailsDto(
    val internalTotal: Long,
    val internalAvailable: Long,
    val internalUsed: Long,
    val externalTotal: Long?,
    val externalAvailable: Long?,
    val externalUsed: Long?,
    val hasExternalStorage: Boolean,
)

@Serializable
internal data class AppDetailsDto(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val targetSdk: Int,
    val minSdk: Int,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val isDebuggable: Boolean,
)

@Serializable
internal data class NetworkDetailsDto(
    val connectionType: String,
    val isConnected: Boolean,
    val isWifiConnected: Boolean,
    val isCellularConnected: Boolean,
    val isMetered: Boolean,
    val wifiSsid: String?,
    val cellularNetworkType: String?,
)

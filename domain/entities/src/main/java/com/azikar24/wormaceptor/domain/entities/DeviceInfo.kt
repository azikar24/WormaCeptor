/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Complete device and application information snapshot.
 * Used for debugging, support tickets, and environment context.
 */
data class DeviceInfo(
    val device: DeviceDetails,
    val os: OsDetails,
    val screen: ScreenDetails,
    val memory: MemoryDetails,
    val storage: StorageDetails,
    val app: AppDetails,
    val network: NetworkDetails,
    val timestamp: EpochMillis = System.currentTimeMillis(),
)

/**
 * Physical device hardware information.
 */
data class DeviceDetails(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val device: String,
    val hardware: String,
    val board: String,
    val product: String,
    val isEmulator: Boolean,
)

/**
 * Android OS version and build information.
 */
data class OsDetails(
    val androidVersion: String,
    val sdkLevel: Int,
    val buildId: String,
    val securityPatch: String?,
    val bootloader: String,
    val fingerprint: String,
    val incremental: String,
)

/**
 * Display screen characteristics.
 */
data class ScreenDetails(
    val widthPixels: Int,
    val heightPixels: Int,
    val densityDpi: Int,
    val density: Float,
    val scaledDensity: Float,
    val sizeCategory: String,
    val orientation: String,
    val refreshRate: Float,
)

/**
 * System memory (RAM) information.
 */
data class MemoryDetails(
    val totalRam: Long,
    val availableRam: Long,
    val lowMemoryThreshold: Long,
    val isLowMemory: Boolean,
    val usedRam: Long,
    val usagePercentage: Float,
)

/**
 * Storage (internal/external) information.
 */
data class StorageDetails(
    val internalTotal: Long,
    val internalAvailable: Long,
    val internalUsed: Long,
    val externalTotal: Long?,
    val externalAvailable: Long?,
    val externalUsed: Long?,
    val hasExternalStorage: Boolean,
)

/**
 * Application-specific information.
 */
data class AppDetails(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val targetSdk: Int,
    val minSdk: Int,
    val firstInstallTime: EpochMillis,
    val lastUpdateTime: EpochMillis,
    val isDebuggable: Boolean,
)

/**
 * Network connectivity information.
 */
data class NetworkDetails(
    val connectionType: String,
    val isConnected: Boolean,
    val isWifiConnected: Boolean,
    val isCellularConnected: Boolean,
    val isMetered: Boolean,
    val wifiSsid: String?,
    val cellularNetworkType: String?,
)

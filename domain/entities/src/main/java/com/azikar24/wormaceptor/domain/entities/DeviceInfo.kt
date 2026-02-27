package com.azikar24.wormaceptor.domain.entities

/**
 * Complete device and application information snapshot.
 * Used for debugging, support tickets, and environment context.
 *
 * @property device Physical hardware details of the device.
 * @property os Android OS version and build information.
 * @property screen Display screen characteristics.
 * @property memory System RAM usage information.
 * @property storage Internal and external storage statistics.
 * @property app Application-specific metadata (package, version, SDK).
 * @property network Current network connectivity state.
 * @property timestamp Epoch millis when this snapshot was captured.
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
 *
 * @property manufacturer Device manufacturer (e.g., "Samsung").
 * @property model End-user-visible device model name (e.g., "Pixel 7").
 * @property brand Consumer-visible brand name (e.g., "google").
 * @property device Industrial design name of the device.
 * @property hardware Hardware platform name (from the kernel).
 * @property board Motherboard/board name.
 * @property product Overall product name.
 * @property isEmulator Whether the device is detected as an emulator.
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
 *
 * @property androidVersion Human-readable Android version string (e.g., "14").
 * @property sdkLevel Android SDK API level (e.g., 34).
 * @property buildId Build display ID (e.g., "UPB5.230623.003").
 * @property securityPatch Security patch level date string, or null if unavailable.
 * @property bootloader Bootloader version string.
 * @property fingerprint Unique build fingerprint identifying this OS image.
 * @property incremental Internal build version used for source control.
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
 *
 * @property widthPixels Screen width in physical pixels.
 * @property heightPixels Screen height in physical pixels.
 * @property densityDpi Screen density expressed as dots per inch.
 * @property density Logical density of the display (scaling factor).
 * @property scaledDensity Scaled density accounting for user font size preference.
 * @property sizeCategory Screen size bucket (e.g., "normal", "large", "xlarge").
 * @property orientation Current orientation (e.g., "portrait", "landscape").
 * @property refreshRate Display refresh rate in Hz.
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
 *
 * @property totalRam Total physical RAM in bytes.
 * @property availableRam Currently available RAM in bytes.
 * @property lowMemoryThreshold Threshold in bytes below which the system considers memory low.
 * @property isLowMemory Whether the system is currently in a low-memory state.
 * @property usedRam Currently used RAM in bytes (totalRam - availableRam).
 * @property usagePercentage RAM usage as a percentage (0-100).
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
 *
 * @property internalTotal Total internal storage capacity in bytes.
 * @property internalAvailable Available internal storage in bytes.
 * @property internalUsed Used internal storage in bytes.
 * @property externalTotal Total external storage capacity in bytes, or null if absent.
 * @property externalAvailable Available external storage in bytes, or null if absent.
 * @property externalUsed Used external storage in bytes, or null if absent.
 * @property hasExternalStorage Whether external storage is present on the device.
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
 *
 * @property packageName Application package name (e.g., "com.example.app").
 * @property versionName User-visible version string from the manifest.
 * @property versionCode Internal version code from the manifest.
 * @property targetSdk Target SDK version the app is compiled against.
 * @property minSdk Minimum SDK version the app supports.
 * @property firstInstallTime Epoch millis when the app was first installed.
 * @property lastUpdateTime Epoch millis when the app was last updated.
 * @property isDebuggable Whether the app has the debuggable flag set.
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
 *
 * @property connectionType Current connection type label (e.g., "WiFi", "Cellular", "None").
 * @property isConnected Whether the device has any active network connection.
 * @property isWifiConnected Whether the device is connected via Wi-Fi.
 * @property isCellularConnected Whether the device is connected via cellular data.
 * @property isMetered Whether the active connection is metered (data-capped).
 * @property wifiSsid SSID of the connected Wi-Fi network, or null if not on Wi-Fi.
 * @property cellularNetworkType Cellular network type (e.g., "LTE", "5G"), or null if not on cellular.
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

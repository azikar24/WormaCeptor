/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.deviceinfo

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.WindowManager
import com.azikar24.wormaceptor.domain.entities.AppDetails
import com.azikar24.wormaceptor.domain.entities.DeviceDetails
import com.azikar24.wormaceptor.domain.entities.DeviceInfo
import com.azikar24.wormaceptor.domain.entities.MemoryDetails
import com.azikar24.wormaceptor.domain.entities.NetworkDetails
import com.azikar24.wormaceptor.domain.entities.OsDetails
import com.azikar24.wormaceptor.domain.entities.ScreenDetails
import com.azikar24.wormaceptor.domain.entities.StorageDetails

/**
 * Use case for collecting comprehensive device and application information.
 * All data is collected using standard Android APIs without requiring special permissions
 * (except ACCESS_NETWORK_STATE for network info).
 */
class GetDeviceInfoUseCase(private val context: Context) {

    /**
     * Collects all device information synchronously.
     * Should be called from a background thread for best performance.
     */
    fun execute(): DeviceInfo {
        return DeviceInfo(
            device = getDeviceDetails(),
            os = getOsDetails(),
            screen = getScreenDetails(),
            memory = getMemoryDetails(),
            storage = getStorageDetails(),
            app = getAppDetails(),
            network = getNetworkDetails(),
        )
    }

    private fun getDeviceDetails(): DeviceDetails {
        return DeviceDetails(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            brand = Build.BRAND,
            device = Build.DEVICE,
            hardware = Build.HARDWARE,
            board = Build.BOARD,
            product = Build.PRODUCT,
            isEmulator = isEmulator(),
        )
    }

    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for x86") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
            Build.PRODUCT == "google_sdk" ||
            Build.HARDWARE.contains("goldfish") ||
            Build.HARDWARE.contains("ranchu")
    }

    private fun getOsDetails(): OsDetails {
        return OsDetails(
            androidVersion = Build.VERSION.RELEASE,
            sdkLevel = Build.VERSION.SDK_INT,
            buildId = Build.ID,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Build.VERSION.SECURITY_PATCH
            } else {
                null
            },
            bootloader = Build.BOOTLOADER,
            fingerprint = Build.FINGERPRINT,
            incremental = Build.VERSION.INCREMENTAL,
        )
    }

    @Suppress("DEPRECATION")
    private fun getScreenDetails(): ScreenDetails {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            displayMetrics.widthPixels = bounds.width()
            displayMetrics.heightPixels = bounds.height()
            displayMetrics.density = context.resources.displayMetrics.density
            displayMetrics.densityDpi = context.resources.displayMetrics.densityDpi
            displayMetrics.scaledDensity = context.resources.displayMetrics.scaledDensity
        } else {
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }

        val refreshRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.refreshRate ?: 60f
        } else {
            windowManager.defaultDisplay.refreshRate
        }

        val configuration = context.resources.configuration
        val sizeCategory = when (configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
            Configuration.SCREENLAYOUT_SIZE_SMALL -> "Small"
            Configuration.SCREENLAYOUT_SIZE_NORMAL -> "Normal"
            Configuration.SCREENLAYOUT_SIZE_LARGE -> "Large"
            Configuration.SCREENLAYOUT_SIZE_XLARGE -> "XLarge"
            else -> "Unknown"
        }

        val orientation = when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> "Portrait"
            Configuration.ORIENTATION_LANDSCAPE -> "Landscape"
            else -> "Undefined"
        }

        return ScreenDetails(
            widthPixels = displayMetrics.widthPixels,
            heightPixels = displayMetrics.heightPixels,
            densityDpi = displayMetrics.densityDpi,
            density = displayMetrics.density,
            scaledDensity = displayMetrics.scaledDensity,
            sizeCategory = sizeCategory,
            orientation = orientation,
            refreshRate = refreshRate,
        )
    }

    private fun getMemoryDetails(): MemoryDetails {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRam = memoryInfo.totalMem
        val availableRam = memoryInfo.availMem
        val usedRam = totalRam - availableRam
        val usagePercentage = if (totalRam > 0) {
            (usedRam.toFloat() / totalRam.toFloat()) * 100f
        } else {
            0f
        }

        return MemoryDetails(
            totalRam = totalRam,
            availableRam = availableRam,
            lowMemoryThreshold = memoryInfo.threshold,
            isLowMemory = memoryInfo.lowMemory,
            usedRam = usedRam,
            usagePercentage = usagePercentage,
        )
    }

    private fun getStorageDetails(): StorageDetails {
        // Internal storage
        val internalPath = Environment.getDataDirectory()
        val internalStat = StatFs(internalPath.path)
        val internalTotal = internalStat.blockCountLong * internalStat.blockSizeLong
        val internalAvailable = internalStat.availableBlocksLong * internalStat.blockSizeLong
        val internalUsed = internalTotal - internalAvailable

        // External storage
        val externalStorageState = Environment.getExternalStorageState()
        val hasExternalStorage = externalStorageState == Environment.MEDIA_MOUNTED ||
            externalStorageState == Environment.MEDIA_MOUNTED_READ_ONLY

        var externalTotal: Long? = null
        var externalAvailable: Long? = null
        var externalUsed: Long? = null

        if (hasExternalStorage) {
            try {
                @Suppress("DEPRECATION")
                val externalPath = Environment.getExternalStorageDirectory()
                val externalStat = StatFs(externalPath.path)
                externalTotal = externalStat.blockCountLong * externalStat.blockSizeLong
                externalAvailable = externalStat.availableBlocksLong * externalStat.blockSizeLong
                externalUsed = externalTotal - externalAvailable
            } catch (e: Exception) {
                // External storage not accessible
            }
        }

        return StorageDetails(
            internalTotal = internalTotal,
            internalAvailable = internalAvailable,
            internalUsed = internalUsed,
            externalTotal = externalTotal,
            externalAvailable = externalAvailable,
            externalUsed = externalUsed,
            hasExternalStorage = hasExternalStorage,
        )
    }

    @Suppress("DEPRECATION")
    private fun getAppDetails(): AppDetails {
        val packageManager = context.packageManager
        val packageName = context.packageName
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
        val applicationInfo = packageInfo.applicationInfo

        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }

        val isDebuggable = applicationInfo != null &&
            (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applicationInfo?.minSdkVersion ?: 1
        } else {
            // For older devices, we can't easily get minSdk
            1
        }

        return AppDetails(
            packageName = packageName,
            versionName = packageInfo.versionName ?: "Unknown",
            versionCode = versionCode,
            targetSdk = applicationInfo?.targetSdkVersion ?: Build.VERSION.SDK_INT,
            minSdk = minSdk,
            firstInstallTime = packageInfo.firstInstallTime,
            lastUpdateTime = packageInfo.lastUpdateTime,
            isDebuggable = isDebuggable,
        )
    }

    private fun getNetworkDetails(): NetworkDetails {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var isConnected = false
        var isWifiConnected = false
        var isCellularConnected = false
        var isMetered = false
        var connectionType = "None"
        var cellularNetworkType: String? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }

            if (capabilities != null) {
                isConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                isWifiConnected = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                isCellularConnected = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

                connectionType = when {
                    isWifiConnected -> "WiFi"
                    isCellularConnected -> "Cellular"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
                    else -> "Unknown"
                }
            }

            isMetered = connectivityManager.isActiveNetworkMetered
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            isConnected = networkInfo?.isConnected == true
            @Suppress("DEPRECATION")
            connectionType = networkInfo?.typeName ?: "None"
            @Suppress("DEPRECATION")
            isWifiConnected = networkInfo?.type == ConnectivityManager.TYPE_WIFI
            @Suppress("DEPRECATION")
            isCellularConnected = networkInfo?.type == ConnectivityManager.TYPE_MOBILE
        }

        // Get cellular network type (2G/3G/4G/5G) if on cellular
        if (isCellularConnected && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val telephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as? android.telephony.TelephonyManager
                cellularNetworkType = when (telephonyManager?.dataNetworkType) {
                    android.telephony.TelephonyManager.NETWORK_TYPE_GPRS,
                    android.telephony.TelephonyManager.NETWORK_TYPE_EDGE,
                    android.telephony.TelephonyManager.NETWORK_TYPE_CDMA,
                    android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT,
                    android.telephony.TelephonyManager.NETWORK_TYPE_IDEN,
                    android.telephony.TelephonyManager.NETWORK_TYPE_GSM,
                    -> "2G"

                    android.telephony.TelephonyManager.NETWORK_TYPE_UMTS,
                    android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0,
                    android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A,
                    android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA,
                    android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA,
                    android.telephony.TelephonyManager.NETWORK_TYPE_HSPA,
                    android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B,
                    android.telephony.TelephonyManager.NETWORK_TYPE_EHRPD,
                    android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP,
                    android.telephony.TelephonyManager.NETWORK_TYPE_TD_SCDMA,
                    -> "3G"

                    android.telephony.TelephonyManager.NETWORK_TYPE_LTE,
                    android.telephony.TelephonyManager.NETWORK_TYPE_IWLAN,
                    -> "4G"

                    android.telephony.TelephonyManager.NETWORK_TYPE_NR -> "5G"

                    else -> null
                }
            } catch (e: SecurityException) {
                // Permission not granted for reading phone state
                cellularNetworkType = null
            }
        }

        return NetworkDetails(
            connectionType = connectionType,
            isConnected = isConnected,
            isWifiConnected = isWifiConnected,
            isCellularConnected = isCellularConnected,
            isMetered = isMetered,
            wifiSsid = null, // Requires location permission on newer Android versions
            cellularNetworkType = cellularNetworkType,
        )
    }
}

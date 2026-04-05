package com.azikar24.wormaceptor.feature.deviceinfo.vm

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatDateFull
import com.azikar24.wormaceptor.domain.entities.AppDetails
import com.azikar24.wormaceptor.domain.entities.DeviceDetails
import com.azikar24.wormaceptor.domain.entities.DeviceInfo
import com.azikar24.wormaceptor.domain.entities.MemoryDetails
import com.azikar24.wormaceptor.domain.entities.NetworkDetails
import com.azikar24.wormaceptor.domain.entities.OsDetails
import com.azikar24.wormaceptor.domain.entities.ScreenDetails
import com.azikar24.wormaceptor.domain.entities.StorageDetails
import com.azikar24.wormaceptor.feature.deviceinfo.GetDeviceInfoUseCase
import com.azikar24.wormaceptor.feature.deviceinfo.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private const val REFRESH_DEBOUNCE_MS = 200L

class DeviceInfoViewModel(
    private val application: Application,
) : BaseViewModel<DeviceInfoViewState, DeviceInfoViewEffect, DeviceInfoViewEvent>(
    DeviceInfoViewState(),
) {

    init {
        loadDeviceInfo()
    }

    override fun handleEvent(event: DeviceInfoViewEvent) {
        when (event) {
            DeviceInfoViewEvent.LoadDeviceInfo -> loadDeviceInfo()
            DeviceInfoViewEvent.Refresh -> refresh()
            DeviceInfoViewEvent.CopyAll -> copyAll()
            DeviceInfoViewEvent.ShareReport -> shareReport()
            is DeviceInfoViewEvent.CopySection -> copySection(event.section)
        }
    }

    private fun loadDeviceInfo() {
        viewModelScope.launch {
            updateState { copy(isInitialLoading = true) }
            val info = withContext(Dispatchers.IO) {
                GetDeviceInfoUseCase(application).execute()
            }
            updateState { copy(deviceInfo = info, isInitialLoading = false) }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            updateState { copy(isRefreshing = true) }
            delay(REFRESH_DEBOUNCE_MS)
            val info = withContext(Dispatchers.IO) {
                GetDeviceInfoUseCase(application).execute()
            }
            updateState { copy(deviceInfo = info, isRefreshing = false) }
        }
    }

    private fun copyAll() {
        val info = uiState.value.deviceInfo ?: return
        emitEffect(
            DeviceInfoViewEffect.CopyToClipboard(
                label = str(R.string.deviceinfo_copy_all_label),
                text = formatCompactReport(info),
            ),
        )
    }

    private fun shareReport() {
        val info = uiState.value.deviceInfo ?: return
        emitEffect(
            DeviceInfoViewEffect.ShareText(
                text = formatFullReport(info),
                subject = str(R.string.deviceinfo_share_subject),
            ),
        )
    }

    private fun copySection(section: DeviceInfoSection) {
        val info = uiState.value.deviceInfo ?: return
        val (titleRes, text) = when (section) {
            DeviceInfoSection.DEVICE -> R.string.deviceinfo_section_device to formatDeviceSection(info.device)
            DeviceInfoSection.OS -> R.string.deviceinfo_section_os to formatOsSection(info.os)
            DeviceInfoSection.DISPLAY -> R.string.deviceinfo_section_display to formatScreenSection(info.screen)
            DeviceInfoSection.MEMORY -> R.string.deviceinfo_section_memory to formatMemorySection(info.memory)
            DeviceInfoSection.STORAGE -> R.string.deviceinfo_section_storage to formatStorageSection(info.storage)
            DeviceInfoSection.APPLICATION -> R.string.deviceinfo_section_application to formatAppSection(info.app)
            DeviceInfoSection.NETWORK -> R.string.deviceinfo_section_network to formatNetworkSection(info.network)
        }
        emitEffect(
            DeviceInfoViewEffect.CopyToClipboard(
                label = str(titleRes),
                text = text,
            ),
        )
    }

    // -- Formatting helpers --

    private fun str(resId: Int): String = application.getString(resId)

    private fun str(
        resId: Int,
        vararg args: Any,
    ): String = application.getString(resId, *args)

    private fun labelValue(
        labelRes: Int,
        value: String,
    ): String = str(R.string.deviceinfo_format_label_value, str(labelRes), value)

    private fun yesNo(value: Boolean): String = if (value) str(R.string.deviceinfo_yes) else str(R.string.deviceinfo_no)

    // -- Report formatters --

    private fun formatCompactReport(info: DeviceInfo): String = buildString {
        appendLine(labelValue(R.string.deviceinfo_device_manufacturer, info.device.manufacturer))
        appendLine(labelValue(R.string.deviceinfo_device_model, info.device.model))
        appendLine(labelValue(R.string.deviceinfo_device_brand, info.device.brand))
        appendLine(labelValue(R.string.deviceinfo_device_device, info.device.device))
        appendLine(labelValue(R.string.deviceinfo_device_emulator, yesNo(info.device.isEmulator)))
        appendLine()
        appendLine(str(R.string.deviceinfo_format_android_compact, info.os.androidVersion, info.os.sdkLevel))
        appendLine(labelValue(R.string.deviceinfo_os_build_id, info.os.buildId))
        info.os.securityPatch?.let { appendLine(labelValue(R.string.deviceinfo_os_security_patch, it)) }
        appendLine()
        appendLine(
            str(
                R.string.deviceinfo_format_screen_compact,
                info.screen.widthPixels,
                info.screen.heightPixels,
                info.screen.densityDpi,
            ),
        )
        appendLine(str(R.string.deviceinfo_format_refresh_rate_compact, info.screen.refreshRate.toInt()))
        appendLine()
        val usageStr = String.format(Locale.US, "%.1f%%", info.memory.usagePercentage)
        appendLine(
            str(
                R.string.deviceinfo_format_ram_compact,
                formatBytes(info.memory.usedRam),
                formatBytes(info.memory.totalRam),
                usageStr,
            ),
        )
        appendLine()
        appendLine(
            str(
                R.string.deviceinfo_format_storage_compact,
                formatBytes(info.storage.internalUsed),
                formatBytes(info.storage.internalTotal),
            ),
        )
        appendLine()
        appendLine(str(R.string.deviceinfo_format_package_compact, info.app.packageName))
        appendLine(str(R.string.deviceinfo_format_version_compact, info.app.versionName, info.app.versionCode))
        appendLine(str(R.string.deviceinfo_format_sdk_compact, info.app.targetSdk, info.app.minSdk))
        appendLine(labelValue(R.string.deviceinfo_app_debuggable, yesNo(info.app.isDebuggable)))
        appendLine()
        val connectedStr = if (info.network.isConnected) {
            str(
                R.string.deviceinfo_connected,
            )
        } else {
            str(R.string.deviceinfo_not_connected)
        }
        appendLine(str(R.string.deviceinfo_format_network_compact, info.network.connectionType, connectedStr))
    }

    private fun formatFullReport(info: DeviceInfo): String = buildString {
        appendLine(str(R.string.deviceinfo_report_title))
        appendLine()
        appendLine(str(R.string.deviceinfo_collected, formatDateFull(info.timestamp)))
        appendLine()
        appendSection(R.string.deviceinfo_section_device, formatDeviceSection(info.device))
        appendSection(R.string.deviceinfo_section_os, formatOsSection(info.os))
        appendSection(R.string.deviceinfo_section_display, formatScreenSection(info.screen))
        appendSection(R.string.deviceinfo_section_memory, formatMemorySection(info.memory))
        appendSection(R.string.deviceinfo_section_storage, formatStorageSection(info.storage))
        appendSection(R.string.deviceinfo_section_application, formatAppSection(info.app))
        appendSection(R.string.deviceinfo_section_network, formatNetworkSection(info.network))
    }

    private fun StringBuilder.appendSection(
        titleRes: Int,
        content: String,
    ) {
        appendLine(str(R.string.deviceinfo_report_section_header, str(titleRes)))
        appendLine(content)
        appendLine()
    }

    private fun formatDeviceSection(device: DeviceDetails): String = buildString {
        appendLine(labelValue(R.string.deviceinfo_device_manufacturer, device.manufacturer))
        appendLine(labelValue(R.string.deviceinfo_device_model, device.model))
        appendLine(labelValue(R.string.deviceinfo_device_brand, device.brand))
        appendLine(labelValue(R.string.deviceinfo_device_device, device.device))
        appendLine(labelValue(R.string.deviceinfo_device_hardware, device.hardware))
        appendLine(labelValue(R.string.deviceinfo_device_board, device.board))
        appendLine(labelValue(R.string.deviceinfo_device_product, device.product))
        appendLine(labelValue(R.string.deviceinfo_device_emulator, yesNo(device.isEmulator)))
    }

    private fun formatOsSection(os: OsDetails): String = buildString {
        appendLine(labelValue(R.string.deviceinfo_os_android_version, os.androidVersion))
        appendLine(labelValue(R.string.deviceinfo_os_sdk_level, os.sdkLevel.toString()))
        appendLine(labelValue(R.string.deviceinfo_os_build_id, os.buildId))
        os.securityPatch?.let { appendLine(labelValue(R.string.deviceinfo_os_security_patch, it)) }
        appendLine(labelValue(R.string.deviceinfo_os_bootloader, os.bootloader))
        appendLine(labelValue(R.string.deviceinfo_os_incremental, os.incremental))
        appendLine(labelValue(R.string.deviceinfo_os_fingerprint, os.fingerprint))
    }

    private fun formatScreenSection(screen: ScreenDetails): String = buildString {
        val resolution = str(R.string.deviceinfo_format_resolution_value, screen.widthPixels, screen.heightPixels)
        appendLine(labelValue(R.string.deviceinfo_screen_resolution, resolution))
        appendLine(labelValue(R.string.deviceinfo_screen_density_dpi, screen.densityDpi.toString()))
        appendLine(labelValue(R.string.deviceinfo_screen_density, String.format(Locale.US, "%.2f", screen.density)))
        appendLine(
            labelValue(
                R.string.deviceinfo_screen_scaled_density,
                String.format(Locale.US, "%.2f", screen.scaledDensity),
            ),
        )
        appendLine(labelValue(R.string.deviceinfo_screen_size_category, screen.sizeCategory))
        appendLine(labelValue(R.string.deviceinfo_screen_orientation, screen.orientation))
        val hz = str(R.string.deviceinfo_format_hz_value, screen.refreshRate.toInt())
        appendLine(labelValue(R.string.deviceinfo_screen_refresh_rate, hz))
    }

    private fun formatMemorySection(memory: MemoryDetails): String = buildString {
        appendLine(labelValue(R.string.deviceinfo_memory_total_ram, formatBytes(memory.totalRam)))
        appendLine(labelValue(R.string.deviceinfo_memory_available_ram, formatBytes(memory.availableRam)))
        appendLine(labelValue(R.string.deviceinfo_memory_used_ram, formatBytes(memory.usedRam)))
        val usage = String.format(Locale.US, "%.1f%%", memory.usagePercentage)
        appendLine(labelValue(R.string.deviceinfo_label_usage, usage))
        appendLine(labelValue(R.string.deviceinfo_memory_low_threshold, formatBytes(memory.lowMemoryThreshold)))
        appendLine(labelValue(R.string.deviceinfo_memory_low_memory, yesNo(memory.isLowMemory)))
    }

    private fun formatStorageSection(storage: StorageDetails): String = buildString {
        appendLine(str(R.string.deviceinfo_storage_internal))
        appendLine(
            str(
                R.string.deviceinfo_format_indented_label_value,
                str(R.string.deviceinfo_label_total),
                formatBytes(storage.internalTotal),
            ),
        )
        appendLine(
            str(
                R.string.deviceinfo_format_indented_label_value,
                str(R.string.deviceinfo_label_available),
                formatBytes(storage.internalAvailable),
            ),
        )
        appendLine(
            str(
                R.string.deviceinfo_format_indented_label_value,
                str(R.string.deviceinfo_label_used),
                formatBytes(storage.internalUsed),
            ),
        )
        val extTotal = storage.externalTotal
        if (storage.hasExternalStorage && extTotal != null) {
            appendLine(str(R.string.deviceinfo_storage_external))
            appendLine(
                str(
                    R.string.deviceinfo_format_indented_label_value,
                    str(R.string.deviceinfo_label_total),
                    formatBytes(extTotal),
                ),
            )
            storage.externalAvailable?.let {
                appendLine(
                    str(
                        R.string.deviceinfo_format_indented_label_value,
                        str(R.string.deviceinfo_label_available),
                        formatBytes(it),
                    ),
                )
            }
            storage.externalUsed?.let {
                appendLine(
                    str(
                        R.string.deviceinfo_format_indented_label_value,
                        str(R.string.deviceinfo_label_used),
                        formatBytes(it),
                    ),
                )
            }
        }
    }

    private fun formatAppSection(app: AppDetails): String = buildString {
        appendLine(labelValue(R.string.deviceinfo_app_package_name, app.packageName))
        appendLine(labelValue(R.string.deviceinfo_app_version_name, app.versionName))
        appendLine(labelValue(R.string.deviceinfo_app_version_code, app.versionCode.toString()))
        appendLine(labelValue(R.string.deviceinfo_app_target_sdk, app.targetSdk.toString()))
        appendLine(labelValue(R.string.deviceinfo_app_min_sdk, app.minSdk.toString()))
        appendLine(labelValue(R.string.deviceinfo_app_first_install, formatDateFull(app.firstInstallTime)))
        appendLine(labelValue(R.string.deviceinfo_app_last_update, formatDateFull(app.lastUpdateTime)))
        appendLine(labelValue(R.string.deviceinfo_app_debuggable, yesNo(app.isDebuggable)))
    }

    private fun formatNetworkSection(network: NetworkDetails): String = buildString {
        appendLine(labelValue(R.string.deviceinfo_network_connection_type, network.connectionType))
        appendLine(labelValue(R.string.deviceinfo_network_connected, yesNo(network.isConnected)))
        val connectedStr = str(R.string.deviceinfo_connected)
        val notConnectedStr = str(R.string.deviceinfo_not_connected)
        appendLine(
            labelValue(
                R.string.deviceinfo_network_wifi,
                if (network.isWifiConnected) connectedStr else notConnectedStr,
            ),
        )
        appendLine(
            labelValue(
                R.string.deviceinfo_network_cellular,
                if (network.isCellularConnected) connectedStr else notConnectedStr,
            ),
        )
        appendLine(labelValue(R.string.deviceinfo_network_metered, yesNo(network.isMetered)))
        network.cellularNetworkType?.let { appendLine(labelValue(R.string.deviceinfo_network_cellular_type, it)) }
    }
}

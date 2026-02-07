package com.azikar24.wormaceptor.domain.contracts

/**
 * Configuration for which features are enabled in WormaCeptor.
 * Used to dynamically show/hide tabs and menu items.
 */
data class FeatureConfig(
    val showNetworkTab: Boolean = true,
    val showCrashesTab: Boolean = true,
    val showPreferences: Boolean = true,
    val showConsoleLogs: Boolean = true,
    val showDeviceInfo: Boolean = true,
    val showSqliteBrowser: Boolean = true,
    val showFileBrowser: Boolean = true,
) {
    companion object {
        /**
         * Default configuration with all features enabled.
         */
        val DEFAULT = FeatureConfig()
    }
}

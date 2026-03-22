package com.azikar24.wormaceptor.domain.contracts

/**
 * Configuration for which features are enabled in WormaCeptor.
 * Used to dynamically show/hide tabs and menu items.
 */
data class FeatureConfig(
    /** Whether the network transactions tab is visible. */
    val showNetworkTab: Boolean = true,
    /** Whether the crashes tab is visible. */
    val showCrashesTab: Boolean = true,
    /** Whether the SharedPreferences viewer tool is available. */
    val showPreferences: Boolean = true,
    /** Whether the logcat console tool is available. */
    val showConsoleLogs: Boolean = true,
    /** Whether the device information tool is available. */
    val showDeviceInfo: Boolean = true,
    /** Whether the SQLite database browser tool is available. */
    val showSqliteBrowser: Boolean = true,
    /** Whether the filesystem browser tool is available. */
    val showFileBrowser: Boolean = true,
) {
    /** Default instances for [FeatureConfig]. */
    companion object {
        /**
         * Default configuration with all features enabled.
         */
        val DEFAULT = FeatureConfig()
    }
}

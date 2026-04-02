package com.azikar24.wormaceptor.feature.settings.vm

/**
 * User-initiated events dispatched to [SettingsViewModel].
 */
sealed class SettingsViewEvent {
    /** Toggles visibility of the Network tab. */
    data object ToggleNetworkTab : SettingsViewEvent()

    /** Toggles visibility of the Crashes tab. */
    data object ToggleCrashesTab : SettingsViewEvent()

    /** Toggles visibility of the Preferences inspector tool. */
    data object TogglePreferences : SettingsViewEvent()

    /** Toggles visibility of the Console Logs tool. */
    data object ToggleConsoleLogs : SettingsViewEvent()

    /** Toggles visibility of the Device Info tool. */
    data object ToggleDeviceInfo : SettingsViewEvent()

    /** Toggles visibility of the SQLite Browser tool. */
    data object ToggleSqliteBrowser : SettingsViewEvent()

    /** Toggles visibility of the File Browser tool. */
    data object ToggleFileBrowser : SettingsViewEvent()

    /** Restores all feature toggles to their default values. */
    data object ResetToDefaults : SettingsViewEvent()
}

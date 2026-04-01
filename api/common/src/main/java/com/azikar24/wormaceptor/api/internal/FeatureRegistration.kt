package com.azikar24.wormaceptor.api.internal

import com.azikar24.wormaceptor.core.ui.navigation.FeatureRegistry
import com.azikar24.wormaceptor.feature.cpu.CpuNavigationContributor
import com.azikar24.wormaceptor.feature.crypto.CryptoNavigationContributor
import com.azikar24.wormaceptor.feature.database.DatabaseNavigationContributor
import com.azikar24.wormaceptor.feature.dependenciesinspector.DependenciesInspectorNavigationContributor
import com.azikar24.wormaceptor.feature.deviceinfo.DeviceInfoNavigationContributor
import com.azikar24.wormaceptor.feature.filebrowser.FileBrowserNavigationContributor
import com.azikar24.wormaceptor.feature.fps.FpsNavigationContributor
import com.azikar24.wormaceptor.feature.leakdetection.LeakDetectionNavigationContributor
import com.azikar24.wormaceptor.feature.loadedlibraries.LoadedLibrariesNavigationContributor
import com.azikar24.wormaceptor.feature.location.LocationNavigationContributor
import com.azikar24.wormaceptor.feature.logs.LogsNavigationContributor
import com.azikar24.wormaceptor.feature.memory.MemoryNavigationContributor
import com.azikar24.wormaceptor.feature.preferences.PreferencesNavigationContributor
import com.azikar24.wormaceptor.feature.pushsimulator.PushSimulatorNavigationContributor
import com.azikar24.wormaceptor.feature.pushtoken.PushTokenNavigationContributor
import com.azikar24.wormaceptor.feature.ratelimit.RateLimitNavigationContributor
import com.azikar24.wormaceptor.feature.securestorage.SecureStorageNavigationContributor
import com.azikar24.wormaceptor.feature.threadviolation.ThreadViolationNavigationContributor
import com.azikar24.wormaceptor.feature.websocket.WebSocketNavigationContributor
import com.azikar24.wormaceptor.feature.webviewmonitor.WebViewMonitorNavigationContributor

/** Registers all feature navigation contributors with the [FeatureRegistry]. */
internal object FeatureRegistration {

    private var registered = false

    @Synchronized
    fun registerAll() {
        if (registered) return
        registered = true

        FeatureRegistry.register(LogsNavigationContributor())
        FeatureRegistry.register(DeviceInfoNavigationContributor())
        FeatureRegistry.register(MemoryNavigationContributor())
        FeatureRegistry.register(FpsNavigationContributor())
        FeatureRegistry.register(CpuNavigationContributor())
        FeatureRegistry.register(LocationNavigationContributor())
        FeatureRegistry.register(PushSimulatorNavigationContributor())
        FeatureRegistry.register(LeakDetectionNavigationContributor())
        FeatureRegistry.register(ThreadViolationNavigationContributor())
        FeatureRegistry.register(WebViewMonitorNavigationContributor())
        FeatureRegistry.register(CryptoNavigationContributor())
        FeatureRegistry.register(SecureStorageNavigationContributor())
        FeatureRegistry.register(RateLimitNavigationContributor())
        FeatureRegistry.register(PushTokenNavigationContributor())
        FeatureRegistry.register(LoadedLibrariesNavigationContributor())
        FeatureRegistry.register(DependenciesInspectorNavigationContributor())
        FeatureRegistry.register(FileBrowserNavigationContributor())
        FeatureRegistry.register(PreferencesNavigationContributor())
        FeatureRegistry.register(DatabaseNavigationContributor())
        FeatureRegistry.register(WebSocketNavigationContributor())
    }
}

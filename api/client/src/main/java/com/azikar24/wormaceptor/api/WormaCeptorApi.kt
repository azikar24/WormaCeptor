package com.azikar24.wormaceptor.api

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.platform.android.ShakeDetector

object WormaCeptorApi {

    @Volatile
    internal var provider: ServiceProvider? = null
        private set

    @Volatile
    private var enabledFeatures: Set<Feature> = Feature.DEFAULT

    val redactionConfig = RedactionConfig()

    /**
     * Initializes WormaCeptor.
     * If an implementation module (persistence, imdb) is present in the classpath,
     * it will be automatically discovered and initialized.
     *
     * @param context Application context
     * @param logCrashes Whether to capture uncaught exceptions
     * @param features Set of features to enable (defaults to all features)
     */
    fun init(
        context: Context,
        logCrashes: Boolean = true,
        features: Set<Feature> = Feature.DEFAULT
    ) {
        if (provider != null) return

        enabledFeatures = features

        // Discovery via reflection to avoid compile-time dependency on implementation modules
        val implClass = try {
            Class.forName("com.azikar24.wormaceptor.api.internal.ServiceProviderImpl")
        } catch (e: Exception) {
            null
        }

        val instance = implClass?.getDeclaredConstructor()?.newInstance() as? ServiceProvider
        provider = instance ?: NoOpProvider()

        provider?.init(context, logCrashes)
    }

    /**
     * Check if a specific feature is enabled.
     * @param feature The feature to check
     * @return true if the feature is enabled, false otherwise
     */
    fun isFeatureEnabled(feature: Feature): Boolean = feature in enabledFeatures

    /**
     * Get all currently enabled features.
     * @return A copy of the enabled features set
     */
    fun getEnabledFeatures(): Set<Feature> = enabledFeatures.toSet()

    fun startActivityOnShake(activity: ComponentActivity) {
        // Platform classes are safe to refer if they are in a common layout or handled similarly
        // For simplicity, we'll delegate shake start to provider if needed,
        // or keep it in platform if platform is a shared dependency.
        try {
            ShakeDetector.start(activity) {
                activity.startActivity(getLaunchIntent(activity))
            }
        } catch (e: Exception) {
            // Shake detector might not be present in No-Op
        }
    }

    fun getLaunchIntent(context: Context): Intent {
        return provider?.getLaunchIntent(context) ?: Intent()
    }

    // ========== Floating Button API ==========

    /**
     * Check if the app has permission to show floating button (overlay permission).
     * Returns true if permission is granted, false otherwise.
     * On API < 23, always returns true.
     */
    fun canShowFloatingButton(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Show the floating button overlay.
     * The button is draggable and snaps to screen edges when released.
     * Tapping the button opens the ViewerActivity.
     *
     * Note: Requires SYSTEM_ALERT_WINDOW permission.
     * Call [canShowFloatingButton] first to check if permission is granted.
     * If not granted, direct user to Settings.ACTION_MANAGE_OVERLAY_PERMISSION.
     *
     * @param context Application or Activity context
     * @return true if the floating button was started, false if permission not granted
     */
    fun showFloatingButton(context: Context): Boolean {
        if (!canShowFloatingButton(context)) {
            return false
        }

        // Use reflection to start FloatingButtonService to avoid hard dependency
        return try {
            val serviceClass = Class.forName(
                "com.azikar24.wormaceptor.platform.android.FloatingButtonService"
            )
            val startMethod = serviceClass.getDeclaredMethod("start", Context::class.java)
            val companion = serviceClass.getDeclaredField("Companion").get(null)
            val companionClass = companion.javaClass
            val companionStartMethod = companionClass.getMethod("start", Context::class.java)
            companionStartMethod.invoke(companion, context)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Hide the floating button overlay.
     *
     * @param context Application or Activity context
     */
    fun hideFloatingButton(context: Context) {
        // Use reflection to stop FloatingButtonService to avoid hard dependency
        try {
            val serviceClass = Class.forName(
                "com.azikar24.wormaceptor.platform.android.FloatingButtonService"
            )
            val companion = serviceClass.getDeclaredField("Companion").get(null)
            val companionClass = companion.javaClass
            val companionStopMethod = companionClass.getMethod("stop", Context::class.java)
            companionStopMethod.invoke(companion, context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get an Intent to open the system overlay permission settings.
     * Use this when [canShowFloatingButton] returns false.
     *
     * @param context Application context
     * @return Intent to open overlay settings, or null if not needed (API < 23)
     */
    fun getOverlayPermissionIntent(context: Context): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:${context.packageName}")
            )
        } else {
            null
        }
    }

    // ========== Performance Overlay API ==========

    /**
     * Show the performance overlay on top of the app.
     * The overlay displays real-time FPS, Memory, and CPU metrics.
     * It is draggable and can be expanded to show mini sparkline charts.
     * Tapping the overlay opens the detailed performance screens.
     *
     * Note: Requires SYSTEM_ALERT_WINDOW permission.
     * Call [canShowFloatingButton] first to check if permission is granted.
     *
     * @param activity The activity to attach the overlay to
     * @return true if the overlay was shown, false if permission not granted
     */
    fun showPerformanceOverlay(activity: ComponentActivity): Boolean {
        if (!canShowFloatingButton(activity)) {
            return false
        }

        return try {
            val engineClass = Class.forName(
                "com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine"
            )
            val koinClass = Class.forName("org.koin.java.KoinJavaComponent")
            val getMethod = koinClass.getMethod("get", Class::class.java)
            val engine = getMethod.invoke(null, engineClass)
            val showMethod = engineClass.getMethod("show", ComponentActivity::class.java.superclass)
            showMethod.invoke(engine, activity)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Hide the performance overlay.
     */
    fun hidePerformanceOverlay() {
        try {
            val engineClass = Class.forName(
                "com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine"
            )
            val koinClass = Class.forName("org.koin.java.KoinJavaComponent")
            val getMethod = koinClass.getMethod("get", Class::class.java)
            val engine = getMethod.invoke(null, engineClass)
            val hideMethod = engineClass.getMethod("hide")
            hideMethod.invoke(engine)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Check if the performance overlay is currently visible.
     *
     * @return true if the overlay is visible, false otherwise
     */
    fun isPerformanceOverlayVisible(): Boolean {
        return try {
            val engineClass = Class.forName(
                "com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine"
            )
            val koinClass = Class.forName("org.koin.java.KoinJavaComponent")
            val getMethod = koinClass.getMethod("get", Class::class.java)
            val engine = getMethod.invoke(null, engineClass)
            val isVisibleField = engineClass.getMethod("isVisible")
            val stateFlow = isVisibleField.invoke(engine)
            val valueMethod = stateFlow.javaClass.getMethod("getValue")
            valueMethod.invoke(stateFlow) as? Boolean ?: false
        } catch (e: Exception) {
            false
        }
    }

    // ========== Extension Provider API ==========

    /**
     * Register a custom extension provider.
     * Extension providers extract custom metadata from network transactions.
     * The extracted data will be stored with the transaction and displayed in the UI.
     *
     * @param provider The extension provider to register
     */
    fun registerExtensionProvider(provider: ExtensionProvider) {
        CoreHolder.extensionRegistry?.register(provider)
    }

    /**
     * Unregister an extension provider by name.
     *
     * @param name The name of the provider to unregister
     * @return true if a provider was removed, false otherwise
     */
    fun unregisterExtensionProvider(name: String): Boolean {
        return CoreHolder.extensionRegistry?.unregister(name) ?: false
    }

    /**
     * Get the names of all registered extension providers.
     *
     * @return List of provider names, or empty list if not initialized
     */
    fun getRegisteredExtensionProviders(): List<String> {
        return CoreHolder.extensionRegistry?.getRegisteredProviders() ?: emptyList()
    }

    private class NoOpProvider : ServiceProvider {
        override fun init(context: Context, logCrashes: Boolean) {}
        override fun startTransaction(
            url: String,
            method: String,
            headers: Map<String, List<String>>,
            bodyStream: java.io.InputStream?,
            bodySize: Long,
        ) = null

        override fun completeTransaction(
            id: java.util.UUID,
            code: Int,
            message: String,
            headers: Map<String, List<String>>,
            bodyStream: java.io.InputStream?,
            bodySize: Long,
            protocol: String?,
            tlsVersion: String?,
            error: String?,
        ) {
        }

        override fun cleanup(threshold: Long) {}
        override fun getLaunchIntent(context: Context): Intent = Intent()
        override fun getAllTransactions(): List<Any> = emptyList()
        override fun getTransaction(id: String): Any? = null
        override fun getTransactionCount(): Int = 0
        override fun clearTransactions() {}
        override fun getTransactionDetail(id: String): TransactionDetailDto? = null
    }
}

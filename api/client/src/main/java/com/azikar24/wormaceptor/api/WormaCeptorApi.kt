package com.azikar24.wormaceptor.api

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import com.azikar24.wormaceptor.platform.android.ShakeDetector

object WormaCeptorApi {

    @Volatile
    internal var provider: ServiceProvider? = null
        private set

    val redactionConfig = RedactionConfig()

    /**
     * Initializes WormaCeptor.
     * If an implementation module (persistence, imdb) is present in the classpath,
     * it will be automatically discovered and initialized.
     */
    fun init(context: Context, logCrashes: Boolean = true) {
        if (provider != null) return

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

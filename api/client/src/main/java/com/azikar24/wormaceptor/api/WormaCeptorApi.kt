package com.azikar24.wormaceptor.api

import android.content.Context
import android.content.Intent
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

    private class NoOpProvider : ServiceProvider {
        override fun init(context: Context, logCrashes: Boolean) {}
        override fun startTransaction(
            url: String,
            method: String,
            headers: Map<String, List<String>>,
            bodyStream: java.io.InputStream?,
            bodySize: Long
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
            error: String?
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

package com.azikar24.wormaceptor.api.internal

import android.content.Context
import android.content.Intent
import com.azikar24.wormaceptor.api.ServiceProvider
import java.io.InputStream
import java.util.UUID

internal class ServiceProviderImpl : ServiceProvider {
    override fun init(context: Context, logCrashes: Boolean) {}
    override fun startTransaction(url: String, method: String, headers: Map<String, List<String>>, bodyStream: InputStream?, bodySize: Long): UUID? = null
    override fun completeTransaction(id: UUID, code: Int, message: String, headers: Map<String, List<String>>, bodyStream: InputStream?, bodySize: Long, protocol: String?, tlsVersion: String?, error: String?) {}
    override fun cleanup(threshold: Long) {}
    override fun getLaunchIntent(context: Context): Intent = Intent()
}

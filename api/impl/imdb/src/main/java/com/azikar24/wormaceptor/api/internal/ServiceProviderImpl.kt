package com.azikar24.wormaceptor.api.internal

import android.content.Context
import android.content.Intent
import com.azikar24.wormaceptor.api.ServiceProvider
import com.azikar24.wormaceptor.core.engine.CaptureEngine
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.CrashReporter
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.infra.persistence.sqlite.InMemoryBlobStorage
import com.azikar24.wormaceptor.infra.persistence.sqlite.InMemoryCrashRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.InMemoryTransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.InputStream
import java.util.UUID

internal class ServiceProviderImpl : ServiceProvider {
    
    private var captureEngine: CaptureEngine? = null
    private var queryEngine: QueryEngine? = null
    private var notificationHelper: com.azikar24.wormaceptor.api.internal.WormaCeptorNotificationHelper? = null

    override fun init(context: Context, logCrashes: Boolean) {
        if (captureEngine != null) return

        val repository = InMemoryTransactionRepository()
        val crashRepository = InMemoryCrashRepository()
        val blobStorage = InMemoryBlobStorage()
        
        captureEngine = CaptureEngine(repository, blobStorage)
        queryEngine = QueryEngine(repository, blobStorage, crashRepository)
        
        if (logCrashes) {
            val crashReporter = CrashReporter(crashRepository)
            crashReporter.init()
        }
        
        notificationHelper = com.azikar24.wormaceptor.api.internal.WormaCeptorNotificationHelper(context)

        // Share with Feature Modules
        CoreHolder.captureEngine = captureEngine
        CoreHolder.queryEngine = queryEngine
    }

    override fun startTransaction(url: String, method: String, headers: Map<String, List<String>>, bodyStream: InputStream?, bodySize: Long): UUID? = runBlocking {
        captureEngine?.startTransaction(url, method, headers, bodyStream, bodySize)
    }

    override fun completeTransaction(id: UUID, code: Int, message: String, headers: Map<String, List<String>>, bodyStream: InputStream?, bodySize: Long, protocol: String?, tlsVersion: String?, error: String?) {
        runBlocking {
            captureEngine?.completeTransaction(id, code, message, headers, bodyStream, bodySize, protocol, tlsVersion, error)
            val transaction = queryEngine?.getDetails(id)
            if (transaction != null) {
                notificationHelper?.show(transaction)
            }
        }
    }

    override fun cleanup(threshold: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            captureEngine?.cleanup(threshold)
        }
    }

    override fun getLaunchIntent(context: Context): Intent {
        return Intent(context, com.azikar24.wormaceptor.feature.viewer.ViewerActivity::class.java)
    }
}

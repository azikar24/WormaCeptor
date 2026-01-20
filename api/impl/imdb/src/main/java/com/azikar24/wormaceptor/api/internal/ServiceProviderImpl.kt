package com.azikar24.wormaceptor.api.internal

import android.content.Context
import com.azikar24.wormaceptor.core.engine.CaptureEngine
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.CrashReporter
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.infra.persistence.sqlite.InMemoryBlobStorage
import com.azikar24.wormaceptor.infra.persistence.sqlite.InMemoryCrashRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.InMemoryTransactionRepository

internal class ServiceProviderImpl : BaseServiceProviderImpl() {

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

        notificationHelper = WormaCeptorNotificationHelper(context, "WormaCeptor (IMDB): Recording...")

        CoreHolder.captureEngine = captureEngine
        CoreHolder.queryEngine = queryEngine
    }
}

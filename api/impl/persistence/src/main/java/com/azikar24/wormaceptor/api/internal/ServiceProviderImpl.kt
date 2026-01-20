package com.azikar24.wormaceptor.api.internal

import android.content.Context
import androidx.room.Room
import com.azikar24.wormaceptor.core.engine.CaptureEngine
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.CrashReporter
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.infra.persistence.sqlite.FileSystemBlobStorage
import com.azikar24.wormaceptor.infra.persistence.sqlite.RoomCrashRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.RoomTransactionRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.WormaCeptorDatabase

internal class ServiceProviderImpl : BaseServiceProviderImpl() {

    override fun init(context: Context, logCrashes: Boolean) {
        if (captureEngine != null) return

        val database = Room.databaseBuilder(
            context.applicationContext,
            WormaCeptorDatabase::class.java,
            "wormaceptor-v2.db",
        )
            .fallbackToDestructiveMigration()
            .build()

        val repository = RoomTransactionRepository(database.transactionDao())
        val crashRepository = RoomCrashRepository(database.crashDao())
        val blobStorage = FileSystemBlobStorage(context.applicationContext)

        captureEngine = CaptureEngine(repository, blobStorage)
        queryEngine = QueryEngine(repository, blobStorage, crashRepository)

        if (logCrashes) {
            val crashReporter = CrashReporter(crashRepository)
            crashReporter.init()
        }

        notificationHelper = WormaCeptorNotificationHelper(context)

        CoreHolder.captureEngine = captureEngine
        CoreHolder.queryEngine = queryEngine
    }
}

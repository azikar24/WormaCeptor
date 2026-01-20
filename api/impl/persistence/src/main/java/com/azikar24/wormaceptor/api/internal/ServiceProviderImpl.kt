package com.azikar24.wormaceptor.api.internal

import android.content.Context
import androidx.room.Room
import com.azikar24.wormaceptor.infra.persistence.sqlite.FileSystemBlobStorage
import com.azikar24.wormaceptor.infra.persistence.sqlite.RoomCrashRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.RoomTransactionRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.WormaCeptorDatabase

internal class ServiceProviderImpl : BaseServiceProviderImpl() {

    override fun createDependencies(context: Context): StorageDependencies {
        val database = Room.databaseBuilder(
            context.applicationContext,
            WormaCeptorDatabase::class.java,
            "wormaceptor-v2.db",
        )
            .fallbackToDestructiveMigration()
            .build()

        return StorageDependencies(
            transactionRepository = RoomTransactionRepository(database.transactionDao()),
            crashRepository = RoomCrashRepository(database.crashDao()),
            blobStorage = FileSystemBlobStorage(context.applicationContext),
        )
    }

    override fun getNotificationTitle() = "WormaCeptor: Recording..."
}

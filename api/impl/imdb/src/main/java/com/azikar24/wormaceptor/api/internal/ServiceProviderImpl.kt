package com.azikar24.wormaceptor.api.internal

import android.content.Context
import com.azikar24.wormaceptor.infra.persistence.sqlite.InMemoryBlobStorage
import com.azikar24.wormaceptor.infra.persistence.sqlite.InMemoryCrashRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.InMemoryLeakRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.InMemoryTransactionRepository

internal class ServiceProviderImpl : BaseServiceProviderImpl() {

    override fun createDependencies(context: Context) = StorageDependencies(
        transactionRepository = InMemoryTransactionRepository(),
        crashRepository = InMemoryCrashRepository(),
        blobStorage = InMemoryBlobStorage(),
        leakRepository = InMemoryLeakRepository(),
    )

    override fun getNotificationTitle() = "WormaCeptor (IMDB): Recording..."
}

/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.imdb

import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.internal.data.TransactionDao
import com.azikar24.wormaceptor.internal.data.WormaCeptorStorage
import java.util.concurrent.atomic.AtomicBoolean

class WormaCeptorIMDB : WormaCeptorStorage {
    override val transactionDao: TransactionDao
        get() = IMDB_TRANSACTION_DAO

    companion object {
        private val IMDB_TRANSACTION_DAO: TransactionDao = IMDBTransactionDao(
            SimpleNetworkTransactionDataStore(),
            SimpleCrashTransactionDataStore(),
            TransactionArchComponentProvider(),
            TransactionPredicateProvider()
        )

        private var INSTANCE: WormaCeptorIMDB? = null
        private val initialized = AtomicBoolean(false)

        fun getInstance(): WormaCeptorIMDB {
            if (!initialized.getAndSet(true)) {
                INSTANCE = WormaCeptorIMDB()
                WormaCeptor.storage = INSTANCE
                WormaCeptor.type = WormaCeptor.WormaCeptorType.IMDB
            }
            return INSTANCE!!
        }
    }
}

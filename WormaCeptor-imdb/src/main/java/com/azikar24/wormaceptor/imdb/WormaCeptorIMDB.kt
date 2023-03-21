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
            TransactionArchComponentProvider(),
            TransactionPredicateProvider()
        )

        private lateinit var INSTANCE: WormaCeptorIMDB
        private val initialized = AtomicBoolean(true)
        fun getInstance(): WormaCeptorIMDB {
            WormaCeptor.type = WormaCeptor.WormaCeptorType.IMDB

            if (initialized.getAndSet(true)) {
                INSTANCE = WormaCeptorIMDB()
            }
            return INSTANCE
        }
    }
}
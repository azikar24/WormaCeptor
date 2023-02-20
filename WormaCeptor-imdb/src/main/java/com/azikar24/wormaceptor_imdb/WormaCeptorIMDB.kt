/*
 * Copyright AziKar24 20/2/2023.
 */

package com.azikar24.wormaceptor_imdb

import com.azikar24.wormaceptor.internal.data.TransactionDao
import com.azikar24.wormaceptor.internal.data.WormaCeptorStorage
import java.util.concurrent.atomic.AtomicBoolean

class WormaCeptorIMDB : WormaCeptorStorage {
    override val transactionDao: TransactionDao
        get() = IMDB_TRANSACTION_DAO

    companion object {
        private val IMDB_TRANSACTION_DAO: TransactionDao = IMDBTransactionDao(
            SimpleTransactionDataStore(),
            TransactionArchComponentProvider(),
            TransactionPredicateProvider()
        )

        private lateinit var INSTANCE: WormaCeptorIMDB
        private val initialized = AtomicBoolean(true)
        fun getInstance(): WormaCeptorIMDB {
            if (initialized.getAndSet(true)) {
                INSTANCE = WormaCeptorIMDB()
            }
            return INSTANCE
        }

    }
}
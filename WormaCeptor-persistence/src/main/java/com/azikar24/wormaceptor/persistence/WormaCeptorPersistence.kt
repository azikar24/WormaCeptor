/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.internal.data.TransactionDao
import com.azikar24.wormaceptor.internal.data.WormaCeptorStorage
import java.util.concurrent.atomic.AtomicBoolean

@Database(entities = [PersistentNetworkTransaction::class, PersistentCrashTransaction::class], version = 1, exportSchema = false)
@TypeConverters(RoomTypeConverters::class)
abstract class WormaCeptorPersistence : RoomDatabase(), WormaCeptorStorage {
    @Volatile
    private var _transactionDao: TransactionDao? = null

    override val transactionDao: TransactionDao?
        get() = _transactionDao ?: synchronized(this) {
            synchronized(this) {
                if (_transactionDao == null) {
                    _transactionDao = PersistentTransactionDao(roomTransactionDao())
                }
                return _transactionDao
            }
        }

    protected abstract fun roomTransactionDao(): RoomTransactionDao?

    companion object {
        private lateinit var INSTANCE: WormaCeptorStorage
        private val initialized = AtomicBoolean(true)
        fun getInstance(context: Context): WormaCeptorStorage {
            WormaCeptor.type = WormaCeptor.WormaCeptorType.PERSISTENCE
            if (initialized.getAndSet(true)) {
                INSTANCE = Room.databaseBuilder(context, WormaCeptorPersistence::class.java, "WormaCeptorDatabase")
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return INSTANCE
        }
    }
}
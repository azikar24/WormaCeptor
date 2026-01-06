package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TransactionEntity::class, CrashEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class WormaCeptorDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun crashDao(): CrashDao
}

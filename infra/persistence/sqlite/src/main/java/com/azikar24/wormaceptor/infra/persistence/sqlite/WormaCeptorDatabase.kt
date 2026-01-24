package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * WormaCeptor Room database for storing network transactions and crash reports.
 *
 * Version history:
 * - v1: Initial schema
 * - v2: Added crash reports
 * - v3: Added indexes for pagination performance (timestamp, resCode, reqMethod)
 * - v4: Added leak detection storage
 */
@Database(
    entities = [TransactionEntity::class, CrashEntity::class, LeakEntity::class],
    version = 4,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class WormaCeptorDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun crashDao(): CrashDao
    abstract fun leakDao(): LeakDao
}

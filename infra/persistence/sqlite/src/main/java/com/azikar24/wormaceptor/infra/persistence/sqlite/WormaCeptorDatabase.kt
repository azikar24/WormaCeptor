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
 * - v5: Added location presets, mock location, and push templates
 * - v6: Added WebView request storage
 * - v7: Added mock rule persistence
 */
@Database(
    entities = [
        TransactionEntity::class,
        CrashEntity::class,
        LeakEntity::class,
        LocationPresetEntity::class,
        MockLocationEntity::class,
        PushTemplateEntity::class,
        WebViewRequestEntity::class,
        MockRuleEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class WormaCeptorDatabase : RoomDatabase() {
    /** Provides access to network transaction CRUD operations. */
    abstract fun transactionDao(): TransactionDao

    /** Provides access to crash report CRUD operations. */
    abstract fun crashDao(): CrashDao

    /** Provides access to memory leak record CRUD operations. */
    abstract fun leakDao(): LeakDao

    /** Provides access to location preset CRUD operations. */
    abstract fun locationPresetDao(): LocationPresetDao

    /** Provides access to mock location state CRUD operations. */
    abstract fun mockLocationDao(): MockLocationDao

    /** Provides access to push notification template CRUD operations. */
    abstract fun pushTemplateDao(): PushTemplateDao

    /** Provides access to WebView request CRUD operations. */
    abstract fun webViewRequestDao(): WebViewRequestDao

    /** Provides access to mock rule CRUD operations. */
    abstract fun mockRuleDao(): MockRuleDao
}

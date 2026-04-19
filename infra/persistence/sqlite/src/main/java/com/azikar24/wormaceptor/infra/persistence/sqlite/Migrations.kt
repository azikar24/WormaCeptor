@file:Suppress("UndocumentedPublicProperty", "MagicNumber")

package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration6To7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `mock_rules` (" +
                "`id` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`enabled` INTEGER NOT NULL, " +
                "`urlPattern` TEXT NOT NULL, " +
                "`matchType` TEXT NOT NULL, " +
                "`method` TEXT, " +
                "`matcherHeadersJson` TEXT NOT NULL, " +
                "`statusCode` INTEGER NOT NULL, " +
                "`statusMessage` TEXT NOT NULL, " +
                "`contentType` TEXT NOT NULL, " +
                "`body` TEXT, " +
                "`responseHeadersJson` TEXT NOT NULL, " +
                "`delayJson` TEXT NOT NULL, " +
                "`behaviorJson` TEXT NOT NULL, " +
                "`priority` INTEGER NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, " +
                "PRIMARY KEY(`id`))",
        )
    }
}

val AllMigrations = arrayOf(Migration6To7)

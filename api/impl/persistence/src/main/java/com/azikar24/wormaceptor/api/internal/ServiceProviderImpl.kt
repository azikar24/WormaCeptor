package com.azikar24.wormaceptor.api.internal

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.room.Room
import com.azikar24.wormaceptor.infra.persistence.sqlite.FileSystemBlobStorage
import com.azikar24.wormaceptor.infra.persistence.sqlite.KeystoreKeyManager
import com.azikar24.wormaceptor.infra.persistence.sqlite.RoomCrashRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.RoomLeakRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.RoomLocationSimulatorRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.RoomPushSimulatorRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.RoomTransactionRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.RoomWebViewMonitorRepository
import com.azikar24.wormaceptor.infra.persistence.sqlite.WormaCeptorDatabase
import net.sqlcipher.database.SupportFactory
import java.io.File

internal class ServiceProviderImpl : BaseServiceProviderImpl() {

    override fun createDependencies(context: Context): StorageDependencies {
        val dbName = "wormaceptor-v2.db"
        val passphrase = KeystoreKeyManager.getOrCreatePassphrase(context)

        // Encrypt existing unencrypted DB in-place, preserving all data
        encryptDatabaseIfNeeded(context, dbName, passphrase)

        val database = Room.databaseBuilder(
            context.applicationContext,
            WormaCeptorDatabase::class.java,
            dbName,
        )
            .openHelperFactory(SupportFactory(passphrase))
            .fallbackToDestructiveMigration()
            .build()

        return StorageDependencies(
            transactionRepository = RoomTransactionRepository(database.transactionDao()),
            crashRepository = RoomCrashRepository(database.crashDao()),
            blobStorage = FileSystemBlobStorage(context.applicationContext),
            leakRepository = RoomLeakRepository(database.leakDao()),
            locationSimulatorRepository = RoomLocationSimulatorRepository(
                database.locationPresetDao(),
                database.mockLocationDao(),
            ),
            pushSimulatorRepository = RoomPushSimulatorRepository(database.pushTemplateDao()),
            webViewMonitorRepository = RoomWebViewMonitorRepository(database.webViewRequestDao()),
        )
    }

    override fun getNotificationTitle() = "WormaCeptor: Recording..."

    /**
     * Migrates an existing unencrypted database to SQLCipher encryption without data loss.
     *
     * Reads all schema and data from the plaintext DB using standard Android SQLite,
     * writes it into a new SQLCipher-encrypted DB, then swaps the files.
     * This avoids asking SQLCipher to open a plaintext file (which fails in 4.x).
     */
    private fun encryptDatabaseIfNeeded(
        context: Context,
        dbName: String,
        passphrase: ByteArray,
    ) {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(dbName)
        if (!dbFile.exists()) return
        if (!isUnencryptedSqlite(dbFile)) return

        net.sqlcipher.database.SQLiteDatabase.loadLibs(appContext)

        val tempFile = File(dbFile.parent, "$dbName-encrypting")
        tempFile.delete()

        try {
            // Open plaintext DB with standard Android SQLite (guaranteed to work)
            val plainDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                android.database.sqlite.SQLiteDatabase.OPEN_READONLY,
            )

            // Create new encrypted DB with SQLCipher
            val encDb = net.sqlcipher.database.SQLiteDatabase.openOrCreateDatabase(
                tempFile,
                String(passphrase, Charsets.UTF_8),
                null,
            )

            // Copy schema: replay all CREATE TABLE/INDEX/TRIGGER/VIEW statements
            val schemaCursor = plainDb.rawQuery(
                "SELECT sql FROM sqlite_master WHERE sql IS NOT NULL ORDER BY type DESC",
                null,
            )
            while (schemaCursor.moveToNext()) {
                try {
                    encDb.execSQL(schemaCursor.getString(0))
                } catch (_: Exception) {
                    // Skip if table already exists or statement fails
                }
            }
            schemaCursor.close()

            // Get all user table names
            val tables = mutableListOf<String>()
            val tablesCursor = plainDb.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table'" +
                    " AND name NOT LIKE 'sqlite_%'" +
                    " AND name NOT LIKE 'android_%'",
                null,
            )
            while (tablesCursor.moveToNext()) {
                tables.add(tablesCursor.getString(0))
            }
            tablesCursor.close()

            // Copy all data table by table inside a single transaction
            encDb.beginTransaction()
            try {
                for (table in tables) {
                    copyTableData(plainDb, encDb, table)
                }
                encDb.setTransactionSuccessful()
            } finally {
                encDb.endTransaction()
            }

            plainDb.close()
            encDb.close()

            // Swap: delete old plaintext files, rename encrypted to original name
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()
            dbFile.delete()
            tempFile.renameTo(dbFile)

            Log.i(TAG, "Database encrypted successfully — all data preserved")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to encrypt database, deleting to start fresh", e)
            tempFile.delete()
            dbFile.delete()
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()
        }
    }

    private fun copyTableData(
        source: android.database.sqlite.SQLiteDatabase,
        dest: net.sqlcipher.database.SQLiteDatabase,
        table: String,
    ) {
        val cursor = source.rawQuery("SELECT * FROM \"$table\"", null)
        while (cursor.moveToNext()) {
            val values = ContentValues(cursor.columnCount)
            for (i in 0 until cursor.columnCount) {
                val col = cursor.getColumnName(i)
                when (cursor.getType(i)) {
                    Cursor.FIELD_TYPE_NULL -> values.putNull(col)
                    Cursor.FIELD_TYPE_INTEGER -> values.put(col, cursor.getLong(i))
                    Cursor.FIELD_TYPE_FLOAT -> values.put(col, cursor.getDouble(i))
                    Cursor.FIELD_TYPE_STRING -> values.put(col, cursor.getString(i))
                    Cursor.FIELD_TYPE_BLOB -> values.put(col, cursor.getBlob(i))
                }
            }
            dest.insert(table, null, values)
        }
        cursor.close()
    }

    private fun isUnencryptedSqlite(file: File): Boolean = try {
        file.inputStream().use { stream ->
            val header = ByteArray(16)
            stream.read(header)
            String(header, 0, 15, Charsets.US_ASCII) == "SQLite format 3"
        }
    } catch (_: Exception) {
        false
    }

    private companion object {
        private const val TAG = "ServiceProviderImpl"
    }
}

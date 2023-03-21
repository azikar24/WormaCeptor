/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.persistence

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import java.util.*

@Dao
abstract class RoomTransactionDao {

    @Query("DELETE FROM CrashTransaction")
    abstract fun clearAllCrashes(): Int

    @Delete
    abstract fun deleteCrash(vararg persistentCrashTransaction: PersistentCrashTransaction?): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertCrash(persistentCrashTransaction: PersistentCrashTransaction?)

    @get:Query("SELECT * FROM CrashTransaction ORDER BY id DESC")
    abstract val allCrashes: DataSource.Factory<Int, PersistentCrashTransaction>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertNetworkTransaction(
        persistentNetworkTransaction: PersistentNetworkTransaction?
    ): Long?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateNetworkTransaction(
        persistentNetworkTransactions: PersistentNetworkTransaction?
    ): Int?

    @Delete
    abstract fun deleteNetworkTransactions(
        vararg persistentNetworkTransactions: PersistentNetworkTransaction?
    ): Int?

    @Query("DELETE FROM NetworkTransaction WHERE request_date < :beforeDate")
    abstract fun deleteNetworkTransactionsBefore(beforeDate: Date?): Int?

    @Query("DELETE FROM NetworkTransaction")
    abstract fun clearAllNetworkTransactions(): Int

    @get:Query("SELECT * FROM NetworkTransaction ORDER BY id DESC")
    abstract val allNetworkTransactions: DataSource.Factory<Int, PersistentNetworkTransaction>?

    @Query("SELECT * FROM NetworkTransaction WHERE id = :id")
    abstract fun getNetworkTransactionsWithId(id: Long): LiveData<PersistentNetworkTransaction>?

    @Query("SELECT id, method, url, path, host, scheme, request_date, error, response_code, took_ms, request_content_length, response_content_length, request_body_is_plain_text, response_body_is_plain_text FROM NetworkTransaction WHERE protocol LIKE :endWildCard OR method LIKE :endWildCard OR url LIKE :doubleWildCard OR request_body LIKE :doubleWildCard OR response_body LIKE :doubleWildCard OR response_message LIKE :doubleWildCard OR response_code LIKE :endWildCard ORDER BY id DESC")
    abstract fun getAllNetworkTransactionsIncludeRequestResponse(
        endWildCard: String?,
        doubleWildCard: String?
    ): DataSource.Factory<Int, PersistentNetworkTransaction>?

    @Query("SELECT id, method, url, path, host, scheme, request_date, error, response_code, took_ms, request_content_length, response_content_length, request_body_is_plain_text, response_body_is_plain_text FROM NetworkTransaction WHERE protocol LIKE :endWildCard OR method LIKE :endWildCard OR url LIKE :doubleWildCard OR response_body LIKE :doubleWildCard OR response_message LIKE :doubleWildCard OR response_code LIKE :endWildCard ORDER BY id DESC")
    abstract fun getAllNetworkTransactionsIncludeResponse(
        endWildCard: String?,
        doubleWildCard: String?
    ): DataSource.Factory<Int, PersistentNetworkTransaction>?

    @Query("SELECT id, method, url, path, host, scheme, request_date, error, response_code, took_ms, request_content_length, response_content_length, request_body_is_plain_text, response_body_is_plain_text FROM NetworkTransaction WHERE protocol LIKE :endWildCard OR method LIKE :endWildCard OR url LIKE :doubleWildCard OR request_body LIKE :doubleWildCard OR response_code LIKE :endWildCard ORDER BY id DESC")
    abstract fun getAllNetworkTransactionsIncludeRequest(
        endWildCard: String?,
        doubleWildCard: String?
    ): DataSource.Factory<Int, PersistentNetworkTransaction>?

    @Query("SELECT id, method, url, path, host, scheme, request_date, error, response_code, took_ms, request_content_length, response_content_length, request_body_is_plain_text, response_body_is_plain_text FROM NetworkTransaction WHERE protocol LIKE :endWildCard OR method LIKE :endWildCard OR url LIKE :doubleWildCard OR response_code LIKE :endWildCard ORDER BY id DESC")
    abstract fun getAllNetworkTransactions(endWildCard: String?, doubleWildCard: String?): DataSource.Factory<Int, PersistentNetworkTransaction>?
}

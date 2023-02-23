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
    abstract fun insertCrash(oersistentCrashTransaction: PersistentCrashTransaction?)

    @get:Query("SELECT * FROM CrashTransaction ORDER BY id DESC")
    abstract val allCrashes: DataSource.Factory<Int, PersistentCrashTransaction>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTransaction(persistentHttpTransaction: PersistentHttpTransaction?): Long?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateTransaction(persistentHttpTransactions: PersistentHttpTransaction?): Int?

    @Delete
    abstract fun deleteTransactions(vararg persistentHttpTransactions: PersistentHttpTransaction?): Int?

    @Query("DELETE FROM HttpTransaction WHERE request_date < :beforeDate")
    abstract fun deleteTransactionsBefore(beforeDate: Date?): Int?

    @Query("DELETE FROM HttpTransaction")
    abstract fun clearAll(): Int

    @get:Query("SELECT * FROM HttpTransaction ORDER BY id DESC")
    abstract val allTransactions: DataSource.Factory<Int, PersistentHttpTransaction>?

    @Query("SELECT * FROM HttpTransaction WHERE id = :id")
    abstract fun getTransactionsWithId(id: Long): LiveData<PersistentHttpTransaction>?

    @Query("SELECT id, method, url, path, host, scheme, request_date, error, response_code, took_ms, request_content_length, response_content_length, request_body_is_plain_text, response_body_is_plain_text FROM HttpTransaction WHERE protocol LIKE :endWildCard OR method LIKE :endWildCard OR url LIKE :doubleWildCard OR request_body LIKE :doubleWildCard OR response_body LIKE :doubleWildCard OR response_message LIKE :doubleWildCard OR response_code LIKE :endWildCard ORDER BY id DESC")
    abstract fun getAllTransactionsIncludeRequestResponse(endWildCard: String?, doubleWildCard: String?): DataSource.Factory<Int, PersistentHttpTransaction>?

    @Query("SELECT id, method, url, path, host, scheme, request_date, error, response_code, took_ms, request_content_length, response_content_length, request_body_is_plain_text, response_body_is_plain_text FROM HttpTransaction WHERE protocol LIKE :endWildCard OR method LIKE :endWildCard OR url LIKE :doubleWildCard OR response_body LIKE :doubleWildCard OR response_message LIKE :doubleWildCard OR response_code LIKE :endWildCard ORDER BY id DESC")
    abstract fun getAllTransactionsIncludeResponse(endWildCard: String?, doubleWildCard: String?): DataSource.Factory<Int, PersistentHttpTransaction>?

    @Query("SELECT id, method, url, path, host, scheme, request_date, error, response_code, took_ms, request_content_length, response_content_length, request_body_is_plain_text, response_body_is_plain_text FROM HttpTransaction WHERE protocol LIKE :endWildCard OR method LIKE :endWildCard OR url LIKE :doubleWildCard OR request_body LIKE :doubleWildCard OR response_code LIKE :endWildCard ORDER BY id DESC")
    abstract fun getAllTransactionsIncludeRequest(endWildCard: String?, doubleWildCard: String?): DataSource.Factory<Int, PersistentHttpTransaction>?

    @Query("SELECT id, method, url, path, host, scheme, request_date, error, response_code, took_ms, request_content_length, response_content_length, request_body_is_plain_text, response_body_is_plain_text FROM HttpTransaction WHERE protocol LIKE :endWildCard OR method LIKE :endWildCard OR url LIKE :doubleWildCard OR response_code LIKE :endWildCard ORDER BY id DESC")
    abstract fun getAllTransactions(endWildCard: String?, doubleWildCard: String?): DataSource.Factory<Int, PersistentHttpTransaction>?

}

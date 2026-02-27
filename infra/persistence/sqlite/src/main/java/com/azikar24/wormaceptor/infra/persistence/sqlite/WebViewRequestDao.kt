package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Data access object for WebView intercepted request persistence. */
@Dao
interface WebViewRequestDao {
    /** Inserts or updates a WebView request record. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(request: WebViewRequestEntity)

    /** Observes all WebView request records ordered by newest first. */
    @Query("SELECT * FROM webview_requests ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<WebViewRequestEntity>>

    /** Deletes all WebView request records. */
    @Query("DELETE FROM webview_requests")
    suspend fun deleteAll()

    /** Deletes all requests belonging to a specific WebView instance. */
    @Query("DELETE FROM webview_requests WHERE webViewId = :webViewId")
    suspend fun deleteByWebViewId(webViewId: String)

    /** Trims storage by deleting the oldest requests, keeping only [keepCount] most recent. */
    @Query(
        "DELETE FROM webview_requests WHERE id NOT IN " +
            "(SELECT id FROM webview_requests ORDER BY timestamp DESC LIMIT :keepCount)",
    )
    suspend fun deleteOldest(keepCount: Int)
}

package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WebViewRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(request: WebViewRequestEntity)

    @Query("SELECT * FROM webview_requests ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<WebViewRequestEntity>>

    @Query("DELETE FROM webview_requests")
    suspend fun deleteAll()

    @Query("DELETE FROM webview_requests WHERE webViewId = :webViewId")
    suspend fun deleteByWebViewId(webViewId: String)

    @Query(
        "DELETE FROM webview_requests WHERE id NOT IN " +
            "(SELECT id FROM webview_requests ORDER BY timestamp DESC LIMIT :keepCount)",
    )
    suspend fun deleteOldest(keepCount: Int)
}

package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for persisting WebView network requests.
 */
interface WebViewMonitorRepository {
    /** Saves a new request. */
    suspend fun saveRequest(request: WebViewRequest)

    /** Updates an existing request with response data. */
    suspend fun updateRequest(request: WebViewRequest)

    /** Deletes all stored requests. */
    suspend fun clearRequests()

    /** Deletes all requests originating from the given [webViewId]. */
    suspend fun clearRequestsForWebView(webViewId: String)

    /** Returns a [Flow] that emits the current list of requests ordered by timestamp descending. */
    fun observeRequests(): Flow<List<WebViewRequest>>

    /** Deletes the oldest requests, keeping at most [keepCount] newest entries. */
    suspend fun deleteOldest(keepCount: Int)
}

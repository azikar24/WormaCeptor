package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.WebViewMonitorRepository
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [WebViewMonitorRepository] implementation for non-persistent debug builds. */
class InMemoryWebViewMonitorRepository : WebViewMonitorRepository {

    private val _requestsFlow = MutableStateFlow<Map<String, WebViewRequest>>(emptyMap())

    override suspend fun saveRequest(request: WebViewRequest) {
        _requestsFlow.update { current -> current + (request.id to request) }
    }

    override suspend fun updateRequest(request: WebViewRequest) {
        _requestsFlow.update { current -> current + (request.id to request) }
    }

    override suspend fun clearRequests() {
        _requestsFlow.value = emptyMap()
    }

    override suspend fun clearRequestsForWebView(webViewId: String) {
        _requestsFlow.update { current ->
            current.filterValues { it.webViewId != webViewId }
        }
    }

    override fun observeRequests(): Flow<List<WebViewRequest>> {
        return _requestsFlow.map { map ->
            map.values.sortedByDescending { it.timestamp }
        }
    }

    override suspend fun deleteOldest(keepCount: Int) {
        _requestsFlow.update { current ->
            if (current.size <= keepCount) {
                current
            } else {
                val toKeep = current.values.sortedByDescending { it.timestamp }.take(keepCount)
                toKeep.associateBy { it.id }
            }
        }
    }
}

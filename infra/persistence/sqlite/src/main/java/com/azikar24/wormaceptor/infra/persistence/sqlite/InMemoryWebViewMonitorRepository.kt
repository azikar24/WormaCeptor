package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.WebViewMonitorRepository
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

class InMemoryWebViewMonitorRepository : WebViewMonitorRepository {

    private val requests = ConcurrentHashMap<String, WebViewRequest>()
    private val _requestsFlow = MutableStateFlow<List<WebViewRequest>>(emptyList())

    override suspend fun saveRequest(request: WebViewRequest) {
        requests[request.id] = request
        emitSnapshot()
    }

    override suspend fun updateRequest(request: WebViewRequest) {
        requests[request.id] = request
        emitSnapshot()
    }

    override suspend fun clearRequests() {
        requests.clear()
        _requestsFlow.value = emptyList()
    }

    override suspend fun clearRequestsForWebView(webViewId: String) {
        requests.entries.removeIf { it.value.webViewId == webViewId }
        emitSnapshot()
    }

    override fun observeRequests(): Flow<List<WebViewRequest>> {
        return _requestsFlow.map { it }
    }

    override suspend fun deleteOldest(keepCount: Int) {
        if (requests.size <= keepCount) return
        val toKeep = requests.values.sortedByDescending { it.timestamp }.take(keepCount)
        requests.clear()
        toKeep.forEach { requests[it.id] = it }
        emitSnapshot()
    }

    private fun emitSnapshot() {
        _requestsFlow.value = requests.values.sortedByDescending { it.timestamp }
    }
}

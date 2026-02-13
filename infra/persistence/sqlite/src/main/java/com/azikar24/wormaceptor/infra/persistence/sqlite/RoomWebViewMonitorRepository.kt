package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.WebViewMonitorRepository
import com.azikar24.wormaceptor.domain.entities.WebViewRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomWebViewMonitorRepository(
    private val dao: WebViewRequestDao,
) : WebViewMonitorRepository {

    override suspend fun saveRequest(request: WebViewRequest) {
        dao.insertOrUpdate(WebViewRequestEntity.fromDomain(request))
    }

    override suspend fun updateRequest(request: WebViewRequest) {
        dao.insertOrUpdate(WebViewRequestEntity.fromDomain(request))
    }

    override suspend fun clearRequests() {
        dao.deleteAll()
    }

    override suspend fun clearRequestsForWebView(webViewId: String) {
        dao.deleteByWebViewId(webViewId)
    }

    override fun observeRequests(): Flow<List<WebViewRequest>> {
        return dao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deleteOldest(keepCount: Int) {
        dao.deleteOldest(keepCount)
    }
}

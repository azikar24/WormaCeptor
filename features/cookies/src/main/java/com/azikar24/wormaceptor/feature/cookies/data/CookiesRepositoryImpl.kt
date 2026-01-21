/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.cookies.data

import com.azikar24.wormaceptor.domain.contracts.CookiesRepository
import com.azikar24.wormaceptor.domain.entities.CookieInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/**
 * Flow-based implementation of CookiesRepository.
 * Uses polling to detect cookie changes since Android doesn't provide
 * a listener API for cookie changes.
 */
class CookiesRepositoryImpl(
    private val dataSource: CookiesDataSource,
) : CookiesRepository {

    companion object {
        private const val POLL_INTERVAL_MS = 3000L
    }

    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    override fun getAllCookies(): Flow<List<CookieInfo>> = merge(
        flow {
            while (true) {
                emit(dataSource.getAllCookies())
                delay(POLL_INTERVAL_MS)
            }
        },
        refreshTrigger.onStart { emit(Unit) }.let {
            flow {
                it.collect {
                    emit(dataSource.getAllCookies())
                }
            }
        }
    ).flowOn(Dispatchers.IO)

    override fun getCookiesForDomain(domain: String): Flow<List<CookieInfo>> = merge(
        flow {
            while (true) {
                emit(dataSource.getCookiesForDomain(domain))
                delay(POLL_INTERVAL_MS)
            }
        },
        refreshTrigger.onStart { emit(Unit) }.let {
            flow {
                it.collect {
                    emit(dataSource.getCookiesForDomain(domain))
                }
            }
        }
    ).flowOn(Dispatchers.IO)

    override suspend fun deleteCookie(domain: String, name: String) =
        withContext(Dispatchers.IO) {
            dataSource.deleteCookie(domain, name)
            refreshTrigger.emit(Unit)
        }

    override suspend fun deleteAllCookiesForDomain(domain: String) =
        withContext(Dispatchers.IO) {
            dataSource.deleteAllCookiesForDomain(domain)
            refreshTrigger.emit(Unit)
        }

    override suspend fun clearAllCookies() =
        withContext(Dispatchers.IO) {
            dataSource.clearAllCookies()
            refreshTrigger.emit(Unit)
        }

    override suspend fun refresh() {
        refreshTrigger.emit(Unit)
    }
}

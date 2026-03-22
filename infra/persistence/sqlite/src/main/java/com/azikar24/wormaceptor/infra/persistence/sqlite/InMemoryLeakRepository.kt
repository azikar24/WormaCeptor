package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.LeakRepository
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [LeakRepository] implementation for non-persistent debug builds. */
class InMemoryLeakRepository : LeakRepository {
    private val _leaksFlow = MutableStateFlow<List<LeakInfo>>(emptyList())

    override suspend fun saveLeak(leak: LeakInfo) {
        _leaksFlow.update { current -> current + leak }
    }

    override suspend fun clearLeaks() {
        _leaksFlow.value = emptyList()
    }

    override fun observeLeaks(): Flow<List<LeakInfo>> {
        return _leaksFlow.map { it.reversed() }
    }
}

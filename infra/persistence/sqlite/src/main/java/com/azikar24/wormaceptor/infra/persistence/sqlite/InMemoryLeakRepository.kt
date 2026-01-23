package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.LeakRepository
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentLinkedQueue

class InMemoryLeakRepository : LeakRepository {
    private val leaks = ConcurrentLinkedQueue<LeakInfo>()
    private val _leaksFlow = MutableStateFlow<List<LeakInfo>>(emptyList())

    override suspend fun saveLeak(leak: LeakInfo) {
        leaks.add(leak)
        _leaksFlow.value = leaks.toList()
    }

    override suspend fun clearLeaks() {
        leaks.clear()
        _leaksFlow.value = emptyList()
    }

    override fun observeLeaks(): Flow<List<LeakInfo>> {
        return _leaksFlow.map { it.reversed() }
    }
}

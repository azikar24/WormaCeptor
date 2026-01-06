package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.CrashRepository
import com.azikar24.wormaceptor.domain.entities.Crash
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentLinkedQueue

class InMemoryCrashRepository : CrashRepository {
    private val crashes = ConcurrentLinkedQueue<Crash>()
    private val _crashesFlow = MutableStateFlow<List<Crash>>(emptyList())

    override suspend fun saveCrash(crash: Crash) {
        crashes.add(crash)
        _crashesFlow.value = crashes.toList()
    }

    override suspend fun clearCrashes() {
        crashes.clear()
        _crashesFlow.value = emptyList()
    }

    override fun observeCrashes(): Flow<List<Crash>> {
        return _crashesFlow.map { it.reversed() }
    }
}

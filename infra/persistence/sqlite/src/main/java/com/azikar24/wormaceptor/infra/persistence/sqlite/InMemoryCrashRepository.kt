package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.CrashRepository
import com.azikar24.wormaceptor.domain.entities.Crash
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [CrashRepository] implementation for non-persistent debug builds. */
class InMemoryCrashRepository : CrashRepository {
    private val _crashesFlow = MutableStateFlow<List<Crash>>(emptyList())

    override suspend fun saveCrash(crash: Crash) {
        _crashesFlow.update { current -> current + crash }
    }

    override suspend fun clearCrashes() {
        _crashesFlow.value = emptyList()
    }

    override fun observeCrashes(): Flow<List<Crash>> {
        return _crashesFlow.map { it.reversed() }
    }
}

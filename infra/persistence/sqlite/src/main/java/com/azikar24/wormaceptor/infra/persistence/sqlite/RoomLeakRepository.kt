package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.LeakRepository
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomLeakRepository(
    private val dao: LeakDao,
) : LeakRepository {
    override suspend fun saveLeak(leak: LeakInfo) {
        dao.insert(LeakEntity.fromDomain(leak))
    }

    override suspend fun clearLeaks() {
        dao.deleteAll()
    }

    override fun observeLeaks(): Flow<List<LeakInfo>> {
        return dao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }
}

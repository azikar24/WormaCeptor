package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.CrashRepository
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.infra.persistence.sqlite.CrashDao
import com.azikar24.wormaceptor.infra.persistence.sqlite.CrashEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCrashRepository(
    private val dao: CrashDao
) : CrashRepository {
    override suspend fun saveCrash(crash: Crash) {
        dao.insert(CrashEntity.fromDomain(crash))
    }

    override suspend fun clearCrashes() {
        dao.deleteAll()
    }

    override fun observeCrashes(): Flow<List<Crash>> {
        return dao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }
}

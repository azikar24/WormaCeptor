package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.MockRuleRepository
import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMockRuleRepository(
    private val dao: MockRuleDao,
) : MockRuleRepository {

    override fun getAll(): Flow<List<MockRule>> {
        return dao.observeAll().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getById(id: String): MockRule? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun insert(rule: MockRule) {
        dao.upsert(MockRuleEntity.fromDomain(rule))
    }

    override suspend fun update(rule: MockRule) {
        dao.upsert(MockRuleEntity.fromDomain(rule))
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }
}

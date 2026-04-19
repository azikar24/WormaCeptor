package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.contracts.MockRuleRepository
import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of [MockRuleRepository].
 *
 * Stores mock rules in a thread-safe map and exposes changes via [Flow].
 * Room-based persistence can replace this implementation later without
 * affecting consumers.
 */
class InMemoryMockRuleRepository : MockRuleRepository {

    private val store = ConcurrentHashMap<String, MockRule>()
    private val _flow = MutableStateFlow<List<MockRule>>(emptyList())

    override fun getAll(): Flow<List<MockRule>> = _flow

    override suspend fun getById(id: String): MockRule? = store[id]

    override suspend fun insert(rule: MockRule) = upsert(rule)

    override suspend fun update(rule: MockRule) = upsert(rule)

    private fun upsert(rule: MockRule) {
        store[rule.id] = rule
        emitSnapshot()
    }

    override suspend fun delete(id: String) {
        store.remove(id)
        emitSnapshot()
    }

    override suspend fun deleteAll() {
        store.clear()
        emitSnapshot()
    }

    private fun emitSnapshot() {
        _flow.value = store.values.sortedByDescending { it.createdAt }.toList()
    }
}

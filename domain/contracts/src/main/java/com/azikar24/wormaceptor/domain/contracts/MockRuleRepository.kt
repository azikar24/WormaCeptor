package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.mock.MockRule
import kotlinx.coroutines.flow.Flow

/** Repository contract for CRUD operations on mock rules. */
interface MockRuleRepository {

    /** Emits the current list of all mock rules, updating when the data changes. */
    fun getAll(): Flow<List<MockRule>>

    /** Returns the mock rule with the given [id], or null if not found. */
    suspend fun getById(id: String): MockRule?

    /** Persists a new mock rule. */
    suspend fun insert(rule: MockRule)

    /** Updates an existing mock rule. */
    suspend fun update(rule: MockRule)

    /** Deletes the mock rule with the given [id]. */
    suspend fun delete(id: String)

    /** Deletes all mock rules. */
    suspend fun deleteAll()
}

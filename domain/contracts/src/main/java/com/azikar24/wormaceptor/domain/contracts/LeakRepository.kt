package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.LeakInfo
import kotlinx.coroutines.flow.Flow

/** Repository contract for storing and observing detected memory leaks. */
interface LeakRepository {
    /** Persists a [LeakInfo] record for later inspection. */
    suspend fun saveLeak(leak: LeakInfo)

    /** Deletes all stored leak records. */
    suspend fun clearLeaks()

    /** Emits the current list of leaks, updating whenever the underlying data changes. */
    fun observeLeaks(): Flow<List<LeakInfo>>
}

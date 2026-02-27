package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.Crash

/** Repository contract for storing and observing captured application crashes. */
interface CrashRepository {
    /** Persists a [Crash] record for later inspection. */
    suspend fun saveCrash(crash: Crash)

    /** Deletes all stored crash records. */
    suspend fun clearCrashes()

    /** Emits the current list of crashes, updating whenever the underlying data changes. */
    fun observeCrashes(): kotlinx.coroutines.flow.Flow<List<Crash>>
}

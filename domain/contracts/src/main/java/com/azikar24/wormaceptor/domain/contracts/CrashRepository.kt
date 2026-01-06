package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.Crash

interface CrashRepository {
    suspend fun saveCrash(crash: Crash)
    suspend fun clearCrashes()
    fun observeCrashes(): kotlinx.coroutines.flow.Flow<List<Crash>>
}

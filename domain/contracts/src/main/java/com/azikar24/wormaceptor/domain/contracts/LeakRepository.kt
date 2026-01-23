package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.LeakInfo
import kotlinx.coroutines.flow.Flow

interface LeakRepository {
    suspend fun saveLeak(leak: LeakInfo)
    suspend fun clearLeaks()
    fun observeLeaks(): Flow<List<LeakInfo>>
}

package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azikar24.wormaceptor.domain.entities.Crash

@Entity(tableName = "crashes")
data class CrashEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val exceptionType: String,
    val message: String?,
    val stackTrace: String
) {
    fun toDomain() = Crash(id, timestamp, exceptionType, message, stackTrace)
    
    companion object {
        fun fromDomain(crash: Crash) = CrashEntity(
            id = crash.id,
            timestamp = crash.timestamp,
            exceptionType = crash.exceptionType,
            message = crash.message,
            stackTrace = crash.stackTrace
        )
    }
}

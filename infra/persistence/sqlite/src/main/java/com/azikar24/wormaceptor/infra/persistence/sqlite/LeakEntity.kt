package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity

@Entity(tableName = "leaks")
data class LeakEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val objectClass: String,
    val leakDescription: String,
    val retainedSize: Long,
    val referencePath: List<String>,
    val severity: String,
) {
    fun toDomain() = LeakInfo(
        timestamp = timestamp,
        objectClass = objectClass,
        leakDescription = leakDescription,
        retainedSize = retainedSize,
        referencePath = referencePath,
        severity = LeakSeverity.valueOf(severity),
    )

    companion object {
        fun fromDomain(leak: LeakInfo) = LeakEntity(
            timestamp = leak.timestamp,
            objectClass = leak.objectClass,
            leakDescription = leak.leakDescription,
            retainedSize = leak.retainedSize,
            referencePath = leak.referencePath,
            severity = leak.severity.name,
        )
    }
}

package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity

/**
 * Room entity representing a detected memory leak.
 *
 * @property id Auto-generated primary key for the leak row.
 * @property timestamp Epoch millis when the leak was detected.
 * @property objectClass Fully-qualified class name of the leaked object.
 * @property leakDescription Human-readable description of the leak.
 * @property retainedSize Estimated bytes retained by the leaked object.
 * @property referencePath GC root reference chain leading to the leak.
 * @property severity Stringified [LeakSeverity] level of the leak.
 */
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
    /** Converts this entity to a domain [LeakInfo] model. */
    fun toDomain() = LeakInfo(
        timestamp = timestamp,
        objectClass = objectClass,
        leakDescription = leakDescription,
        retainedSize = retainedSize,
        referencePath = referencePath,
        severity = LeakSeverity.valueOf(severity),
    )

    /** Domain-entity conversion factory. */
    companion object {
        /** Creates a [LeakEntity] from a domain [LeakInfo] model. */
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

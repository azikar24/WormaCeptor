package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.azikar24.wormaceptor.domain.entities.Crash

/**
 * Room entity representing a captured application crash.
 *
 * @property id Auto-generated primary key for the crash row.
 * @property timestamp Epoch millis when the crash occurred.
 * @property exceptionType Fully-qualified class name of the thrown exception.
 * @property message Optional human-readable message from the exception.
 * @property stackTrace Full stack trace captured at crash time.
 */
@Entity(tableName = "crashes")
data class CrashEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val exceptionType: String,
    val message: String?,
    val stackTrace: String,
) {
    /** Converts this entity to a domain [Crash] model. */
    fun toDomain() = Crash(id, timestamp, exceptionType, message, stackTrace)

    /** Domain-entity conversion factory. */
    companion object {
        /** Creates a [CrashEntity] from a domain [Crash] model. */
        fun fromDomain(crash: Crash) = CrashEntity(
            id = crash.id,
            timestamp = crash.timestamp,
            exceptionType = crash.exceptionType,
            message = crash.message,
            stackTrace = crash.stackTrace,
        )
    }
}

package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.TypeConverter
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date
import java.util.UUID

/** Room type converters for complex types stored in the WormaCeptor database. */
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    /** Converts a Unix timestamp to a [Date]. */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    /** Converts a [Date] to a Unix timestamp. */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    /** Converts a [TransactionStatus] to its string name for storage. */
    @TypeConverter
    fun fromStatus(status: TransactionStatus): String = status.name

    /** Converts a stored string back to a [TransactionStatus]. */
    @TypeConverter
    fun toStatus(value: String): TransactionStatus =
        TransactionStatus.entries.find { it.name == value } ?: TransactionStatus.FAILED

    /** Converts a [UUID] to its string representation. */
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    /** Converts a stored string back to a [UUID]. */
    @TypeConverter
    fun toUUID(value: String?): UUID? = value?.let { UUID.fromString(it) }

    /** Serializes HTTP headers to JSON for storage. */
    @TypeConverter
    fun fromHeaders(headers: Map<String, List<String>>?): String? {
        return headers?.let { json.encodeToString(it) }
    }

    /** Deserializes JSON back to HTTP headers. */
    @TypeConverter
    fun toHeaders(value: String?): Map<String, List<String>>? {
        return value?.let { json.decodeFromString(it) }
    }

    /** Serializes an extension metadata map to JSON. */
    @TypeConverter
    fun fromExtensions(ext: Map<String, String>?): String? = ext?.let { json.encodeToString(it) }

    /** Deserializes JSON back to an extension metadata map. */
    @TypeConverter
    fun toExtensions(value: String?): Map<String, String>? = value?.let { json.decodeFromString(it) }

    /** Serializes a string list to JSON. */
    @TypeConverter
    fun fromStringList(list: List<String>?): String? = list?.let { json.encodeToString(it) }

    /** Deserializes JSON back to a string list. */
    @TypeConverter
    fun toStringList(value: String?): List<String>? = value?.let { json.decodeFromString(it) }
}

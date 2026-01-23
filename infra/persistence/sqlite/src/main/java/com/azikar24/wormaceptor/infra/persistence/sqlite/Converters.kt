package com.azikar24.wormaceptor.infra.persistence.sqlite

import androidx.room.TypeConverter
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date
import java.util.UUID

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromStatus(status: TransactionStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): TransactionStatus = TransactionStatus.valueOf(value)

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUUID(value: String?): UUID? = value?.let { UUID.fromString(it) }

    @TypeConverter
    fun fromHeaders(headers: Map<String, List<String>>?): String? {
        return headers?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toHeaders(value: String?): Map<String, List<String>>? {
        return value?.let { json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromExtensions(ext: Map<String, String>?): String? = ext?.let { json.encodeToString(it) }

    @TypeConverter
    fun toExtensions(value: String?): Map<String, String>? = value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? = list?.let { json.encodeToString(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? = value?.let { json.decodeFromString(it) }
}

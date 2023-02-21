/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "StackTraceTransaction")
class PersistentStackTraceTransaction {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "stack_trace")
    var stackTrace: List<StackTraceElement>? = null

    @ColumnInfo(name = "response_date")
    var stackTraceDate: Date? = null

    @ColumnInfo(name = "throwable")
    var throwable: String? = null
}

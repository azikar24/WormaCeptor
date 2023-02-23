/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity(tableName = "CrashTransaction")
class PersistentCrashTransaction {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "crash_list")
    var crashList: List<StackTraceElement>? = null

    @ColumnInfo(name = "response_date")
    var crashDate: Date? = null

    @ColumnInfo(name = "throwable")
    var throwable: String? = null
}

/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.data

import android.os.Parcelable
import java.util.*
import kotlinx.parcelize.Parcelize

@Parcelize
data class CrashTransaction(
    var id: Long,
    var crashList: List<StackTraceElement>?,
    var crashDate: Date?,
    var throwable: String?,
) : Parcelable {

    @Parcelize
    class Builder : Parcelable {
        //region START Variables
        var id: Long = 0
        var crashList: List<StackTraceElement>? = null
        var crashDate: Date? = null
        var throwable: String? = null

        //region START Builder Setters
        fun setId(value: Long) = this.apply { id = value }
        fun setCrashList(value: List<StackTraceElement>) = this.apply { crashList = value }
        fun setCrashDate(value: Date) = this.apply { crashDate = value }
        fun setThrowable(value: String) = this.apply { throwable = value }
        //endregion END Builder Setters

        fun build(): CrashTransaction {
            return CrashTransaction(
                id,
                crashList,
                crashDate,
                throwable
            )
        }
    }
}
/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class StackTraceTransaction(
    var id: Long,
    var stackTrace: List<StackTraceElement>?,
    var stackTraceDate: Date?,
    var throwable: String?,
) : Parcelable {

    @Parcelize
    class Builder : Parcelable {
        //region START Variables
        var id: Long = 0
        var stackTrace: List<StackTraceElement>? = null
        var stackTraceDate: Date? = null
        var throwable: String? = null

        //region START Builder Setters
        fun setId(value: Long) = this.apply { id = value }
        fun setStackTrace(value: List<StackTraceElement>) = this.apply { stackTrace = value }
        fun setStackTraceDate(value: Date) = this.apply { stackTraceDate = value }
        fun setThrowable(value: String) = this.apply { throwable = value }
        //endregion END Builder Setters

        fun build(): StackTraceTransaction {
            return StackTraceTransaction(
                id,
                stackTrace,
                stackTraceDate,
                throwable
            )
        }
    }
}
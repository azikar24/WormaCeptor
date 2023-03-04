/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.data

import android.content.Context
import android.os.Parcelable
import com.azikar24.wormaceptor.R
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class CrashTransaction(
    var id: Long,
    var crashList: List<StackTraceElement>?,
    var crashDate: Date?,
    var throwable: String?,
) : Parcelable {
    fun getClassNameAndLineNumber(): String? {
        return crashList?.firstOrNull()?.let { "${it.className} (${it.lineNumber})" }
    }

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

fun StackTraceElement.stacktraceData(context: Context): String {
    return context.getString(
        R.string.stack_trace_string,
        className,
        methodName,
        fileName,
        lineNumber
    )
}
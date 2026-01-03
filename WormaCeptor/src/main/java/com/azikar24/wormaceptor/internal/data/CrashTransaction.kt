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
    var id: Long = 0,
    var crashList: List<StackTraceElement>?,
    var crashDate: Date?,
    var throwable: String?,
) : Parcelable {
    fun getClassNameAndLineNumber(): String? {
        return crashList?.firstOrNull()?.let { "${it.className} (${it.lineNumber})" }
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
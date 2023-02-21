/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*


@Parcelize
class StackTraceTransaction(private val builder: Builder) : Parcelable {
    //region START Variables
    var id: Long = builder.id

    var stackTrace = builder.stackTrace

    var stackTraceDate = builder.stackTraceDate

    //endregion END Variables

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as StackTraceTransaction
        if (id != that.id) return false
        if (stackTrace != that.stackTrace) return false
        if (stackTraceDate != that.stackTraceDate) return false
        return true
    }

    override fun hashCode(): Int {
        var result = (id xor (id ushr 32)).toInt()
        result = 31 * result + if (stackTrace != null) stackTrace.hashCode() else 0
        result = 31 * result + if (stackTraceDate != null) stackTraceDate.hashCode() else 0
        return result
    }

    override fun toString(): String {
        return "StackTraceTransaction(builder=$builder, id=$id, stackTrace=$stackTrace, stackTraceDate=$stackTraceDate)"
    }

    companion object {
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    @Parcelize
    class Builder : Parcelable {
        //region START Variables
        var id: Long = 0
        var stackTrace: List<StackTraceElement>? = null
        var stackTraceDate: Date? = null

        //region START Builder Setters
        fun setId(value: Long) = this.apply { id = value }
        fun setStackTrace(value: List<StackTraceElement>) = this.apply { stackTrace = value }
        fun setStackTraceDate(value: Date) = this.apply { stackTraceDate = value }
        //endregion END Builder Setters

        fun build(): StackTraceTransaction {
            return StackTraceTransaction(this)
        }
    }
}
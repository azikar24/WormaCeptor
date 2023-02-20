/*
 * Copyright AziKar24 20/2/2023.
 */

package com.azikar24.wormaceptor_persistence

import androidx.room.TypeConverter
import com.azikar24.wormaceptor.internal.data.HttpHeader
import com.azikar24.wormaceptor.internal.support.TextUtil
import java.util.*
import kotlin.collections.ArrayList

internal object RoomTypeConverters {
    @TypeConverter
    fun fromLongToDate(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun fromDateToLong(value: Date?): Long? {
        return value?.time
    }

    private const val NAME_VALUE_SEPARATOR = "__:_:__"
    private const val LIST_SEPARATOR = "__,_,__"
    @TypeConverter
    fun fromStringToHeaderList(value: String?): List<HttpHeader> {
        if (value == null || TextUtil.isNullOrWhiteSpace(value)) {
            return java.util.ArrayList<HttpHeader>()
        }
        val nameValuePairArray = value.split(LIST_SEPARATOR).toTypedArray()
        val list: MutableList<HttpHeader> = ArrayList<HttpHeader>(nameValuePairArray.size)
        for (nameValuePair in nameValuePairArray) {
            val nameValue = nameValuePair.split(NAME_VALUE_SEPARATOR).toTypedArray()
            if (nameValue.size == 2) {
                list.add(HttpHeader(nameValue[0], nameValue[1]))
            } else if (nameValue.size == 1) {
                list.add(HttpHeader(nameValue[0], ""))
            }
        }
        return list
    }

    @TypeConverter
    fun fromHeaderListToString(value: List<HttpHeader?>?): String? {
        if (value == null || value.size == 0) {
            return null
        }
        val stringBuilder = StringBuilder()
        var isFirst = true
        for (header in value) {
            if (!isFirst) {
                stringBuilder.append(LIST_SEPARATOR)
            }
            stringBuilder
                .append(header?.name)
                .append(NAME_VALUE_SEPARATOR)
                .append(header?.value)
            isFirst = false
        }
        return stringBuilder.toString()
    }
}

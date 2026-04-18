package com.azikar24.wormaceptor.feature.preferences.vm

import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class PreferenceEditorState(
    val isEditing: Boolean = false,
    val key: String = "",
    val selectedType: String = TYPE_STRING,
    val stringValue: String = "",
    val intValue: String = "",
    val longValue: String = "",
    val floatValue: String = "",
    val booleanValue: Boolean = false,
    val stringSetValues: ImmutableList<String> = persistentListOf(),
    val newStringSetItem: String = "",
    val typeDropdownExpanded: Boolean = false,
) {

    val isCreating: Boolean get() = !isEditing

    val isKeyValid: Boolean get() = key.isNotBlank()
    val isKeyError: Boolean get() = key.isNotEmpty() && key.isBlank()

    val isIntError: Boolean get() = intValue.isNotBlank() && intValue.toIntOrNull() == null
    val isLongError: Boolean get() = longValue.isNotBlank() && longValue.toLongOrNull() == null
    val isFloatError: Boolean get() = floatValue.isNotBlank() && floatValue.toFloatOrNull() == null

    val isValueValid: Boolean
        get() = when (selectedType) {
            TYPE_INT -> !isIntError
            TYPE_LONG -> !isLongError
            TYPE_FLOAT -> !isFloatError
            else -> true
        }

    val canSave: Boolean get() = isKeyValid && isValueValid

    fun toPreferenceValue(): PreferenceValue = when (selectedType) {
        TYPE_STRING -> PreferenceValue.StringValue(stringValue)
        TYPE_INT -> PreferenceValue.IntValue(intValue.toIntOrNull() ?: 0)
        TYPE_LONG -> PreferenceValue.LongValue(longValue.toLongOrNull() ?: 0L)
        TYPE_FLOAT -> PreferenceValue.FloatValue(floatValue.toFloatOrNull() ?: 0f)
        TYPE_BOOLEAN -> PreferenceValue.BooleanValue(booleanValue)
        TYPE_STRING_SET -> PreferenceValue.StringSetValue(stringSetValues.toSet())
        else -> PreferenceValue.StringValue(stringValue)
    }

    companion object {
        const val TYPE_STRING = "String"
        const val TYPE_INT = "Int"
        const val TYPE_LONG = "Long"
        const val TYPE_FLOAT = "Float"
        const val TYPE_BOOLEAN = "Boolean"
        const val TYPE_STRING_SET = "StringSet"

        val AVAILABLE_TYPES: ImmutableList<String> = listOf(
            TYPE_STRING,
            TYPE_INT,
            TYPE_LONG,
            TYPE_FLOAT,
            TYPE_BOOLEAN,
            TYPE_STRING_SET,
        ).toImmutableList()

        fun fromItem(item: PreferenceItem?): PreferenceEditorState {
            if (item == null) return PreferenceEditorState()
            val value = item.value
            return PreferenceEditorState(
                isEditing = true,
                key = item.key,
                selectedType = value.typeName,
                stringValue = (value as? PreferenceValue.StringValue)?.value.orEmpty(),
                intValue = (value as? PreferenceValue.IntValue)?.value?.toString().orEmpty(),
                longValue = (value as? PreferenceValue.LongValue)?.value?.toString().orEmpty(),
                floatValue = (value as? PreferenceValue.FloatValue)?.value?.toString().orEmpty(),
                booleanValue = (value as? PreferenceValue.BooleanValue)?.value ?: false,
                stringSetValues = (value as? PreferenceValue.StringSetValue)?.value
                    ?.toList()
                    ?.toImmutableList()
                    ?: persistentListOf(),
            )
        }
    }
}

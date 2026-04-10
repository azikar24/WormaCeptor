package com.azikar24.wormaceptor.feature.preferences.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** Encapsulates mutable form state and validation for the preference edit sheet. */
@Stable
class PreferenceEditFormState(item: PreferenceItem?) {

    val isCreating: Boolean = item == null

    var key by mutableStateOf(item?.key.orEmpty())
    var selectedType by mutableStateOf(item?.value?.typeName ?: TYPE_STRING)

    var stringValue by mutableStateOf(
        (item?.value as? PreferenceValue.StringValue)?.value.orEmpty(),
    )
    var intValue by mutableStateOf(
        (item?.value as? PreferenceValue.IntValue)?.value?.toString().orEmpty(),
    )
    var longValue by mutableStateOf(
        (item?.value as? PreferenceValue.LongValue)?.value?.toString().orEmpty(),
    )
    var floatValue by mutableStateOf(
        (item?.value as? PreferenceValue.FloatValue)?.value?.toString().orEmpty(),
    )
    var booleanValue by mutableStateOf(
        (item?.value as? PreferenceValue.BooleanValue)?.value ?: false,
    )

    private val _stringSetValues: SnapshotStateList<String> = mutableStateListOf<String>().also { list ->
        (item?.value as? PreferenceValue.StringSetValue)?.value?.let { list.addAll(it) }
    }
    val stringSetValues: ImmutableList<String> get() = _stringSetValues.toImmutableList()

    var newStringSetItem by mutableStateOf("")
    var typeDropdownExpanded by mutableStateOf(false)

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

    fun addStringSetItem() {
        if (newStringSetItem.isNotBlank()) {
            _stringSetValues.add(newStringSetItem)
            newStringSetItem = ""
        }
    }

    fun removeStringSetItem(index: Int) {
        _stringSetValues.removeAt(index)
    }

    fun selectType(type: String) {
        selectedType = type
        typeDropdownExpanded = false
    }

    fun toPreferenceValue(): PreferenceValue = when (selectedType) {
        TYPE_STRING -> PreferenceValue.StringValue(stringValue)
        TYPE_INT -> PreferenceValue.IntValue(intValue.toIntOrNull() ?: 0)
        TYPE_LONG -> PreferenceValue.LongValue(longValue.toLongOrNull() ?: 0L)
        TYPE_FLOAT -> PreferenceValue.FloatValue(floatValue.toFloatOrNull() ?: 0f)
        TYPE_BOOLEAN -> PreferenceValue.BooleanValue(booleanValue)
        TYPE_STRING_SET -> PreferenceValue.StringSetValue(_stringSetValues.toSet())
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
    }
}

@Composable
fun rememberPreferenceEditFormState(item: PreferenceItem?): PreferenceEditFormState =
    remember(item) { PreferenceEditFormState(item) }

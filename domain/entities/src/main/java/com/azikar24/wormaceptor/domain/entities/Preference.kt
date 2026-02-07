package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a value stored in SharedPreferences with type safety.
 */
sealed class PreferenceValue {
    abstract val displayValue: String
    abstract val typeName: String

    data class StringValue(val value: String) : PreferenceValue() {
        override val displayValue: String = value
        override val typeName: String = "String"
    }

    data class IntValue(val value: Int) : PreferenceValue() {
        override val displayValue: String = value.toString()
        override val typeName: String = "Int"
    }

    data class LongValue(val value: Long) : PreferenceValue() {
        override val displayValue: String = value.toString()
        override val typeName: String = "Long"
    }

    data class FloatValue(val value: Float) : PreferenceValue() {
        override val displayValue: String = value.toString()
        override val typeName: String = "Float"
    }

    data class BooleanValue(val value: Boolean) : PreferenceValue() {
        override val displayValue: String = value.toString()
        override val typeName: String = "Boolean"
    }

    data class StringSetValue(val value: Set<String>) : PreferenceValue() {
        override val displayValue: String = value.joinToString(", ")
        override val typeName: String = "StringSet"
    }

    companion object {
        /**
         * Creates a PreferenceValue from an Any object.
         * Returns null if the type is not supported.
         */
        @Suppress("UNCHECKED_CAST")
        fun fromAny(value: Any?): PreferenceValue? = when (value) {
            is String -> StringValue(value)
            is Int -> IntValue(value)
            is Long -> LongValue(value)
            is Float -> FloatValue(value)
            is Boolean -> BooleanValue(value)
            is Set<*> -> {
                val stringSet = value.filterIsInstance<String>().toSet()
                if (stringSet.size == value.size) StringSetValue(stringSet) else null
            }
            else -> null
        }
    }
}

/**
 * Represents a single key-value pair in a SharedPreferences file.
 */
data class PreferenceItem(
    val key: String,
    val value: PreferenceValue,
)

/**
 * Represents a SharedPreferences file with its name and item count.
 */
data class PreferenceFile(
    val name: String,
    val itemCount: Int,
)

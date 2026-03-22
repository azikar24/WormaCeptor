package com.azikar24.wormaceptor.domain.entities

/**
 * Represents a value stored in SharedPreferences with type safety.
 */
sealed class PreferenceValue {
    /** Human-readable representation of the stored value. */
    abstract val displayValue: String

    /** Short type label (e.g., "String", "Int", "Boolean"). */
    abstract val typeName: String

    /** Wraps a [String] SharedPreferences value. */
    data class StringValue(
        /** The stored string. */
        val value: String,
    ) : PreferenceValue() {
        override val displayValue: String = value
        override val typeName: String = "String"
    }

    /** Wraps an [Int] SharedPreferences value. */
    data class IntValue(
        /** The stored integer. */
        val value: Int,
    ) : PreferenceValue() {
        override val displayValue: String = value.toString()
        override val typeName: String = "Int"
    }

    /** Wraps a [Long] SharedPreferences value. */
    data class LongValue(
        /** The stored long integer. */
        val value: Long,
    ) : PreferenceValue() {
        override val displayValue: String = value.toString()
        override val typeName: String = "Long"
    }

    /** Wraps a [Float] SharedPreferences value. */
    data class FloatValue(
        /** The stored float. */
        val value: Float,
    ) : PreferenceValue() {
        override val displayValue: String = value.toString()
        override val typeName: String = "Float"
    }

    /** Wraps a [Boolean] SharedPreferences value. */
    data class BooleanValue(
        /** The stored boolean. */
        val value: Boolean,
    ) : PreferenceValue() {
        override val displayValue: String = value.toString()
        override val typeName: String = "Boolean"
    }

    /** Wraps a [Set] of [String] SharedPreferences value. */
    data class StringSetValue(
        /** The stored set of strings. */
        val value: Set<String>,
    ) : PreferenceValue() {
        override val displayValue: String = value.joinToString(", ")
        override val typeName: String = "StringSet"
    }

    /** Conversion helpers for creating [PreferenceValue] from untyped data. */
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
    /** SharedPreferences key for this entry. */
    val key: String,
    /** Typed wrapper around the stored value. */
    val value: PreferenceValue,
)

/**
 * Represents a SharedPreferences file with its name and item count.
 */
data class PreferenceFile(
    /** SharedPreferences file name (without the .xml extension). */
    val name: String,
    /** Number of key-value pairs stored in this file. */
    val itemCount: Int,
)

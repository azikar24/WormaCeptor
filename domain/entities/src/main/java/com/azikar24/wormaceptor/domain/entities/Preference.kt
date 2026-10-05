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
        val value: String,
    ) : PreferenceValue() {
        override val displayValue: String = value
        override val typeName: String = "String"
    }

    /** Wraps an [Int] SharedPreferences value. */
    data class IntValue(
        val value: Int,
    ) : PreferenceValue() {
        override val displayValue: String = value.toString()
        override val typeName: String = "Int"
    }

    /** Wraps a [Long] SharedPreferences value. */
    data class LongValue(
        val value: Long,
    ) : PreferenceValue() {
        override val displayValue: String = value.toString()
        override val typeName: String = "Long"
    }

    /** Wraps a [Float] SharedPreferences value. */
    data class FloatValue(
        val value: Float,
    ) : PreferenceValue() {
        override val displayValue: String = value.toString()
        override val typeName: String = "Float"
    }

    /** Wraps a [Boolean] SharedPreferences value. */
    data class BooleanValue(
        val value: Boolean,
    ) : PreferenceValue() {
        override val displayValue: String = value.toString()
        override val typeName: String = "Boolean"
    }

    /** Wraps a [Set] of [String] SharedPreferences value. */
    data class StringSetValue(
        val value: Set<String>,
    ) : PreferenceValue() {
        override val displayValue: String = value.joinToString(", ")
        override val typeName: String = "StringSet"
    }

    /** Wraps a [Double] value. Only Preferences DataStore stores these. */
    data class DoubleValue(
        /** The stored double. */
        val value: Double,
    ) : PreferenceValue() {
        override val displayValue: String = value.toString()
        override val typeName: String = "Double"
    }

    /** Wraps a [ByteArray] value, shown as hex. Only Preferences DataStore stores these. */
    class BytesValue(
        /** The stored bytes. */
        val value: ByteArray,
    ) : PreferenceValue() {
        override val displayValue: String = toHex(value)
        override val typeName: String = "Bytes"

        override fun equals(other: Any?): Boolean = other is BytesValue && value.contentEquals(other.value)

        override fun hashCode(): Int = value.contentHashCode()

        private companion object {
            const val MAX_DISPLAY_BYTES = 64

            fun toHex(bytes: ByteArray): String {
                val hex = bytes.take(MAX_DISPLAY_BYTES).joinToString("") { "%02x".format(it) }
                return if (bytes.size > MAX_DISPLAY_BYTES) "$hex… (${bytes.size} bytes)" else hex
            }
        }
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
 * Represents a preferences file with its name and item count.
 */
data class PreferenceFile(
    /** SharedPreferences file name without the .xml extension, or the full DataStore file name. */
    val name: String,
    /** Number of key-value pairs stored in this file. */
    val itemCount: Int,
    /** Storage mechanism that owns this file. */
    val source: PreferenceSource = PreferenceSource.SHARED_PREFERENCES,
)

/** Storage mechanism behind a [PreferenceFile]. */
enum class PreferenceSource {
    /** `shared_prefs/<name>.xml`, read and written through SharedPreferences. */
    SHARED_PREFERENCES,

    /** `files/datastore/<name>.preferences_pb`, read-only because the app's DataStore caches its contents. */
    DATASTORE,
}

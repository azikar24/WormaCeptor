package com.azikar24.wormaceptor.domain.entities

/**
 * A decoded key-value pair from URL-encoded form data.
 *
 * @property key The decoded parameter name.
 * @property value The decoded parameter value.
 */
data class FormParameter(
    val key: String,
    val value: String,
)

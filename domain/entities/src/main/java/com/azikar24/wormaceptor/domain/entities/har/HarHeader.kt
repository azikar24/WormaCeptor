package com.azikar24.wormaceptor.domain.entities.har

/** A single name/value header pair. */
data class HarHeader(
    val name: String,
    val value: String,
)

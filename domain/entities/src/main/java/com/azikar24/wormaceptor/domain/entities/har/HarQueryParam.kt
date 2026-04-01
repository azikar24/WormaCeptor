package com.azikar24.wormaceptor.domain.entities.har

/** A single parsed query-string parameter. */
data class HarQueryParam(
    val name: String,
    val value: String,
)

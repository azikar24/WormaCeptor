package com.azikar24.wormaceptor.domain.entities.har

/** A parsed cookie. */
data class HarCookie(
    val name: String,
    val value: String,
    val path: String? = null,
    val domain: String? = null,
    val expires: String? = null,
    val httpOnly: Boolean? = null,
    val secure: Boolean? = null,
)

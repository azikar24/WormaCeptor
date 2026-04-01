package com.azikar24.wormaceptor.domain.entities.har

/** Timing breakdown of the request lifecycle. Values are in milliseconds; -1 means unavailable. */
data class HarTimings(
    val blocked: Long = -1,
    val dns: Long = -1,
    val connect: Long = -1,
    val ssl: Long = -1,
    val send: Long = -1,
    val wait: Long = -1,
    val receive: Long = -1,
)

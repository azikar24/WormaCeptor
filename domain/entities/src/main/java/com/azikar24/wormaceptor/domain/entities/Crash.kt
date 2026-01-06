package com.azikar24.wormaceptor.domain.entities

data class Crash(
    val id: Long = 0,
    val timestamp: Long,
    val exceptionType: String,
    val message: String?,
    val stackTrace: String
)

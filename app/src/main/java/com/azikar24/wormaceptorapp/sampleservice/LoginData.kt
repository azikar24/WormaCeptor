/*
 * Copyright AziKar24 2023.
 */

package com.azikar24.wormaceptorapp.sampleservice

/**
 * Sample login request data for testing body redaction.
 */
data class LoginData(
    val username: String,
    val password: String,
    val rememberMe: Boolean = false,
)

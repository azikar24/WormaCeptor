/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.entities

/**
 * Represents an HTTP cookie with all its attributes.
 */
data class CookieInfo(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val expiresAt: Long?,
    val isSecure: Boolean,
    val isHttpOnly: Boolean,
    val sameSite: SameSite?,
) {
    /**
     * Returns true if the cookie has expired.
     */
    val isExpired: Boolean
        get() = expiresAt != null && expiresAt < System.currentTimeMillis()

    /**
     * Returns true if this is a session cookie (no expiration).
     */
    val isSessionCookie: Boolean
        get() = expiresAt == null

    /**
     * Returns a display string for the expiration status.
     */
    val expirationStatus: String
        get() = when {
            isSessionCookie -> "Session"
            isExpired -> "Expired"
            else -> "Valid"
        }

    /**
     * SameSite cookie attribute values.
     */
    enum class SameSite {
        STRICT,
        LAX,
        NONE;

        companion object {
            fun fromString(value: String?): SameSite? = when (value?.uppercase()) {
                "STRICT" -> STRICT
                "LAX" -> LAX
                "NONE" -> NONE
                else -> null
            }
        }
    }
}

/**
 * Represents a group of cookies for a specific domain.
 */
data class CookieDomain(
    val domain: String,
    val cookies: List<CookieInfo>,
) {
    val cookieCount: Int get() = cookies.size
}

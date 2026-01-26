/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.cookies.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Feature-specific design tokens for the Cookies Manager feature.
 * Uses WormaCeptorDesignSystem from core:ui for shared tokens (Spacing, CornerRadius, BorderWidth, Alpha).
 */
object CookiesDesignSystem {

    /**
     * Colors for cookie attributes and states.
     */
    object CookieColors {
        val secure = Color(0xFF4CAF50) // Green for secure cookies
        val httpOnly = Color(0xFF2196F3) // Blue for HTTP-only cookies
        val session = Color(0xFF9C27B0) // Purple for session cookies
        val expired = Color(0xFFF44336) // Red for expired cookies
        val valid = Color(0xFF4CAF50) // Green for valid cookies
        val domain = Color(0xFFFF9800) // Orange for domain indicator
        val sameSiteStrict = Color(0xFF3F51B5) // Indigo for strict
        val sameSiteLax = Color(0xFF00BCD4) // Cyan for lax
        val sameSiteNone = Color(0xFFFF5722) // Deep orange for none

        fun forExpirationStatus(status: String): Color = when (status) {
            "Expired" -> expired
            "Valid" -> valid
            "Session" -> session
            else -> Color.Gray
        }
    }
}

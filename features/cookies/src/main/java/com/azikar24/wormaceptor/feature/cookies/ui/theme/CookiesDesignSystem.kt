/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.cookies.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Design system for the Cookies Manager feature.
 * Aligned with WormaCeptorDesignSystem from viewer module.
 */
object CookiesDesignSystem {

    object Spacing {
        val xxs = 2.dp
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val xxl = 32.dp
    }

    object CornerRadius {
        val xs = 4.dp
        val sm = 6.dp
        val md = 8.dp
        val lg = 12.dp
        val xl = 16.dp
        val pill = 999.dp
    }

    object BorderWidth {
        val thin = 0.5.dp
        val regular = 1.dp
        val thick = 2.dp
    }

    object Alpha {
        const val subtle = 0.08f
        const val light = 0.12f
        const val medium = 0.20f
        const val strong = 0.40f
    }

    object Shapes {
        val card = RoundedCornerShape(CornerRadius.md)
        val chip = RoundedCornerShape(CornerRadius.xs)
        val button = RoundedCornerShape(CornerRadius.sm)
    }

    /**
     * Colors for cookie attributes and states.
     */
    object CookieColors {
        val secure = Color(0xFF4CAF50)          // Green for secure cookies
        val httpOnly = Color(0xFF2196F3)        // Blue for HTTP-only cookies
        val session = Color(0xFF9C27B0)         // Purple for session cookies
        val expired = Color(0xFFF44336)         // Red for expired cookies
        val valid = Color(0xFF4CAF50)           // Green for valid cookies
        val domain = Color(0xFFFF9800)          // Orange for domain indicator
        val sameSiteStrict = Color(0xFF3F51B5)  // Indigo for strict
        val sameSiteLax = Color(0xFF00BCD4)     // Cyan for lax
        val sameSiteNone = Color(0xFFFF5722)    // Deep orange for none

        fun forExpirationStatus(status: String): Color = when (status) {
            "Expired" -> expired
            "Valid" -> valid
            "Session" -> session
            else -> Color.Gray
        }
    }
}

fun Color.asSubtleBackground(): Color = this.copy(alpha = CookiesDesignSystem.Alpha.subtle)

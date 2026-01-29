/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.cookies.ui.theme

import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors

/**
 * Cookies feature colors - delegates to centralized colors.
 * @see WormaCeptorColors.Cookies
 */
object CookiesDesignSystem {
    object CookieColors {
        val secure = WormaCeptorColors.Cookies.Secure
        val httpOnly = WormaCeptorColors.Cookies.HttpOnly
        val session = WormaCeptorColors.Cookies.Session
        val expired = WormaCeptorColors.Cookies.Expired
        val valid = WormaCeptorColors.Cookies.Valid
        val domain = WormaCeptorColors.Cookies.Domain
        val sameSiteStrict = WormaCeptorColors.Cookies.SameSiteStrict
        val sameSiteLax = WormaCeptorColors.Cookies.SameSiteLax
        val sameSiteNone = WormaCeptorColors.Cookies.SameSiteNone

        fun forExpirationStatus(status: String): Color = when (status) {
            "Expired" -> expired
            "Valid" -> valid
            "Session" -> session
            else -> Color.Gray
        }
    }
}

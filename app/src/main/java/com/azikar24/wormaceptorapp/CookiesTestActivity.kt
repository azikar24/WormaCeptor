/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptorapp

import android.os.Bundle
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.azikar24.wormaceptor.feature.cookies.CookiesInspector
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme

/**
 * Test activity for the Cookies Manager feature.
 * Sets up some test cookies for demonstration.
 */
class CookiesTestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set up test cookies
        setupTestCookies()

        setContent {
            WormaCeptorMainTheme {
                Scaffold { padding ->
                    CookiesInspector(
                        context = this,
                        onNavigateBack = { finish() },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                    )
                }
            }
        }
    }

    private fun setupTestCookies() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

        // Test cookies for different domains
        val testCookies = listOf(
            // Session cookies
            "https://example.com" to "session_id=abc123; Path=/",
            "https://example.com" to "user_pref=dark_mode; Path=/; Secure",

            // Authentication cookies
            "https://api.example.com" to "auth_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9; Path=/; Secure; HttpOnly",
            "https://api.example.com" to "refresh_token=xyz789; Path=/api/auth; Secure; HttpOnly",

            // Analytics cookies
            "https://analytics.example.com" to "_ga=GA1.2.123456789.1234567890; Path=/; Max-Age=63072000",
            "https://analytics.example.com" to "_gid=GA1.2.987654321.1234567890; Path=/; Max-Age=86400",

            // Preferences cookies
            "https://shop.example.com" to "cart_id=cart_12345; Path=/",
            "https://shop.example.com" to "currency=USD; Path=/",
            "https://shop.example.com" to "language=en; Path=/; Secure",

            // Third-party cookies
            "https://cdn.example.com" to "cache_version=v2; Path=/; Max-Age=31536000",
        )

        testCookies.forEach { (url, cookie) ->
            cookieManager.setCookie(url, cookie)
        }

        cookieManager.flush()
    }
}

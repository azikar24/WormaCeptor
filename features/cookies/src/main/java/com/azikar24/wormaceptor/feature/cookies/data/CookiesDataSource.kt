/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.cookies.data

import android.content.Context
import android.os.Build
import android.webkit.CookieManager
import com.azikar24.wormaceptor.domain.entities.CookieInfo
import java.net.CookieHandler
import java.net.CookieStore
import java.net.HttpCookie

/**
 * Data source for reading and managing HTTP cookies.
 * Supports both Android WebView CookieManager and Java CookieHandler.
 */
class CookiesDataSource(private val context: Context) {

    private val webViewCookieManager: CookieManager?
        get() = try {
            CookieManager.getInstance()
        } catch (_: Exception) {
            null
        }

    private val javaCookieStore: CookieStore?
        get() = try {
            (CookieHandler.getDefault() as? java.net.CookieManager)?.cookieStore
        } catch (_: Exception) {
            null
        }

    /**
     * Gets all cookies from all available sources.
     */
    fun getAllCookies(): List<CookieInfo> {
        val cookies = mutableListOf<CookieInfo>()

        // Get cookies from Java CookieStore (OkHttp compatible)
        javaCookieStore?.let { store ->
            cookies.addAll(getCookiesFromJavaStore(store))
        }

        // Note: WebView CookieManager doesn't provide a way to enumerate all cookies
        // without knowing the domains. We track known domains from the Java store.

        return cookies.distinctBy { "${it.domain}|${it.name}|${it.path}" }
    }

    /**
     * Gets cookies for a specific domain.
     */
    fun getCookiesForDomain(domain: String): List<CookieInfo> {
        val cookies = mutableListOf<CookieInfo>()

        // From Java CookieStore
        javaCookieStore?.let { store ->
            cookies.addAll(
                getCookiesFromJavaStore(store).filter {
                    it.domain.equals(domain, ignoreCase = true) ||
                        it.domain.endsWith(".$domain", ignoreCase = true) ||
                        domain.endsWith(".${it.domain}", ignoreCase = true)
                },
            )
        }

        // From WebView CookieManager
        webViewCookieManager?.let { manager ->
            val webViewCookies = getWebViewCookiesForDomain(manager, domain)
            cookies.addAll(webViewCookies)
        }

        return cookies.distinctBy { "${it.domain}|${it.name}|${it.path}" }
    }

    /**
     * Gets all unique domains that have cookies.
     */
    fun getAllDomains(): List<String> {
        val domains = mutableSetOf<String>()

        javaCookieStore?.let { store ->
            store.urIs.forEach { uri ->
                uri.host?.let { domains.add(it) }
            }
        }

        return domains.sorted()
    }

    /**
     * Deletes a specific cookie.
     */
    fun deleteCookie(domain: String, name: String) {
        // Remove from Java CookieStore
        javaCookieStore?.let { store ->
            val uris = store.urIs.filter { it.host?.equals(domain, ignoreCase = true) == true }
            uris.forEach { uri ->
                store.get(uri).filter { it.name == name }.forEach { cookie ->
                    store.remove(uri, cookie)
                }
            }
        }

        // Remove from WebView - requires setting expired cookie
        webViewCookieManager?.let { manager ->
            val expiredCookie = "$name=; Domain=$domain; Path=/; Max-Age=0"
            manager.setCookie(domain, expiredCookie)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                manager.flush()
            }
        }
    }

    /**
     * Deletes all cookies for a domain.
     */
    fun deleteAllCookiesForDomain(domain: String) {
        // Remove from Java CookieStore
        javaCookieStore?.let { store ->
            val uris = store.urIs.filter {
                it.host?.equals(domain, ignoreCase = true) == true ||
                    it.host?.endsWith(".$domain", ignoreCase = true) == true
            }
            uris.forEach { uri ->
                store.get(uri).forEach { cookie ->
                    store.remove(uri, cookie)
                }
            }
        }

        // For WebView, we need to get all cookies and expire them
        webViewCookieManager?.let { manager ->
            val cookies = getWebViewCookiesForDomain(manager, domain)
            cookies.forEach { cookie ->
                val expiredCookie = "${cookie.name}=; Domain=${cookie.domain}; Path=${cookie.path}; Max-Age=0"
                manager.setCookie(domain, expiredCookie)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                manager.flush()
            }
        }
    }

    /**
     * Clears all cookies from all sources.
     */
    fun clearAllCookies() {
        // Clear Java CookieStore
        javaCookieStore?.removeAll()

        // Clear WebView cookies
        webViewCookieManager?.let { manager ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                manager.removeAllCookies(null)
                manager.flush()
            } else {
                @Suppress("DEPRECATION")
                manager.removeAllCookie()
            }
        }
    }

    private fun getCookiesFromJavaStore(store: CookieStore): List<CookieInfo> {
        return store.cookies.map { httpCookie ->
            httpCookieToCookieInfo(httpCookie)
        }
    }

    private fun httpCookieToCookieInfo(httpCookie: HttpCookie): CookieInfo {
        val expiresAt = if (httpCookie.maxAge >= 0) {
            System.currentTimeMillis() + (httpCookie.maxAge * 1000)
        } else {
            null
        }

        return CookieInfo(
            name = httpCookie.name,
            value = httpCookie.value,
            domain = httpCookie.domain ?: "",
            path = httpCookie.path ?: "/",
            expiresAt = expiresAt,
            isSecure = httpCookie.secure,
            isHttpOnly = httpCookie.isHttpOnly,
            sameSite = null, // HttpCookie doesn't expose SameSite
        )
    }

    private fun getWebViewCookiesForDomain(manager: CookieManager, domain: String): List<CookieInfo> {
        val cookies = mutableListOf<CookieInfo>()

        // Try both http and https
        listOf("http://$domain", "https://$domain").forEach { url ->
            val cookieString = manager.getCookie(url) ?: return@forEach
            cookies.addAll(parseWebViewCookieString(cookieString, domain))
        }

        return cookies.distinctBy { "${it.domain}|${it.name}" }
    }

    private fun parseWebViewCookieString(cookieString: String, domain: String): List<CookieInfo> {
        return cookieString.split(";").mapNotNull { part ->
            val trimmed = part.trim()
            val equalsIndex = trimmed.indexOf('=')
            if (equalsIndex > 0) {
                val name = trimmed.substring(0, equalsIndex).trim()
                val value = trimmed.substring(equalsIndex + 1).trim()

                // Skip cookie attributes
                if (name.equals("path", ignoreCase = true) ||
                    name.equals("domain", ignoreCase = true) ||
                    name.equals("expires", ignoreCase = true) ||
                    name.equals("max-age", ignoreCase = true) ||
                    name.equals("secure", ignoreCase = true) ||
                    name.equals("httponly", ignoreCase = true) ||
                    name.equals("samesite", ignoreCase = true)
                ) {
                    null
                } else {
                    CookieInfo(
                        name = name,
                        value = value,
                        domain = domain,
                        path = "/",
                        expiresAt = null, // WebView doesn't expose this in getCookie()
                        isSecure = false,
                        isHttpOnly = false,
                        sameSite = null,
                    )
                }
            } else {
                null
            }
        }
    }
}

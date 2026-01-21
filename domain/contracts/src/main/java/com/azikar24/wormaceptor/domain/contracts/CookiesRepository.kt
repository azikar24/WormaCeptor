/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.CookieInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing and managing HTTP cookies.
 * Supports both system CookieManager and WebView cookies.
 */
interface CookiesRepository {

    /**
     * Observes all cookies from all sources.
     * Emits updates when cookies change.
     */
    fun getAllCookies(): Flow<List<CookieInfo>>

    /**
     * Observes cookies for a specific domain.
     *
     * @param domain The domain to filter cookies by
     */
    fun getCookiesForDomain(domain: String): Flow<List<CookieInfo>>

    /**
     * Deletes a specific cookie by domain and name.
     *
     * @param domain The cookie's domain
     * @param name The cookie's name
     */
    suspend fun deleteCookie(domain: String, name: String)

    /**
     * Deletes all cookies for a specific domain.
     *
     * @param domain The domain whose cookies should be deleted
     */
    suspend fun deleteAllCookiesForDomain(domain: String)

    /**
     * Clears all cookies from all sources.
     */
    suspend fun clearAllCookies()

    /**
     * Refreshes the cookie data from sources.
     */
    suspend fun refresh()
}

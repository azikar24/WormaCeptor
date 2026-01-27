/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.cookies.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.domain.contracts.CookiesRepository
import com.azikar24.wormaceptor.domain.entities.CookieDomain
import com.azikar24.wormaceptor.domain.entities.CookieInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Represents a change to a cookie.
 */
enum class CookieChangeType {
    ADDED,
    MODIFIED,
    REMOVED,
}

/**
 * Tracks a cookie change with timestamp.
 */
data class CookieChange(
    val type: CookieChangeType,
    val cookieKey: String, // domain:name
    val timestamp: Long,
)

/**
 * ViewModel for the Cookies Manager feature.
 * Handles search, filtering, and management of HTTP cookies.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class CookiesViewModel(
    private val repository: CookiesRepository,
) : ViewModel() {

    // Change tracking - maps cookie key (domain:name) to change info
    private val _cookieChanges = MutableStateFlow<ImmutableMap<String, CookieChange>>(persistentMapOf())
    val cookieChanges: StateFlow<ImmutableMap<String, CookieChange>> = _cookieChanges.asStateFlow()

    // Change counts for UI summary banner
    val newCookiesCount: StateFlow<Int> = _cookieChanges
        .map { changes ->
            val thirtySecondsAgo = System.currentTimeMillis() - 30_000
            changes.values.count { it.type == CookieChangeType.ADDED && it.timestamp > thirtySecondsAgo }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val modifiedCookiesCount: StateFlow<Int> = _cookieChanges
        .map { changes ->
            val thirtySecondsAgo = System.currentTimeMillis() - 30_000
            changes.values.count { it.type == CookieChangeType.MODIFIED && it.timestamp > thirtySecondsAgo }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Store previous cookie state for change detection
    private var previousCookies: Map<String, String> = emptyMap() // key -> value

    // Search query for filtering cookies
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Expanded domains in the list view
    private val _expandedDomains = MutableStateFlow<ImmutableSet<String>>(persistentSetOf())
    val expandedDomains: StateFlow<ImmutableSet<String>> = _expandedDomains

    // Currently selected cookie for detail view
    private val _selectedCookie = MutableStateFlow<CookieInfo?>(null)
    val selectedCookie: StateFlow<CookieInfo?> = _selectedCookie

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // All cookies grouped by domain
    val cookieDomains: StateFlow<ImmutableList<CookieDomain>> = combine(
        repository.getAllCookies(),
        _searchQuery.debounce(150),
    ) { cookies, query ->
        // Detect changes
        detectCookieChanges(cookies)

        val filteredCookies = if (query.isBlank()) {
            cookies
        } else {
            cookies.filter { cookie ->
                cookie.name.contains(query, ignoreCase = true) ||
                    cookie.value.contains(query, ignoreCase = true) ||
                    cookie.domain.contains(query, ignoreCase = true)
            }
        }

        // Group by domain
        filteredCookies
            .groupBy { it.domain }
            .map { (domain, domainCookies) ->
                CookieDomain(
                    domain = domain,
                    cookies = domainCookies.sortedBy { it.name.lowercase() },
                )
            }
            .sortedBy { it.domain.lowercase() }
            .toImmutableList()
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    // Total cookie count
    val totalCookieCount: StateFlow<Int> = repository.getAllCookies()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Total domain count
    val totalDomainCount: StateFlow<Int> = cookieDomains
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleDomainExpanded(domain: String) {
        _expandedDomains.value = if (_expandedDomains.value.contains(domain)) {
            (_expandedDomains.value - domain).toImmutableSet()
        } else {
            (_expandedDomains.value + domain).toImmutableSet()
        }
    }

    fun expandAllDomains() {
        _expandedDomains.value = cookieDomains.value.map { it.domain }.toImmutableSet()
    }

    fun collapseAllDomains() {
        _expandedDomains.value = persistentSetOf()
    }

    fun selectCookie(cookie: CookieInfo) {
        _selectedCookie.value = cookie
    }

    fun clearCookieSelection() {
        _selectedCookie.value = null
    }

    fun deleteCookie(cookie: CookieInfo) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteCookie(cookie.domain, cookie.name)
                if (_selectedCookie.value == cookie) {
                    _selectedCookie.value = null
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAllCookiesForDomain(domain: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteAllCookiesForDomain(domain)
                _expandedDomains.value = (_expandedDomains.value - domain).toImmutableSet()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearAllCookies() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.clearAllCookies()
                _expandedDomains.value = persistentSetOf()
                _selectedCookie.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.refresh()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Gets the change type for a specific cookie.
     *
     * @param domain Cookie domain
     * @param name Cookie name
     * @return The change type, or null if no recent change
     */
    fun getChangeType(domain: String, name: String): CookieChangeType? {
        val key = "$domain:$name"
        val change = _cookieChanges.value[key] ?: return null

        // Only show changes from the last 30 seconds
        val thirtySecondsAgo = System.currentTimeMillis() - 30_000
        return if (change.timestamp > thirtySecondsAgo) change.type else null
    }

    /**
     * Clears all tracked changes.
     */
    fun clearChanges() {
        _cookieChanges.value = persistentMapOf()
    }

    private fun detectCookieChanges(currentCookies: List<CookieInfo>) {
        val currentMap = currentCookies.associate { "${it.domain}:${it.name}" to it.value }
        val now = System.currentTimeMillis()
        val changes = mutableMapOf<String, CookieChange>()

        // Copy existing changes that are still recent (within 30 seconds)
        val thirtySecondsAgo = now - 30_000
        _cookieChanges.value.forEach { (key, change) ->
            if (change.timestamp > thirtySecondsAgo) {
                changes[key] = change
            }
        }

        // Detect new and modified cookies
        currentMap.forEach { (key, value) ->
            val previousValue = previousCookies[key]
            when {
                previousValue == null -> {
                    // New cookie (only if we had previous state)
                    if (previousCookies.isNotEmpty()) {
                        changes[key] = CookieChange(CookieChangeType.ADDED, key, now)
                    }
                }
                previousValue != value -> {
                    // Modified cookie
                    changes[key] = CookieChange(CookieChangeType.MODIFIED, key, now)
                }
            }
        }

        // Detect removed cookies
        previousCookies.keys.forEach { key ->
            if (key !in currentMap) {
                changes[key] = CookieChange(CookieChangeType.REMOVED, key, now)
            }
        }

        // Update state
        previousCookies = currentMap
        _cookieChanges.value = changes.toImmutableMap()
    }
}

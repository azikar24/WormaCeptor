/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.content.Context
import android.content.SharedPreferences
import com.azikar24.wormaceptor.domain.entities.PushTokenInfo
import com.azikar24.wormaceptor.domain.entities.PushTokenInfo.PushProvider
import com.azikar24.wormaceptor.domain.entities.TokenHistory
import com.azikar24.wormaceptor.domain.entities.TokenHistory.TokenEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Engine that manages push notification tokens.
 *
 * Detects and retrieves FCM tokens via FirebaseMessaging (when available),
 * handles token refresh callbacks, and maintains token history.
 *
 * Features:
 * - Automatic FCM token detection
 * - Token history tracking with persistence
 * - Provider detection (FCM, HMS, etc.)
 * - Token refresh and deletion support
 */
class PushTokenEngine(
    private val context: Context,
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Current token state
    private val _currentToken = MutableStateFlow<PushTokenInfo?>(null)
    val currentToken: StateFlow<PushTokenInfo?> = _currentToken.asStateFlow()

    // Token history
    private val _tokenHistory = MutableStateFlow<List<TokenHistory>>(emptyList())
    val tokenHistory: StateFlow<List<TokenHistory>> = _tokenHistory.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Load persisted history on init
        loadPersistedHistory()
        loadPersistedToken()
    }

    /**
     * Fetches the current push token from available providers.
     * Tries FCM first, then falls back to other providers.
     */
    fun fetchCurrentToken() {
        scope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val tokenInfo = fetchTokenInternal()
                if (tokenInfo != null) {
                    _currentToken.value = tokenInfo
                    persistToken(tokenInfo)

                    // Add to history if this is a new token
                    if (_tokenHistory.value.none { it.token == tokenInfo.token && it.event == TokenEvent.CREATED }) {
                        addHistoryEntry(tokenInfo.token, TokenEvent.CREATED)
                    }
                } else {
                    _error.value = "No push token available. Firebase may not be configured."
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch token: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Requests a new token from the push provider.
     * This may invalidate the previous token.
     */
    fun requestNewToken() {
        scope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val oldToken = _currentToken.value?.token

                // Try to delete existing token first
                deleteTokenInternal()

                // Fetch new token
                val newTokenInfo = fetchTokenInternal()
                if (newTokenInfo != null) {
                    _currentToken.value = newTokenInfo
                    persistToken(newTokenInfo)

                    // Add refresh event if token changed
                    if (oldToken != null && oldToken != newTokenInfo.token) {
                        addHistoryEntry(oldToken, TokenEvent.INVALIDATED)
                    }
                    addHistoryEntry(newTokenInfo.token, TokenEvent.REFRESHED)
                } else {
                    _error.value = "Failed to get new token"
                }
            } catch (e: Exception) {
                _error.value = "Failed to refresh token: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes the current push token.
     */
    fun deleteToken() {
        scope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val currentTokenValue = _currentToken.value?.token
                deleteTokenInternal()

                if (currentTokenValue != null) {
                    addHistoryEntry(currentTokenValue, TokenEvent.DELETED)
                }

                _currentToken.value = null
                clearPersistedToken()
            } catch (e: Exception) {
                _error.value = "Failed to delete token: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clears all token history.
     */
    fun clearHistory() {
        _tokenHistory.value = emptyList()
        persistHistory(emptyList())
    }

    /**
     * Clears the current error.
     */
    fun clearError() {
        _error.value = null
    }

    private suspend fun fetchTokenInternal(): PushTokenInfo? = withContext(Dispatchers.IO) {
        // Try FCM first
        try {
            val fcmToken = getFcmToken()
            if (fcmToken != null) {
                val now = System.currentTimeMillis()
                return@withContext PushTokenInfo(
                    token = fcmToken,
                    provider = PushProvider.FCM,
                    createdAt = _currentToken.value?.createdAt ?: now,
                    lastRefreshed = now,
                    isValid = true,
                    associatedUserId = null,
                    metadata = buildMetadata(),
                )
            }
        } catch (e: Exception) {
            // FCM not available, continue
        }

        // Try Huawei HMS
        try {
            val hmsToken = getHmsToken()
            if (hmsToken != null) {
                val now = System.currentTimeMillis()
                return@withContext PushTokenInfo(
                    token = hmsToken,
                    provider = PushProvider.HUAWEI_HMS,
                    createdAt = _currentToken.value?.createdAt ?: now,
                    lastRefreshed = now,
                    isValid = true,
                    associatedUserId = null,
                    metadata = buildMetadata(),
                )
            }
        } catch (e: Exception) {
            // HMS not available, continue
        }

        null
    }

    private fun getFcmToken(): String? {
        return try {
            // Use reflection to avoid compile-time dependency on Firebase
            val firebaseMessagingClass = Class.forName("com.google.firebase.messaging.FirebaseMessaging")
            val getInstance = firebaseMessagingClass.getMethod("getInstance")
            val instance = getInstance.invoke(null)

            val getToken = firebaseMessagingClass.getMethod("getToken")
            val taskResult = getToken.invoke(instance)

            // Get the task result synchronously using Tasks.await()
            val tasksClass = Class.forName("com.google.android.gms.tasks.Tasks")
            val awaitMethod = tasksClass.getMethod("await", Class.forName("com.google.android.gms.tasks.Task"))
            val token = awaitMethod.invoke(null, taskResult) as? String

            token
        } catch (e: ClassNotFoundException) {
            // Firebase not available
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun getHmsToken(): String? {
        return try {
            // Use reflection to avoid compile-time dependency on HMS
            val hmsInstanceIdClass = Class.forName("com.huawei.hms.aaid.HmsInstanceId")
            val getInstance = hmsInstanceIdClass.getMethod("getInstance", Context::class.java)
            val instance = getInstance.invoke(null, context)

            // Get app ID from agconnect-services.json
            val agConnectServicesClass = Class.forName("com.huawei.agconnect.config.AGConnectServicesConfig")
            val agGetInstance = agConnectServicesClass.getMethod("fromContext", Context::class.java)
            val agInstance = agGetInstance.invoke(null, context)
            val getString = agConnectServicesClass.getMethod("getString", String::class.java)
            val appId = getString.invoke(agInstance, "client/app_id") as? String ?: return null

            val getToken = hmsInstanceIdClass.getMethod("getToken", String::class.java, String::class.java)
            val token = getToken.invoke(instance, appId, "HCM") as? String

            token
        } catch (e: ClassNotFoundException) {
            // HMS not available
            null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun deleteTokenInternal() = withContext(Dispatchers.IO) {
        // Try FCM deletion
        try {
            val firebaseMessagingClass = Class.forName("com.google.firebase.messaging.FirebaseMessaging")
            val getInstance = firebaseMessagingClass.getMethod("getInstance")
            val instance = getInstance.invoke(null)

            val deleteToken = firebaseMessagingClass.getMethod("deleteToken")
            val taskResult = deleteToken.invoke(instance)

            val tasksClass = Class.forName("com.google.android.gms.tasks.Tasks")
            val awaitMethod = tasksClass.getMethod("await", Class.forName("com.google.android.gms.tasks.Task"))
            awaitMethod.invoke(null, taskResult)
        } catch (e: Exception) {
            // FCM deletion failed or not available
        }

        // Try HMS deletion
        try {
            val hmsInstanceIdClass = Class.forName("com.huawei.hms.aaid.HmsInstanceId")
            val getInstance = hmsInstanceIdClass.getMethod("getInstance", Context::class.java)
            val instance = getInstance.invoke(null, context)

            val agConnectServicesClass = Class.forName("com.huawei.agconnect.config.AGConnectServicesConfig")
            val agGetInstance = agConnectServicesClass.getMethod("fromContext", Context::class.java)
            val agInstance = agGetInstance.invoke(null, context)
            val getString = agConnectServicesClass.getMethod("getString", String::class.java)
            val appId = getString.invoke(agInstance, "client/app_id") as? String ?: return@withContext

            val deleteToken = hmsInstanceIdClass.getMethod("deleteToken", String::class.java, String::class.java)
            deleteToken.invoke(instance, appId, "HCM")
        } catch (e: Exception) {
            // HMS deletion failed or not available
        }
    }

    private fun buildMetadata(): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        metadata["package"] = context.packageName
        metadata["sdk_version"] = android.os.Build.VERSION.SDK_INT.toString()
        metadata["device"] = android.os.Build.MODEL
        return metadata
    }

    private fun addHistoryEntry(token: String, event: TokenEvent) {
        val entry = TokenHistory(
            token = token,
            timestamp = System.currentTimeMillis(),
            event = event,
        )
        val newHistory = listOf(entry) + _tokenHistory.value.take(MAX_HISTORY_SIZE - 1)
        _tokenHistory.value = newHistory
        persistHistory(newHistory)
    }

    private fun persistToken(tokenInfo: PushTokenInfo) {
        prefs.edit()
            .putString(KEY_TOKEN, tokenInfo.token)
            .putString(KEY_PROVIDER, tokenInfo.provider.name)
            .putLong(KEY_CREATED_AT, tokenInfo.createdAt)
            .putLong(KEY_LAST_REFRESHED, tokenInfo.lastRefreshed)
            .putBoolean(KEY_IS_VALID, tokenInfo.isValid)
            .putString(KEY_USER_ID, tokenInfo.associatedUserId)
            .putString(KEY_METADATA, JSONObject(tokenInfo.metadata).toString())
            .apply()
    }

    private fun loadPersistedToken() {
        val token = prefs.getString(KEY_TOKEN, null) ?: return
        if (token.isEmpty()) return

        val providerName = prefs.getString(KEY_PROVIDER, PushProvider.UNKNOWN.name) ?: PushProvider.UNKNOWN.name
        val provider = try {
            PushProvider.valueOf(providerName)
        } catch (e: Exception) {
            PushProvider.UNKNOWN
        }

        val metadataJson = prefs.getString(KEY_METADATA, "{}") ?: "{}"
        val metadata = try {
            val json = JSONObject(metadataJson)
            json.keys().asSequence().associateWith { json.getString(it) }
        } catch (e: Exception) {
            emptyMap()
        }

        _currentToken.value = PushTokenInfo(
            token = token,
            provider = provider,
            createdAt = prefs.getLong(KEY_CREATED_AT, 0L),
            lastRefreshed = prefs.getLong(KEY_LAST_REFRESHED, 0L),
            isValid = prefs.getBoolean(KEY_IS_VALID, false),
            associatedUserId = prefs.getString(KEY_USER_ID, null),
            metadata = metadata,
        )
    }

    private fun clearPersistedToken() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_PROVIDER)
            .remove(KEY_CREATED_AT)
            .remove(KEY_LAST_REFRESHED)
            .remove(KEY_IS_VALID)
            .remove(KEY_USER_ID)
            .remove(KEY_METADATA)
            .apply()
    }

    private fun persistHistory(history: List<TokenHistory>) {
        val jsonArray = JSONArray()
        history.forEach { entry ->
            val obj = JSONObject()
            obj.put("token", entry.token)
            obj.put("timestamp", entry.timestamp)
            obj.put("event", entry.event.name)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }

    private fun loadPersistedHistory() {
        val json = prefs.getString(KEY_HISTORY, null) ?: return

        try {
            val jsonArray = JSONArray(json)
            val history = mutableListOf<TokenHistory>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val event = try {
                    TokenEvent.valueOf(obj.getString("event"))
                } catch (e: Exception) {
                    continue
                }

                history.add(
                    TokenHistory(
                        token = obj.getString("token"),
                        timestamp = obj.getLong("timestamp"),
                        event = event,
                    ),
                )
            }

            _tokenHistory.value = history
        } catch (e: Exception) {
            // Failed to parse history, start fresh
        }
    }

    companion object {
        private const val PREFS_NAME = "wormaceptor_push_token"
        private const val KEY_TOKEN = "token"
        private const val KEY_PROVIDER = "provider"
        private const val KEY_CREATED_AT = "created_at"
        private const val KEY_LAST_REFRESHED = "last_refreshed"
        private const val KEY_IS_VALID = "is_valid"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_METADATA = "metadata"
        private const val KEY_HISTORY = "history"
        private const val MAX_HISTORY_SIZE = 50
    }
}

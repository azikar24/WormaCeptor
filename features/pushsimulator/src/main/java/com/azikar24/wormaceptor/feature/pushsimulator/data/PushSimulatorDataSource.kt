/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.pushsimulator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.azikar24.wormaceptor.domain.entities.NotificationAction
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate
import com.azikar24.wormaceptor.domain.entities.SimulatedNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * DataStore-based storage for notification templates.
 * Uses JSON serialization to persist templates.
 */
class PushSimulatorDataSource(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "wormaceptor_push_templates",
    )

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Observes all saved notification templates.
     */
    fun observeTemplates(): Flow<List<NotificationTemplate>> {
        return context.dataStore.data.map { preferences ->
            val templatesJson = preferences[Keys.TEMPLATES] ?: "[]"
            try {
                val storedTemplates = json.decodeFromString<List<StoredTemplate>>(templatesJson)
                storedTemplates.map { it.toNotificationTemplate() }
            } catch (e: Exception) {
                getPresetTemplates()
            }
        }
    }

    /**
     * Saves a notification template.
     */
    suspend fun saveTemplate(template: NotificationTemplate) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[Keys.TEMPLATES] ?: "[]"
            val currentTemplates = try {
                json.decodeFromString<MutableList<StoredTemplate>>(currentJson)
            } catch (e: Exception) {
                mutableListOf()
            }

            // Remove existing template with same ID if present
            currentTemplates.removeAll { it.id == template.id }
            currentTemplates.add(StoredTemplate.fromNotificationTemplate(template))

            preferences[Keys.TEMPLATES] = json.encodeToString(currentTemplates)
        }
    }

    /**
     * Deletes a template by ID.
     */
    suspend fun deleteTemplate(id: String) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[Keys.TEMPLATES] ?: "[]"
            val currentTemplates = try {
                json.decodeFromString<MutableList<StoredTemplate>>(currentJson)
            } catch (e: Exception) {
                mutableListOf()
            }

            currentTemplates.removeAll { it.id == id }
            preferences[Keys.TEMPLATES] = json.encodeToString(currentTemplates)
        }
    }

    /**
     * Initializes with preset templates if none exist.
     */
    suspend fun initializePresets() {
        context.dataStore.edit { preferences ->
            if (!preferences.contains(Keys.TEMPLATES)) {
                val presets = getPresetTemplates().map { StoredTemplate.fromNotificationTemplate(it) }
                preferences[Keys.TEMPLATES] = json.encodeToString(presets)
            }
        }
    }

    /**
     * Clears all stored templates.
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.TEMPLATES)
        }
    }

    private object Keys {
        val TEMPLATES = stringPreferencesKey("notification_templates")
    }

    companion object {
        /**
         * Returns the default preset templates.
         */
        fun getPresetTemplates(): List<NotificationTemplate> = listOf(
            NotificationTemplate(
                id = "preset_simple_alert",
                name = "Simple Alert",
                notification = SimulatedNotification(
                    id = "simple_alert",
                    title = "Alert",
                    body = "This is a test notification",
                    channelId = "wormaceptor_test_channel",
                    priority = NotificationPriority.DEFAULT,
                ),
            ),
            NotificationTemplate(
                id = "preset_message",
                name = "Message Style",
                notification = SimulatedNotification(
                    id = "message_style",
                    title = "New Message",
                    body = "You have a new message from John",
                    channelId = "wormaceptor_test_channel",
                    priority = NotificationPriority.HIGH,
                ),
            ),
            NotificationTemplate(
                id = "preset_actions",
                name = "Action Buttons",
                notification = SimulatedNotification(
                    id = "action_buttons",
                    title = "Download Complete",
                    body = "Your file is ready",
                    channelId = "wormaceptor_test_channel",
                    priority = NotificationPriority.DEFAULT,
                    actions = listOf(
                        NotificationAction(title = "Open", actionId = "action_open"),
                        NotificationAction(title = "Share", actionId = "action_share"),
                    ),
                ),
            ),
        )
    }
}

/**
 * Serializable version of NotificationTemplate for JSON storage.
 */
@Serializable
private data class StoredTemplate(
    val id: String,
    val name: String,
    val notificationId: String,
    val title: String,
    val body: String,
    val channelId: String,
    val smallIconRes: Int = 0,
    val largeIconUri: String? = null,
    val priority: String = "DEFAULT",
    val actions: List<StoredAction> = emptyList(),
    val extras: Map<String, String> = emptyMap(),
    val timestamp: Long = 0L,
) {
    fun toNotificationTemplate(): NotificationTemplate {
        return NotificationTemplate(
            id = id,
            name = name,
            notification = SimulatedNotification(
                id = notificationId,
                title = title,
                body = body,
                channelId = channelId,
                smallIconRes = smallIconRes,
                largeIconUri = largeIconUri,
                priority = NotificationPriority.valueOf(priority),
                actions = actions.map { NotificationAction(it.title, it.actionId) },
                extras = extras,
                timestamp = timestamp,
            ),
        )
    }

    companion object {
        fun fromNotificationTemplate(template: NotificationTemplate): StoredTemplate {
            return StoredTemplate(
                id = template.id,
                name = template.name,
                notificationId = template.notification.id,
                title = template.notification.title,
                body = template.notification.body,
                channelId = template.notification.channelId,
                smallIconRes = template.notification.smallIconRes,
                largeIconUri = template.notification.largeIconUri,
                priority = template.notification.priority.name,
                actions = template.notification.actions.map { StoredAction(it.title, it.actionId) },
                extras = template.notification.extras,
                timestamp = template.notification.timestamp,
            )
        }
    }
}

@Serializable
private data class StoredAction(
    val title: String,
    val actionId: String,
)

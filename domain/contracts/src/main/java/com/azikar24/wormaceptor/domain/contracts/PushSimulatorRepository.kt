package com.azikar24.wormaceptor.domain.contracts

import com.azikar24.wormaceptor.domain.entities.NotificationChannelInfo
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing notification templates and channels.
 */
interface PushSimulatorRepository {

    /**
     * Observes all saved notification templates.
     * @return Flow emitting the list of templates whenever it changes
     */
    fun getTemplates(): Flow<List<NotificationTemplate>>

    /**
     * Saves a notification template.
     * @param template The template to save
     */
    suspend fun saveTemplate(template: NotificationTemplate)

    /**
     * Deletes a notification template by ID.
     * @param id The ID of the template to delete
     */
    suspend fun deleteTemplate(id: String)

    /**
     * Gets all notification channels registered by the app.
     * @return List of notification channel information
     */
    suspend fun getNotificationChannels(): List<NotificationChannelInfo>
}

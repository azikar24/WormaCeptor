package com.azikar24.wormaceptor.infra.persistence.sqlite

import com.azikar24.wormaceptor.domain.contracts.PushSimulatorRepository
import com.azikar24.wormaceptor.domain.entities.NotificationAction
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate
import com.azikar24.wormaceptor.domain.entities.SimulatedNotification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [PushSimulatorRepository] implementation with preset notification templates. */
class InMemoryPushSimulatorRepository : PushSimulatorRepository {

    private val _templates = MutableStateFlow(presetTemplates)

    override fun getTemplates(): Flow<List<NotificationTemplate>> {
        return _templates.map { it.toList() }
    }

    override suspend fun saveTemplate(template: NotificationTemplate) {
        _templates.update { current ->
            current.filter { it.id != template.id } + template
        }
    }

    override suspend fun deleteTemplate(id: String) {
        _templates.update { current -> current.filter { it.id != id } }
    }

    /** Pre-configured notification templates for quick testing. */
    companion object {
        private val presetTemplates: List<NotificationTemplate> = listOf(
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

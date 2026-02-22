package com.azikar24.wormaceptor.feature.pushsimulator.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.NotificationPermissionException
import com.azikar24.wormaceptor.core.engine.PushSimulatorEngine
import com.azikar24.wormaceptor.domain.contracts.PushSimulatorRepository
import com.azikar24.wormaceptor.domain.entities.NotificationAction
import com.azikar24.wormaceptor.domain.entities.NotificationChannelInfo
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate
import com.azikar24.wormaceptor.domain.entities.SimulatedNotification
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import java.util.UUID

private const val MaxActions = 3
private const val ActionIdLength = 8

/**
 * ViewModel for the Push Notification Simulator screen.
 */
class PushSimulatorViewModel(
    private val repository: PushSimulatorRepository,
    private val engine: PushSimulatorEngine,
) : BaseViewModel<PushSimulatorViewState, PushSimulatorViewEffect, PushSimulatorViewEvent>(
    PushSimulatorViewState(),
) {

    init {
        observeTemplates()
        loadChannels()
    }

    override fun handleEvent(event: PushSimulatorViewEvent) {
        when (event) {
            is PushSimulatorViewEvent.UpdateTitle -> updateState { copy(title = event.title) }
            is PushSimulatorViewEvent.UpdateBody -> updateState { copy(body = event.body) }
            is PushSimulatorViewEvent.UpdateChannelId -> updateState { copy(selectedChannelId = event.channelId) }
            is PushSimulatorViewEvent.UpdatePriority -> updateState { copy(priority = event.priority) }
            is PushSimulatorViewEvent.UpdateNewActionTitle -> updateState { copy(newActionTitle = event.title) }
            is PushSimulatorViewEvent.AddAction -> handleAddAction(event.title)
            is PushSimulatorViewEvent.RemoveAction -> handleRemoveAction(event.actionId)
            is PushSimulatorViewEvent.SendNotification -> handleSendNotification()
            is PushSimulatorViewEvent.SaveAsTemplate -> handleSaveAsTemplate(event.templateName)
            is PushSimulatorViewEvent.LoadTemplate -> handleLoadTemplate(event.template)
            is PushSimulatorViewEvent.DeleteTemplate -> handleDeleteTemplate(event.templateId)
            is PushSimulatorViewEvent.SendFromTemplate -> handleSendFromTemplate(event.template)
            is PushSimulatorViewEvent.ClearForm -> handleClearForm()
        }
    }

    private fun handleAddAction(title: String) {
        val currentActions = uiState.value.actions
        if (currentActions.size >= MaxActions) return

        val actionId = "action_${UUID.randomUUID().toString().take(ActionIdLength)}"
        val newAction = NotificationAction(title = title, actionId = actionId)
        updateState {
            copy(
                actions = (actions + newAction).toImmutableList(),
                newActionTitle = "",
            )
        }
    }

    private fun handleRemoveAction(actionId: String) {
        updateState {
            copy(actions = actions.filterNot { it.actionId == actionId }.toImmutableList())
        }
    }

    private fun handleSendNotification() {
        val state = uiState.value

        if (state.title.isBlank()) {
            emitEffect(PushSimulatorViewEffect.Error("Title is required"))
            return
        }

        val notification = SimulatedNotification(
            id = UUID.randomUUID().toString(),
            title = state.title,
            body = state.body,
            channelId = state.selectedChannelId.ifBlank { PushSimulatorEngine.DEFAULT_CHANNEL_ID },
            priority = state.priority,
            actions = state.actions,
            timestamp = System.currentTimeMillis(),
        )

        viewModelScope.launch {
            try {
                engine.sendNotification(notification)
                emitEffect(PushSimulatorViewEffect.NotificationSent)
            } catch (e: NotificationPermissionException) {
                emitEffect(PushSimulatorViewEffect.PermissionRequired)
            } catch (e: Exception) {
                emitEffect(PushSimulatorViewEffect.Error("Failed to send: ${e.message}"))
            }
        }
    }

    private fun handleSaveAsTemplate(templateName: String) {
        val state = uiState.value

        if (templateName.isBlank()) {
            emitEffect(PushSimulatorViewEffect.Error("Template name is required"))
            return
        }

        if (state.title.isBlank()) {
            emitEffect(PushSimulatorViewEffect.Error("Title is required to save template"))
            return
        }

        val template = NotificationTemplate(
            id = "user_${UUID.randomUUID()}",
            name = templateName,
            notification = SimulatedNotification(
                id = UUID.randomUUID().toString(),
                title = state.title,
                body = state.body,
                channelId = state.selectedChannelId.ifBlank { PushSimulatorEngine.DEFAULT_CHANNEL_ID },
                priority = state.priority,
                actions = state.actions,
            ),
        )

        viewModelScope.launch {
            repository.saveTemplate(template)
            emitEffect(PushSimulatorViewEffect.TemplateSaved)
        }
    }

    private fun handleLoadTemplate(template: NotificationTemplate) {
        updateState {
            copy(
                title = template.notification.title,
                body = template.notification.body,
                selectedChannelId = template.notification.channelId,
                priority = template.notification.priority,
                actions = template.notification.actions.toImmutableList(),
            )
        }
        emitEffect(PushSimulatorViewEffect.TemplateLoaded(template.name))
    }

    private fun handleDeleteTemplate(templateId: String) {
        viewModelScope.launch {
            repository.deleteTemplate(templateId)
            emitEffect(PushSimulatorViewEffect.TemplateDeleted)
        }
    }

    private fun handleSendFromTemplate(template: NotificationTemplate) {
        viewModelScope.launch {
            try {
                val notification = template.notification.copy(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                )
                engine.sendNotification(notification)
                emitEffect(PushSimulatorViewEffect.NotificationSent)
            } catch (e: NotificationPermissionException) {
                emitEffect(PushSimulatorViewEffect.PermissionRequired)
            } catch (e: Exception) {
                emitEffect(PushSimulatorViewEffect.Error("Failed to send: ${e.message}"))
            }
        }
    }

    private fun handleClearForm() {
        updateState {
            PushSimulatorViewState(
                selectedChannelId = selectedChannelId,
                channels = channels,
                templates = templates,
            )
        }
    }

    /**
     * Checks if notification permission is granted.
     */
    fun hasPermission(): Boolean {
        return engine.hasNotificationPermission()
    }

    private fun observeTemplates() {
        viewModelScope.launch {
            repository.getTemplates().collect { templateList ->
                updateState { copy(templates = templateList.toImmutableList()) }
            }
        }
    }

    private fun loadChannels() {
        viewModelScope.launch {
            engine.createDefaultChannel()
            val channelList = engine.getNotificationChannels().ifEmpty {
                listOf(
                    NotificationChannelInfo(
                        id = PushSimulatorEngine.DEFAULT_CHANNEL_ID,
                        name = PushSimulatorEngine.DEFAULT_CHANNEL_NAME,
                        description = PushSimulatorEngine.DEFAULT_CHANNEL_DESCRIPTION,
                        importance = android.app.NotificationManager.IMPORTANCE_DEFAULT,
                    ),
                )
            }
            updateState {
                copy(
                    channels = channelList.toImmutableList(),
                    selectedChannelId = if (selectedChannelId.isBlank() && channelList.isNotEmpty()) {
                        channelList.first().id
                    } else {
                        selectedChannelId
                    },
                )
            }
        }
    }
}

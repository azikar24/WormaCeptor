package com.azikar24.wormaceptor.feature.pushsimulator.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.NotificationPermissionException
import com.azikar24.wormaceptor.core.engine.PushSimulatorEngine
import com.azikar24.wormaceptor.domain.contracts.PushSimulatorRepository
import com.azikar24.wormaceptor.domain.entities.NotificationAction
import com.azikar24.wormaceptor.domain.entities.NotificationChannelInfo
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate
import com.azikar24.wormaceptor.domain.entities.SimulatedNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for the Push Notification Simulator screen.
 */
class PushSimulatorViewModel(
    private val repository: PushSimulatorRepository,
    private val engine: PushSimulatorEngine,
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(PushSimulatorUiState())
    val uiState: StateFlow<PushSimulatorUiState> = _uiState.asStateFlow()

    // Templates from repository
    val templates: StateFlow<List<NotificationTemplate>> = repository.getTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    // Notification channels
    private val _channels = MutableStateFlow<List<NotificationChannelInfo>>(emptyList())
    val channels: StateFlow<List<NotificationChannelInfo>> = _channels.asStateFlow()

    // Events
    private val _events = MutableStateFlow<PushSimulatorEvent?>(null)
    val events: StateFlow<PushSimulatorEvent?> = _events.asStateFlow()

    init {
        loadChannels()
    }

    /**
     * Updates the notification title.
     */
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    /**
     * Updates the notification body.
     */
    fun updateBody(body: String) {
        _uiState.value = _uiState.value.copy(body = body)
    }

    /**
     * Updates the selected channel ID.
     */
    fun updateChannelId(channelId: String) {
        _uiState.value = _uiState.value.copy(selectedChannelId = channelId)
    }

    /**
     * Updates the notification priority.
     */
    fun updatePriority(priority: NotificationPriority) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }

    /**
     * Adds an action button to the notification.
     */
    fun addAction(title: String) {
        val currentState = _uiState.value
        if (currentState.actions.size >= 3) return

        val actionId = "action_${UUID.randomUUID().toString().take(8)}"
        val newAction = NotificationAction(title = title, actionId = actionId)
        _uiState.value = currentState.copy(
            actions = currentState.actions + newAction,
            newActionTitle = "",
        )
    }

    /**
     * Removes an action button by ID.
     */
    fun removeAction(actionId: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            actions = currentState.actions.filterNot { it.actionId == actionId },
        )
    }

    /**
     * Updates the new action title input.
     */
    fun updateNewActionTitle(title: String) {
        _uiState.value = _uiState.value.copy(newActionTitle = title)
    }

    /**
     * Sends the notification with current configuration.
     */
    fun sendNotification() {
        val state = _uiState.value

        if (state.title.isBlank()) {
            _events.value = PushSimulatorEvent.Error("Title is required")
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
                _events.value = PushSimulatorEvent.NotificationSent
            } catch (e: NotificationPermissionException) {
                _events.value = PushSimulatorEvent.PermissionRequired
            } catch (e: Exception) {
                _events.value = PushSimulatorEvent.Error("Failed to send: ${e.message}")
            }
        }
    }

    /**
     * Saves the current configuration as a template.
     */
    fun saveAsTemplate(templateName: String) {
        val state = _uiState.value

        if (templateName.isBlank()) {
            _events.value = PushSimulatorEvent.Error("Template name is required")
            return
        }

        if (state.title.isBlank()) {
            _events.value = PushSimulatorEvent.Error("Title is required to save template")
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
            _events.value = PushSimulatorEvent.TemplateSaved
        }
    }

    /**
     * Loads a template into the UI.
     */
    fun loadTemplate(template: NotificationTemplate) {
        _uiState.value = _uiState.value.copy(
            title = template.notification.title,
            body = template.notification.body,
            selectedChannelId = template.notification.channelId,
            priority = template.notification.priority,
            actions = template.notification.actions,
        )
        _events.value = PushSimulatorEvent.TemplateLoaded(template.name)
    }

    /**
     * Deletes a template.
     */
    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            repository.deleteTemplate(templateId)
            _events.value = PushSimulatorEvent.TemplateDeleted
        }
    }

    /**
     * Sends notification from a template directly without loading.
     */
    fun sendFromTemplate(template: NotificationTemplate) {
        viewModelScope.launch {
            try {
                val notification = template.notification.copy(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                )
                engine.sendNotification(notification)
                _events.value = PushSimulatorEvent.NotificationSent
            } catch (e: NotificationPermissionException) {
                _events.value = PushSimulatorEvent.PermissionRequired
            } catch (e: Exception) {
                _events.value = PushSimulatorEvent.Error("Failed to send: ${e.message}")
            }
        }
    }

    /**
     * Clears the current notification form.
     */
    fun clearForm() {
        _uiState.value = PushSimulatorUiState(
            selectedChannelId = _uiState.value.selectedChannelId,
        )
    }

    /**
     * Clears the current event after handling.
     */
    fun clearEvent() {
        _events.value = null
    }

    /**
     * Checks if notification permission is granted.
     */
    fun hasPermission(): Boolean {
        return engine.hasNotificationPermission()
    }

    private fun loadChannels() {
        viewModelScope.launch {
            val channelList = repository.getNotificationChannels()
            _channels.value = channelList

            // Set default channel if none selected
            if (_uiState.value.selectedChannelId.isBlank() && channelList.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    selectedChannelId = channelList.first().id,
                )
            }
        }
    }
}

/**
 * UI state for the Push Simulator screen.
 */
data class PushSimulatorUiState(
    val title: String = "",
    val body: String = "",
    val selectedChannelId: String = "",
    val priority: NotificationPriority = NotificationPriority.DEFAULT,
    val actions: List<NotificationAction> = emptyList(),
    val newActionTitle: String = "",
)

/**
 * Events emitted by the ViewModel.
 */
sealed class PushSimulatorEvent {
    data object NotificationSent : PushSimulatorEvent()
    data object TemplateSaved : PushSimulatorEvent()
    data object TemplateDeleted : PushSimulatorEvent()
    data class TemplateLoaded(val name: String) : PushSimulatorEvent()
    data object PermissionRequired : PushSimulatorEvent()
    data class Error(val message: String) : PushSimulatorEvent()
}

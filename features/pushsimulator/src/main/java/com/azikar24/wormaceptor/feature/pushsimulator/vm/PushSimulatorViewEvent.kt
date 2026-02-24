package com.azikar24.wormaceptor.feature.pushsimulator.vm

import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate

/**
 * User-initiated events dispatched to [PushSimulatorViewModel].
 */
sealed class PushSimulatorViewEvent {
    data class UpdateTitle(val title: String) : PushSimulatorViewEvent()
    data class UpdateBody(val body: String) : PushSimulatorViewEvent()
    data class UpdateChannelId(val channelId: String) : PushSimulatorViewEvent()
    data class UpdatePriority(val priority: NotificationPriority) : PushSimulatorViewEvent()
    data class UpdateNewActionTitle(val title: String) : PushSimulatorViewEvent()
    data class AddAction(val title: String) : PushSimulatorViewEvent()
    data class RemoveAction(val actionId: String) : PushSimulatorViewEvent()
    data object SendNotification : PushSimulatorViewEvent()
    data class SaveAsTemplate(val templateName: String) : PushSimulatorViewEvent()
    data class LoadTemplate(val template: NotificationTemplate) : PushSimulatorViewEvent()
    data class DeleteTemplate(val templateId: String) : PushSimulatorViewEvent()
    data class SendFromTemplate(val template: NotificationTemplate) : PushSimulatorViewEvent()
    data object ClearForm : PushSimulatorViewEvent()
}

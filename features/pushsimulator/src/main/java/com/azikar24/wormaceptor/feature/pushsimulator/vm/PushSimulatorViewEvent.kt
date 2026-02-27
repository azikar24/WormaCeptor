package com.azikar24.wormaceptor.feature.pushsimulator.vm

import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate

/**
 * User-initiated events dispatched to [PushSimulatorViewModel].
 */
sealed class PushSimulatorViewEvent {
    /**
     * Updates the notification title field.
     *
     * @property title New title text.
     */
    data class UpdateTitle(val title: String) : PushSimulatorViewEvent()

    /**
     * Updates the notification body field.
     *
     * @property body New body text.
     */
    data class UpdateBody(val body: String) : PushSimulatorViewEvent()

    /**
     * Selects a notification channel by its ID.
     *
     * @property channelId ID of the channel to select.
     */
    data class UpdateChannelId(val channelId: String) : PushSimulatorViewEvent()

    /**
     * Changes the notification priority level.
     *
     * @property priority New priority level.
     */
    data class UpdatePriority(val priority: NotificationPriority) : PushSimulatorViewEvent()

    /**
     * Updates the text for a new action button being composed.
     *
     * @property title Current text of the action being composed.
     */
    data class UpdateNewActionTitle(val title: String) : PushSimulatorViewEvent()

    /**
     * Adds an action button with the given title to the notification.
     *
     * @property title Label for the new action button.
     */
    data class AddAction(val title: String) : PushSimulatorViewEvent()

    /**
     * Removes an action button by its ID.
     *
     * @property actionId Unique identifier of the action to remove.
     */
    data class RemoveAction(val actionId: String) : PushSimulatorViewEvent()

    /** Sends the notification with the current form values. */
    data object SendNotification : PushSimulatorViewEvent()

    /**
     * Saves the current form values as a reusable template.
     *
     * @property templateName Name to assign to the saved template.
     */
    data class SaveAsTemplate(val templateName: String) : PushSimulatorViewEvent()

    /**
     * Loads a saved template into the form fields.
     *
     * @property template Template to load.
     */
    data class LoadTemplate(val template: NotificationTemplate) : PushSimulatorViewEvent()

    /**
     * Deletes a saved template by its ID.
     *
     * @property templateId Unique identifier of the template to delete.
     */
    data class DeleteTemplate(val templateId: String) : PushSimulatorViewEvent()

    /**
     * Sends a notification directly from a template without loading it first.
     *
     * @property template Template to send as a notification.
     */
    data class SendFromTemplate(val template: NotificationTemplate) : PushSimulatorViewEvent()

    /** Resets all form fields to their default empty values. */
    data object ClearForm : PushSimulatorViewEvent()
}

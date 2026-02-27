package com.azikar24.wormaceptor.feature.pushsimulator.vm

import com.azikar24.wormaceptor.domain.entities.NotificationAction
import com.azikar24.wormaceptor.domain.entities.NotificationChannelInfo
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Consolidated UI state for the Push Simulator screen.
 *
 * @property title Notification title entered in the form.
 * @property body Notification body text entered in the form.
 * @property selectedChannelId ID of the notification channel selected for delivery.
 * @property priority Priority level for the notification.
 * @property actions Action buttons attached to the notification.
 * @property newActionTitle Text of the action button currently being composed.
 * @property templates Saved notification templates available for reuse.
 * @property channels Available notification channels on the device.
 */
data class PushSimulatorViewState(
    val title: String = "",
    val body: String = "",
    val selectedChannelId: String = "",
    val priority: NotificationPriority = NotificationPriority.DEFAULT,
    val actions: ImmutableList<NotificationAction> = persistentListOf(),
    val newActionTitle: String = "",
    val templates: ImmutableList<NotificationTemplate> = persistentListOf(),
    val channels: ImmutableList<NotificationChannelInfo> = persistentListOf(),
)

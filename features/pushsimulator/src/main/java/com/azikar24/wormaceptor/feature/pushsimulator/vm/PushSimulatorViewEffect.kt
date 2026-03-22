package com.azikar24.wormaceptor.feature.pushsimulator.vm

/**
 * One-time side-effects emitted by [PushSimulatorViewModel] and consumed by the UI layer.
 */
sealed class PushSimulatorViewEffect {
    /** A notification was successfully sent. */
    data object NotificationSent : PushSimulatorViewEffect()

    /** A template was successfully saved. */
    data object TemplateSaved : PushSimulatorViewEffect()

    /** A template was successfully deleted. */
    data object TemplateDeleted : PushSimulatorViewEffect()

    /**
     * A template was loaded into the form.
     *
     * @property name Name of the template that was loaded.
     */
    data class TemplateLoaded(val name: String) : PushSimulatorViewEffect()

    /** The notification permission must be granted before sending. */
    data object PermissionRequired : PushSimulatorViewEffect()

    /**
     * An operation failed with the given message.
     *
     * @property message Human-readable description of the failure.
     */
    data class Error(val message: String) : PushSimulatorViewEffect()
}

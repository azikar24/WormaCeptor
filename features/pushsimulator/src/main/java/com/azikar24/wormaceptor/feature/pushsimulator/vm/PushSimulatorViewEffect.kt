package com.azikar24.wormaceptor.feature.pushsimulator.vm

/**
 * One-time side-effects emitted by [PushSimulatorViewModel] and consumed by the UI layer.
 */
sealed class PushSimulatorViewEffect {
    data object NotificationSent : PushSimulatorViewEffect()
    data object TemplateSaved : PushSimulatorViewEffect()
    data object TemplateDeleted : PushSimulatorViewEffect()
    data class TemplateLoaded(val name: String) : PushSimulatorViewEffect()
    data object PermissionRequired : PushSimulatorViewEffect()
    data class Error(val message: String) : PushSimulatorViewEffect()
}

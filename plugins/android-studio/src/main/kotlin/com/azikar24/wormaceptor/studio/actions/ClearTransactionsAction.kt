package com.azikar24.wormaceptor.studio.actions

import com.azikar24.wormaceptor.studio.service.WormaCeptorService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

/**
 * Action to clear all captured transactions on the device.
 */
class ClearTransactionsAction : AnAction(
    "Clear Transactions",
    "Clear all captured transactions",
    AllIcons.Actions.GC,
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = project.getService(WormaCeptorService::class.java)

        val result = Messages.showYesNoDialog(
            project,
            "Are you sure you want to clear all captured transactions?",
            "Clear Transactions",
            Messages.getQuestionIcon(),
        )

        if (result == Messages.YES) {
            service.clearTransactions { success ->
                if (!success) {
                    Messages.showErrorDialog(
                        project,
                        "Failed to clear transactions. Make sure the device is connected.",
                        "Error",
                    )
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabled = project != null &&
            project.getService(WormaCeptorService::class.java).isDeviceConnectedCached()
    }
}

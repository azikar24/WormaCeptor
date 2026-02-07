package com.azikar24.wormaceptor.studio.actions

import com.azikar24.wormaceptor.studio.service.WormaCeptorService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Action to refresh the transaction list from the device.
 */
class RefreshTransactionsAction : AnAction(
    "Refresh Transactions",
    "Refresh transaction list from device",
    AllIcons.Actions.Refresh,
) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = project.getService(WormaCeptorService::class.java)

        service.getTransactions { transactions ->
            // Transactions will be updated via the state listener
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}

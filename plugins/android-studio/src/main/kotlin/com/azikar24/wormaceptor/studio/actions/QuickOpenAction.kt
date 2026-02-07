package com.azikar24.wormaceptor.studio.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

/**
 * Action to quickly open the WormaCeptor tool window.
 * Bound to Ctrl+Shift+W keyboard shortcut.
 */
class QuickOpenAction : AnAction("Quick Open WormaCeptor", "Open WormaCeptor tool window", null) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("WormaCeptor")
        toolWindow?.show()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}

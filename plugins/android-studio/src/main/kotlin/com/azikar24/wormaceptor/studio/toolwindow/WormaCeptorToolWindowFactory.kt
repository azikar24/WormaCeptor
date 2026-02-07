package com.azikar24.wormaceptor.studio.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Factory for creating the WormaCeptor tool window.
 */
class WormaCeptorToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = WormaCeptorToolWindowPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "Transactions", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        // Only show for Android projects
        return true // Could check for Android facet here
    }
}

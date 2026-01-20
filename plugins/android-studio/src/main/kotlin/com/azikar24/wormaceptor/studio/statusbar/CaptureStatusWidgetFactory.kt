/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.studio.statusbar

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

/**
 * Factory for creating the WormaCeptor capture status widget.
 */
class CaptureStatusWidgetFactory : StatusBarWidgetFactory {

    override fun getId(): String = "WormaCeptorStatus"

    override fun getDisplayName(): String = "WormaCeptor Status"

    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget {
        return CaptureStatusWidget(project)
    }

    override fun disposeWidget(widget: StatusBarWidget) {
        // Cleanup if needed
    }

    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}

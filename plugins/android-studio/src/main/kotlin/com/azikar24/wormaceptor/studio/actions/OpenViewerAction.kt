/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.studio.actions

import com.azikar24.wormaceptor.studio.service.WormaCeptorService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Action to open the WormaCeptor viewer on the connected device.
 */
class OpenViewerAction : AnAction("Open WormaCeptor Viewer", "Open WormaCeptor viewer on device", null) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = project.getService(WormaCeptorService::class.java)
        service.openViewerOnDevice()
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        e.presentation.isEnabled = project != null &&
            project.getService(WormaCeptorService::class.java).isDeviceConnectedCached()
    }
}

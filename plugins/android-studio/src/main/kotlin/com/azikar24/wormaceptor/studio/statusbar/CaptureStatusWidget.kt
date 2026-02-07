package com.azikar24.wormaceptor.studio.statusbar

import com.azikar24.wormaceptor.studio.model.TransactionSummary
import com.azikar24.wormaceptor.studio.service.WormaCeptorService
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.Consumer
import java.awt.event.MouseEvent
import javax.swing.Icon

/**
 * Status bar widget showing WormaCeptor capture status.
 * Clicking the widget opens the WormaCeptor tool window.
 */
class CaptureStatusWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.IconPresentation {

    private var statusBar: StatusBar? = null
    private var isCapturing = false
    private var transactionCount = 0
    private var isConnected = false

    private val service: WormaCeptorService = project.getService(WormaCeptorService::class.java)

    private val stateListener = object : WormaCeptorService.StateListener {
        override fun onDeviceChanged(serial: String?) {
            isConnected = serial != null
            updateWidget()
        }

        override fun onTransactionsUpdated(transactions: List<TransactionSummary>) {
            transactionCount = transactions.size
            updateWidget()
        }

        override fun onCaptureStatusChanged(active: Boolean, count: Int) {
            isCapturing = active
            transactionCount = count
            updateWidget()
        }
    }

    init {
        service.addStateListener(stateListener)
        refreshStatus()
    }

    override fun ID(): String = "WormaCeptorStatus"

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun dispose() {
        service.removeStateListener(stateListener)
    }

    override fun getIcon(): Icon {
        return when {
            !isConnected -> AllIcons.General.Warning
            isCapturing -> AllIcons.Actions.Execute
            transactionCount > 0 -> AllIcons.General.InspectionsOK
            else -> AllIcons.General.Information
        }
    }

    override fun getTooltipText(): String {
        return when {
            !isConnected -> "WormaCeptor: No device connected"
            isCapturing -> "WormaCeptor: Capturing ($transactionCount transactions)"
            transactionCount > 0 -> "WormaCeptor: $transactionCount transactions"
            else -> "WormaCeptor: Ready"
        }
    }

    override fun getClickConsumer(): Consumer<MouseEvent> {
        return Consumer { _ ->
            openToolWindow()
        }
    }

    private fun refreshStatus() {
        isConnected = service.isDeviceConnected()
        service.isCaptureActive { active, count ->
            isCapturing = active
            transactionCount = count
            updateWidget()
        }
    }

    private fun updateWidget() {
        ApplicationManager.getApplication().invokeLater {
            statusBar?.updateWidget(ID())
        }
    }

    private fun openToolWindow() {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("WormaCeptor")
        toolWindow?.show()
    }
}

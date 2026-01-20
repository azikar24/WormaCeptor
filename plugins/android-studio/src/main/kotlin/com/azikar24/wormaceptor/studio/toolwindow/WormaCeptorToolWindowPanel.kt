/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.studio.toolwindow

import com.azikar24.wormaceptor.studio.model.TransactionSummary
import com.azikar24.wormaceptor.studio.service.WormaCeptorService
import com.azikar24.wormaceptor.studio.ui.TransactionDetailPanel
import com.azikar24.wormaceptor.studio.ui.TransactionListCellRenderer
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.CollectionListModel
import com.intellij.ui.JBColor
import com.intellij.ui.JBSplitter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Font
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.Timer
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * Main tool window panel for WormaCeptor.
 * Contains a split view with transaction list on the left and details on the right.
 */
class WormaCeptorToolWindowPanel(private val project: Project) : SimpleToolWindowPanel(true, true) {

    private val service: WormaCeptorService = project.getService(WormaCeptorService::class.java)

    private val transactionList = JBList<TransactionSummary>()
    private val listModel = CollectionListModel<TransactionSummary>()
    private val detailPanel = TransactionDetailPanel(project)
    private val searchField = SearchTextField()

    private val contentPanel = JPanel(CardLayout())
    private val loadingPanel = createLoadingPanel()
    private val emptyPanel = createEmptyPanel()
    private val errorPanel = createErrorPanel()
    private val mainPanel = createMainPanel()

    private var allTransactions = listOf<TransactionSummary>()
    private var filterText = ""
    private var isRefreshing = false
    private var selectedTransactionId: String? = null

    // Auto-refresh timer - 2 second interval
    private val refreshTimer = Timer(2000) { autoRefresh() }

    companion object {
        private const val CARD_LOADING = "loading"
        private const val CARD_EMPTY = "empty"
        private const val CARD_ERROR = "error"
        private const val CARD_MAIN = "main"
    }

    init {
        setupUI()
        setupToolbar()
        setupListeners()
        refreshTransactions()

        // Start auto-refresh
        refreshTimer.isRepeats = true
        refreshTimer.start()
    }

    fun dispose() {
        refreshTimer.stop()
    }

    private fun autoRefresh() {
        if (isRefreshing) return
        if (!service.isDeviceConnected()) return

        // Silent refresh without showing loading state
        service.getTransactions { transactions ->
            ApplicationManager.getApplication().invokeLater {
                if (transactions.isNotEmpty() || allTransactions.isNotEmpty()) {
                    updateTransactionList(transactions)
                    if (transactions.isNotEmpty()) {
                        showCard(CARD_MAIN)
                    }
                }
            }
        }
    }

    private fun setupUI() {
        // Configure list
        transactionList.model = listModel
        transactionList.cellRenderer = TransactionListCellRenderer()
        transactionList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        transactionList.emptyText.text = "No transactions"

        // Configure search
        searchField.textEditor.emptyText.text = "Filter by URL, method, or status..."

        // Setup content cards
        contentPanel.add(loadingPanel, CARD_LOADING)
        contentPanel.add(emptyPanel, CARD_EMPTY)
        contentPanel.add(errorPanel, CARD_ERROR)
        contentPanel.add(mainPanel, CARD_MAIN)

        setContent(contentPanel)
    }

    private fun createMainPanel(): JPanel {
        val panel = JPanel(BorderLayout())

        // Search bar at top
        val searchPanel = JPanel(BorderLayout())
        searchPanel.border = JBUI.Borders.empty(4)
        searchPanel.add(searchField, BorderLayout.CENTER)
        panel.add(searchPanel, BorderLayout.NORTH)

        // Splitter with list and detail
        val splitter = JBSplitter(false, 0.35f)
        splitter.firstComponent = JBScrollPane(transactionList)
        splitter.secondComponent = detailPanel

        panel.add(splitter, BorderLayout.CENTER)

        return panel
    }

    private fun createLoadingPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.background = UIUtil.getPanelBackground()

        val label = JBLabel("Loading transactions...")
        label.horizontalAlignment = JBLabel.CENTER
        label.foreground = JBColor.GRAY
        panel.add(label, BorderLayout.CENTER)

        return panel
    }

    private fun createEmptyPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.background = UIUtil.getPanelBackground()

        val centerPanel = JPanel()
        centerPanel.layout = java.awt.GridBagLayout()
        centerPanel.isOpaque = false

        val contentPanel = JPanel()
        contentPanel.layout = javax.swing.BoxLayout(contentPanel, javax.swing.BoxLayout.Y_AXIS)
        contentPanel.isOpaque = false

        val iconLabel = JBLabel(AllIcons.General.Information)
        iconLabel.alignmentX = JBLabel.CENTER_ALIGNMENT

        val titleLabel = JBLabel("No Transactions")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 14f)
        titleLabel.alignmentX = JBLabel.CENTER_ALIGNMENT
        titleLabel.border = JBUI.Borders.emptyTop(8)

        val messageLabel =
            JBLabel("<html><center>Connect a device and make network<br/>requests to see them here.</center></html>")
        messageLabel.foreground = JBColor.GRAY
        messageLabel.alignmentX = JBLabel.CENTER_ALIGNMENT
        messageLabel.border = JBUI.Borders.emptyTop(4)

        contentPanel.add(iconLabel)
        contentPanel.add(titleLabel)
        contentPanel.add(messageLabel)

        centerPanel.add(contentPanel)
        panel.add(centerPanel, BorderLayout.CENTER)

        return panel
    }

    private fun createErrorPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.background = UIUtil.getPanelBackground()

        val centerPanel = JPanel()
        centerPanel.layout = java.awt.GridBagLayout()
        centerPanel.isOpaque = false

        val contentPanel = JPanel()
        contentPanel.layout = javax.swing.BoxLayout(contentPanel, javax.swing.BoxLayout.Y_AXIS)
        contentPanel.isOpaque = false

        val iconLabel = JBLabel(AllIcons.General.Error)
        iconLabel.alignmentX = JBLabel.CENTER_ALIGNMENT

        val titleLabel = JBLabel("Connection Error")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 14f)
        titleLabel.alignmentX = JBLabel.CENTER_ALIGNMENT
        titleLabel.border = JBUI.Borders.emptyTop(8)

        val messageLabel =
            JBLabel(
                "<html><center>Could not connect to device.<br/>Make sure ADB is configured correctly.</center></html>",
            )
        messageLabel.foreground = JBColor.GRAY
        messageLabel.alignmentX = JBLabel.CENTER_ALIGNMENT
        messageLabel.border = JBUI.Borders.emptyTop(4)

        contentPanel.add(iconLabel)
        contentPanel.add(titleLabel)
        contentPanel.add(messageLabel)

        centerPanel.add(contentPanel)
        panel.add(centerPanel, BorderLayout.CENTER)

        return panel
    }

    private fun setupToolbar() {
        val actionGroup = DefaultActionGroup()

        // Refresh action
        actionGroup.add(
            object : AnAction("Refresh", "Refresh transaction list", AllIcons.Actions.Refresh) {
                override fun actionPerformed(e: AnActionEvent) {
                    refreshTransactions()
                }
            },
        )

        // Clear action
        actionGroup.add(
            object : AnAction("Clear", "Clear all transactions", AllIcons.Actions.GC) {
                override fun actionPerformed(e: AnActionEvent) {
                    clearTransactions()
                }
            },
        )

        actionGroup.addSeparator()

        // Open on device action
        actionGroup.add(
            object : AnAction(
                "Open on Device",
                "Open WormaCeptor viewer on device",
                AllIcons.Debugger.AttachToProcess,
            ) {
                override fun actionPerformed(e: AnActionEvent) {
                    service.openViewerOnDevice()
                }
            },
        )

        val toolbar = ActionManager.getInstance().createActionToolbar(
            ActionPlaces.TOOLWINDOW_TITLE,
            actionGroup,
            true,
        )
        toolbar.targetComponent = this

        setToolbar(toolbar.component)
    }

    private fun setupListeners() {
        // List selection listener
        transactionList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                val selected = transactionList.selectedValue
                if (selected != null && selected.id != selectedTransactionId) {
                    selectedTransactionId = selected.id
                    loadTransactionDetail(selected)
                }
            }
        }

        // Search listener
        searchField.addDocumentListener(
            object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) = updateFilter()
                override fun removeUpdate(e: DocumentEvent?) = updateFilter()
                override fun changedUpdate(e: DocumentEvent?) = updateFilter()
            },
        )

        // Service state listener
        service.addStateListener(
            object : WormaCeptorService.StateListener {
                override fun onDeviceChanged(serial: String?) {
                    refreshTransactions()
                }

                override fun onTransactionsUpdated(transactions: List<TransactionSummary>) {
                    ApplicationManager.getApplication().invokeLater {
                        updateTransactionList(transactions)
                    }
                }

                override fun onCaptureStatusChanged(active: Boolean, count: Int) {
                    // Update status bar widget
                }
            },
        )
    }

    private fun refreshTransactions() {
        if (isRefreshing) return
        isRefreshing = true
        showCard(CARD_LOADING)

        service.getTransactions { transactions ->
            ApplicationManager.getApplication().invokeLater {
                isRefreshing = false
                if (transactions.isEmpty()) {
                    if (!service.isDeviceConnected()) {
                        showCard(CARD_ERROR)
                    } else {
                        showCard(CARD_EMPTY)
                    }
                } else {
                    updateTransactionList(transactions)
                    showCard(CARD_MAIN)
                }
            }
        }
    }

    private fun clearTransactions() {
        service.clearTransactions { success ->
            if (success) {
                refreshTransactions()
            }
        }
    }

    private fun updateTransactionList(transactions: List<TransactionSummary>) {
        allTransactions = transactions
        applyFilter()
    }

    private fun updateFilter() {
        filterText = searchField.text.lowercase()
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = if (filterText.isBlank()) {
            allTransactions
        } else {
            allTransactions.filter { tx ->
                tx.path.lowercase().contains(filterText) ||
                    tx.host.lowercase().contains(filterText) ||
                    tx.method.lowercase().contains(filterText) ||
                    tx.statusDisplay.lowercase().contains(filterText)
            }
        }

        listModel.replaceAll(filtered)

        // Restore selection if the previously selected transaction is still in the list
        selectedTransactionId?.let { id ->
            val index = filtered.indexOfFirst { it.id == id }
            if (index >= 0) {
                transactionList.selectedIndex = index
            }
        }

        if (filtered.isEmpty() && allTransactions.isNotEmpty()) {
            transactionList.emptyText.text = "No matching transactions"
        } else {
            transactionList.emptyText.text = "No transactions"
        }
    }

    private fun loadTransactionDetail(summary: TransactionSummary) {
        detailPanel.showLoading()

        service.getTransactionDetail(summary.id) { detail ->
            ApplicationManager.getApplication().invokeLater {
                if (detail != null) {
                    detailPanel.showDetail(detail)
                } else {
                    // Show basic info from summary
                    detailPanel.showSummaryOnly(summary)
                }
            }
        }
    }

    private fun showCard(cardName: String) {
        (contentPanel.layout as CardLayout).show(contentPanel, cardName)
    }
}

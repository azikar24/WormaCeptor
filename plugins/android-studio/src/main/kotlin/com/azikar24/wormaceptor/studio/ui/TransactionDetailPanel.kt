/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.studio.ui

import com.azikar24.wormaceptor.studio.model.TransactionDetail
import com.azikar24.wormaceptor.studio.model.TransactionStatus
import com.azikar24.wormaceptor.studio.model.TransactionSummary
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Color
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.text.SimpleDateFormat
import java.util.Date
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder

/**
 * Panel for displaying detailed transaction information.
 * Shows overview, request headers/body, and response headers/body in tabs.
 */
class TransactionDetailPanel(private val project: Project) : JPanel(CardLayout()) {

    private val emptyPanel = createEmptyPanel()
    private val loadingPanel = createLoadingPanel()
    private val contentPanel = createContentPanel()

    private val overviewPanel = OverviewPanel()
    private val requestPanel = RequestResponsePanel("Request")
    private val responsePanel = RequestResponsePanel("Response")

    private val tabbedPane = JBTabbedPane()

    companion object {
        private const val CARD_EMPTY = "empty"
        private const val CARD_LOADING = "loading"
        private const val CARD_CONTENT = "content"
    }

    init {
        setupUI()
        showCard(CARD_EMPTY)
    }

    private fun setupUI() {
        // Setup tabbed pane
        tabbedPane.addTab("Overview", AllIcons.General.Information, overviewPanel)
        tabbedPane.addTab("Request", AllIcons.Actions.Upload, requestPanel)
        tabbedPane.addTab("Response", AllIcons.Actions.Download, responsePanel)

        contentPanel.add(tabbedPane, BorderLayout.CENTER)

        // Add cards
        add(emptyPanel, CARD_EMPTY)
        add(loadingPanel, CARD_LOADING)
        add(contentPanel, CARD_CONTENT)
    }

    private fun createEmptyPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.background = UIUtil.getPanelBackground()

        val label = JBLabel("Select a transaction to view details")
        label.horizontalAlignment = SwingConstants.CENTER
        label.foreground = JBColor.GRAY
        panel.add(label, BorderLayout.CENTER)

        return panel
    }

    private fun createLoadingPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.background = UIUtil.getPanelBackground()

        val label = JBLabel("Loading details...")
        label.horizontalAlignment = SwingConstants.CENTER
        label.foreground = JBColor.GRAY
        panel.add(label, BorderLayout.CENTER)

        return panel
    }

    private fun createContentPanel(): JPanel {
        return JPanel(BorderLayout())
    }

    fun showLoading() {
        showCard(CARD_LOADING)
    }

    fun clearDetail() {
        showCard(CARD_EMPTY)
    }

    fun showDetail(detail: TransactionDetail) {
        overviewPanel.setData(detail)
        requestPanel.setData(
            headers = detail.requestHeaders,
            body = detail.requestBody,
            size = detail.summary.requestSize
        )
        responsePanel.setData(
            headers = detail.responseHeaders,
            body = detail.responseBody,
            size = detail.summary.responseSize
        )
        showCard(CARD_CONTENT)
    }

    fun showSummaryOnly(summary: TransactionSummary) {
        overviewPanel.setBasicData(summary)
        requestPanel.setData(emptyMap(), null, summary.requestSize)
        responsePanel.setData(emptyMap(), null, summary.responseSize)
        showCard(CARD_CONTENT)
    }

    private fun showCard(cardName: String) {
        (layout as CardLayout).show(this, cardName)
    }

    /**
     * Panel showing transaction overview information.
     */
    private inner class OverviewPanel : JPanel(BorderLayout()) {

        private val urlLabel = JBLabel()
        private val methodLabel = JBLabel()
        private val statusLabel = JBLabel()
        private val protocolLabel = JBLabel()
        private val durationLabel = JBLabel()
        private val timestampLabel = JBLabel()
        private val requestSizeLabel = JBLabel()
        private val responseSizeLabel = JBLabel()
        private val tlsLabel = JBLabel()
        private val errorLabel = JBLabel()

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

        init {
            setupUI()
        }

        private fun setupUI() {
            border = JBUI.Borders.empty(12)

            val gridPanel = JPanel(GridBagLayout())
            gridPanel.isOpaque = false

            val gbc = GridBagConstraints()
            gbc.anchor = GridBagConstraints.NORTHWEST
            gbc.insets = JBUI.insets(4, 0)

            var row = 0

            // URL
            addRow(gridPanel, gbc, row++, "URL:", urlLabel)

            // Method & Status
            val methodStatusPanel = JPanel(BorderLayout(8, 0))
            methodStatusPanel.isOpaque = false
            methodStatusPanel.add(methodLabel, BorderLayout.WEST)
            methodStatusPanel.add(statusLabel, BorderLayout.CENTER)
            addRow(gridPanel, gbc, row++, "Method:", methodStatusPanel)

            // Protocol
            addRow(gridPanel, gbc, row++, "Protocol:", protocolLabel)

            // TLS Version
            addRow(gridPanel, gbc, row++, "TLS:", tlsLabel)

            // Duration
            addRow(gridPanel, gbc, row++, "Duration:", durationLabel)

            // Timestamp
            addRow(gridPanel, gbc, row++, "Timestamp:", timestampLabel)

            // Request Size
            addRow(gridPanel, gbc, row++, "Request Size:", requestSizeLabel)

            // Response Size
            addRow(gridPanel, gbc, row++, "Response Size:", responseSizeLabel)

            // Error (if any)
            addRow(gridPanel, gbc, row, "Error:", errorLabel)

            add(JBScrollPane(gridPanel), BorderLayout.CENTER)
        }

        private fun addRow(panel: JPanel, gbc: GridBagConstraints, row: Int, labelText: String, valueComponent: javax.swing.JComponent) {
            gbc.gridx = 0
            gbc.gridy = row
            gbc.weightx = 0.0
            gbc.fill = GridBagConstraints.NONE
            gbc.insets = JBUI.insets(4, 0, 4, 12)

            val label = JBLabel(labelText)
            label.foreground = JBColor.GRAY
            label.font = label.font.deriveFont(Font.BOLD)
            panel.add(label, gbc)

            gbc.gridx = 1
            gbc.weightx = 1.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            gbc.insets = JBUI.insets(4, 0)
            panel.add(valueComponent, gbc)
        }

        fun setData(detail: TransactionDetail) {
            val summary = detail.summary

            urlLabel.text = summary.fullUrl
            methodLabel.text = summary.method
            methodLabel.foreground = getMethodColor(summary.method)

            statusLabel.text = summary.statusDisplay
            statusLabel.foreground = getStatusColor(summary)

            protocolLabel.text = detail.protocol ?: "-"
            tlsLabel.text = detail.tlsVersion ?: "-"
            durationLabel.text = summary.durationDisplay
            timestampLabel.text = dateFormat.format(Date(summary.timestamp))
            requestSizeLabel.text = formatSize(summary.requestSize)
            responseSizeLabel.text = formatSize(summary.responseSize)

            if (detail.error != null) {
                errorLabel.text = detail.error
                errorLabel.foreground = JBColor.RED
                errorLabel.isVisible = true
            } else {
                errorLabel.isVisible = false
            }
        }

        fun setBasicData(summary: TransactionSummary) {
            urlLabel.text = summary.fullUrl
            methodLabel.text = summary.method
            methodLabel.foreground = getMethodColor(summary.method)

            statusLabel.text = summary.statusDisplay
            statusLabel.foreground = getStatusColor(summary)

            protocolLabel.text = "-"
            tlsLabel.text = "-"
            durationLabel.text = summary.durationDisplay
            timestampLabel.text = dateFormat.format(Date(summary.timestamp))
            requestSizeLabel.text = formatSize(summary.requestSize)
            responseSizeLabel.text = formatSize(summary.responseSize)
            errorLabel.isVisible = false
        }

        private fun getMethodColor(method: String): Color {
            return when (method.uppercase()) {
                "GET" -> JBColor(Color(0x4A90D9), Color(0x6CB0F0))
                "POST" -> JBColor(Color(0x4DB6AC), Color(0x80CBC4))
                "PUT", "PATCH" -> JBColor(Color(0xFFB74D), Color(0xFFCC80))
                "DELETE" -> JBColor(Color(0xE57373), Color(0xEF9A9A))
                else -> UIUtil.getLabelForeground()
            }
        }

        private fun getStatusColor(transaction: TransactionSummary): Color {
            return when {
                transaction.status == TransactionStatus.ACTIVE -> JBColor.GRAY
                transaction.status == TransactionStatus.FAILED -> JBColor.RED
                transaction.isSuccess -> JBColor(Color(0x81C784), Color(0xA5D6A7))
                transaction.isRedirect -> JBColor(Color(0x64B5F6), Color(0x90CAF9))
                transaction.isClientError -> JBColor(Color(0xFFB74D), Color(0xFFCC80))
                transaction.isServerError -> JBColor.RED
                else -> UIUtil.getLabelForeground()
            }
        }

        private fun formatSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            }
        }
    }

    /**
     * Panel showing request or response headers and body.
     */
    private inner class RequestResponsePanel(title: String) : JPanel(BorderLayout()) {

        private val headersArea = JBTextArea()
        private val bodyArea = JBTextArea()
        private val sizeLabel = JBLabel()

        init {
            setupUI(title)
        }

        private fun setupUI(title: String) {
            border = JBUI.Borders.empty(8)

            // Configure text areas
            headersArea.isEditable = false
            headersArea.font = Font(Font.MONOSPACED, Font.PLAIN, 12)
            headersArea.border = EmptyBorder(4, 4, 4, 4)

            bodyArea.isEditable = false
            bodyArea.font = Font(Font.MONOSPACED, Font.PLAIN, 12)
            bodyArea.lineWrap = true
            bodyArea.wrapStyleWord = true
            bodyArea.border = EmptyBorder(4, 4, 4, 4)

            // Inner tabbed pane for headers/body
            val innerTabs = JBTabbedPane()

            // Headers panel
            val headersPanel = JPanel(BorderLayout())
            headersPanel.add(JBScrollPane(headersArea), BorderLayout.CENTER)
            innerTabs.addTab("Headers", headersPanel)

            // Body panel
            val bodyPanel = JPanel(BorderLayout())
            bodyPanel.add(JBScrollPane(bodyArea), BorderLayout.CENTER)

            // Size info at bottom
            val sizePanel = JPanel(BorderLayout())
            sizePanel.border = JBUI.Borders.empty(4)
            sizeLabel.foreground = JBColor.GRAY
            sizePanel.add(sizeLabel, BorderLayout.WEST)
            bodyPanel.add(sizePanel, BorderLayout.SOUTH)

            innerTabs.addTab("Body", bodyPanel)

            // Pre-select Body tab by default
            innerTabs.selectedIndex = 1

            add(innerTabs, BorderLayout.CENTER)
        }

        fun setData(headers: Map<String, List<String>>, body: String?, size: Long) {
            // Format headers
            val headersText = buildString {
                for ((key, values) in headers) {
                    for (value in values) {
                        appendLine("$key: $value")
                    }
                }
            }
            headersArea.text = headersText.ifBlank { "(No headers)" }
            headersArea.caretPosition = 0

            // Format body
            if (body.isNullOrBlank()) {
                bodyArea.text = "(No body)"
            } else {
                // Try to format JSON
                bodyArea.text = tryFormatJson(body)
            }
            bodyArea.caretPosition = 0

            // Update size label
            sizeLabel.text = "Size: ${formatSize(size)}"
        }

        private fun tryFormatJson(text: String): String {
            return try {
                val trimmed = text.trim()
                if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                    formatJson(trimmed)
                } else {
                    text
                }
            } catch (e: Exception) {
                text
            }
        }

        private fun formatJson(json: String): String {
            val result = StringBuilder()
            var indent = 0
            var inString = false
            var escape = false

            for (char in json) {
                when {
                    escape -> {
                        result.append(char)
                        escape = false
                    }
                    char == '\\' -> {
                        result.append(char)
                        escape = true
                    }
                    char == '"' -> {
                        result.append(char)
                        inString = !inString
                    }
                    inString -> result.append(char)
                    char == '{' || char == '[' -> {
                        result.append(char)
                        result.appendLine()
                        indent++
                        result.append("  ".repeat(indent))
                    }
                    char == '}' || char == ']' -> {
                        result.appendLine()
                        indent--
                        result.append("  ".repeat(indent))
                        result.append(char)
                    }
                    char == ',' -> {
                        result.append(char)
                        result.appendLine()
                        result.append("  ".repeat(indent))
                    }
                    char == ':' -> result.append(": ")
                    !char.isWhitespace() -> result.append(char)
                }
            }

            return result.toString()
        }

        private fun formatSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            }
        }
    }
}

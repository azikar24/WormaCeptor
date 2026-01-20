/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.studio.ui

import com.azikar24.wormaceptor.studio.model.TransactionStatus
import com.azikar24.wormaceptor.studio.model.TransactionSummary
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListCellRenderer

/**
 * Custom cell renderer for transaction list items.
 */
class TransactionListCellRenderer : ListCellRenderer<TransactionSummary> {

    private val panel = JPanel(BorderLayout())
    private val methodLabel = JLabel()
    private val urlLabel = JLabel()
    private val statusLabel = JLabel()
    private val durationLabel = JLabel()
    private val topRow = JPanel(BorderLayout())
    private val bottomRow = JPanel(BorderLayout())

    init {
        panel.border = JBUI.Borders.empty(6, 8)

        methodLabel.font = methodLabel.font.deriveFont(Font.BOLD, 12f)
        urlLabel.font = urlLabel.font.deriveFont(11f)
        statusLabel.font = statusLabel.font.deriveFont(11f)
        durationLabel.font = durationLabel.font.deriveFont(11f)

        topRow.add(methodLabel, BorderLayout.WEST)
        topRow.add(urlLabel, BorderLayout.CENTER)
        topRow.isOpaque = false

        bottomRow.add(statusLabel, BorderLayout.WEST)
        bottomRow.add(durationLabel, BorderLayout.EAST)
        bottomRow.isOpaque = false
        bottomRow.border = JBUI.Borders.emptyTop(2)

        panel.add(topRow, BorderLayout.NORTH)
        panel.add(bottomRow, BorderLayout.SOUTH)
    }

    override fun getListCellRendererComponent(
        list: JList<out TransactionSummary>?,
        value: TransactionSummary?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean,
    ): Component {
        if (value == null) return panel

        // Method with color
        methodLabel.text = "[${value.method}] "
        methodLabel.foreground = getMethodColor(value.method)

        // URL
        urlLabel.text = value.url

        // Status with color
        statusLabel.text = value.statusText
        statusLabel.foreground = getStatusColor(value)

        // Duration
        durationLabel.text = value.durationText
        durationLabel.foreground = JBColor.GRAY

        // Selection colors
        if (isSelected) {
            panel.background = list?.selectionBackground
            urlLabel.foreground = list?.selectionForeground
        } else {
            panel.background = list?.background
            urlLabel.foreground = list?.foreground
        }

        return panel
    }

    private fun getMethodColor(method: String): JBColor {
        return when (method.uppercase()) {
            "GET" -> JBColor(0x61AFEF, 0x61AFEF)
            "POST" -> JBColor(0x98C379, 0x98C379)
            "PUT" -> JBColor(0xE5C07B, 0xE5C07B)
            "DELETE" -> JBColor(0xE06C75, 0xE06C75)
            "PATCH" -> JBColor(0xC678DD, 0xC678DD)
            else -> JBColor.GRAY
        }
    }

    private fun getStatusColor(transaction: TransactionSummary): JBColor {
        return when {
            transaction.status == TransactionStatus.ACTIVE -> JBColor.BLUE
            transaction.status == TransactionStatus.FAILED -> JBColor.RED
            transaction.code == null -> JBColor.GRAY
            transaction.code in 200..299 -> JBColor(0x98C379, 0x98C379)
            transaction.code in 300..399 -> JBColor(0xE5C07B, 0xE5C07B)
            transaction.code in 400..499 -> JBColor(0xE5C07B, 0xE5C07B)
            transaction.code >= 500 -> JBColor(0xE06C75, 0xE06C75)
            else -> JBColor.GRAY
        }
    }
}

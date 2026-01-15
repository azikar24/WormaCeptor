/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.studio.service

import com.azikar24.wormaceptor.studio.model.TransactionDetail
import com.azikar24.wormaceptor.studio.model.TransactionSummary
import com.intellij.openapi.project.Project

/**
 * Service interface for WormaCeptor IDE integration.
 * Provides communication with connected Android device via ADB.
 */
interface WormaCeptorService {

    /**
     * Get the current project.
     */
    val project: Project

    /**
     * Check if a device is connected.
     */
    fun isDeviceConnected(): Boolean

    /**
     * Get list of connected device serial numbers.
     */
    fun getConnectedDevices(): List<String>

    /**
     * Get currently selected device serial, or null if none.
     */
    fun getSelectedDevice(): String?

    /**
     * Select a device by serial number.
     */
    fun selectDevice(serial: String)

    /**
     * Get transactions from the connected device.
     */
    fun getTransactions(callback: (List<TransactionSummary>) -> Unit)

    /**
     * Get details of a specific transaction.
     */
    fun getTransactionDetail(id: String, callback: (TransactionDetail?) -> Unit)

    /**
     * Open the WormaCeptor viewer on the device.
     */
    fun openViewerOnDevice()

    /**
     * Clear all transactions on the device.
     */
    fun clearTransactions(callback: (Boolean) -> Unit)

    /**
     * Check if capture is currently active on the device.
     */
    fun isCaptureActive(callback: (Boolean, Int) -> Unit)

    /**
     * Add a listener for service state changes.
     */
    fun addStateListener(listener: StateListener)

    /**
     * Remove a state listener.
     */
    fun removeStateListener(listener: StateListener)

    /**
     * Listener for service state changes.
     */
    interface StateListener {
        fun onDeviceChanged(serial: String?)
        fun onTransactionsUpdated(transactions: List<TransactionSummary>)
        fun onCaptureStatusChanged(active: Boolean, count: Int)
    }
}

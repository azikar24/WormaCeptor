/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.studio.service

import com.azikar24.wormaceptor.studio.model.TransactionDetail
import com.azikar24.wormaceptor.studio.model.TransactionStatus
import com.azikar24.wormaceptor.studio.model.TransactionSummary
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

/**
 * Implementation of WormaCeptorService using ADB commands.
 */
class WormaCeptorServiceImpl(override val project: Project) : WormaCeptorService {

    private val log = Logger.getInstance(WormaCeptorServiceImpl::class.java)
    private val gson = Gson()
    private val listeners = CopyOnWriteArrayList<WormaCeptorService.StateListener>()
    private var selectedDeviceSerial: String? = null
    private var cachedTransactions = listOf<TransactionSummary>()
    private var captureActive = false
    private var transactionCount = 0

    override fun isDeviceConnected(): Boolean {
        return getConnectedDevices().isNotEmpty()
    }

    override fun getConnectedDevices(): List<String> {
        return try {
            val result = executeAdbCommand("devices")
            result.lines()
                .drop(1) // Skip "List of devices attached"
                .filter { it.contains("\tdevice") }
                .map { it.split("\t").first() }
        } catch (e: Exception) {
            log.warn("Failed to get connected devices", e)
            emptyList()
        }
    }

    override fun getSelectedDevice(): String? {
        val devices = getConnectedDevices()
        return when {
            selectedDeviceSerial != null && devices.contains(selectedDeviceSerial) -> selectedDeviceSerial
            devices.size == 1 -> devices.first().also { selectedDeviceSerial = it }
            else -> null
        }
    }

    override fun selectDevice(serial: String) {
        if (getConnectedDevices().contains(serial)) {
            selectedDeviceSerial = serial
            notifyDeviceChanged(serial)
        }
    }

    override fun getTransactions(callback: (List<TransactionSummary>) -> Unit) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val transactions = fetchTransactionsFromDevice()
            cachedTransactions = transactions
            ApplicationManager.getApplication().invokeLater {
                callback(transactions)
                notifyTransactionsUpdated(transactions)
            }
        }
    }

    override fun getTransactionDetail(id: String, callback: (TransactionDetail?) -> Unit) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val detail = fetchTransactionDetailFromDevice(id)
            ApplicationManager.getApplication().invokeLater {
                callback(detail)
            }
        }
    }

    override fun openViewerOnDevice() {
        val device = getSelectedDevice() ?: return
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                executeAdbCommand(
                    "-s", device,
                    "shell", "am", "start",
                    "-n", "com.azikar24.wormaceptor/.feature.viewer.ViewerActivity"
                )
            } catch (e: Exception) {
                log.warn("Failed to open viewer on device", e)
            }
        }
    }

    override fun clearTransactions(callback: (Boolean) -> Unit) {
        val device = getSelectedDevice()
        if (device == null) {
            callback(false)
            return
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            val success = try {
                executeAdbCommand(
                    "-s", device,
                    "shell", "content", "delete",
                    "--uri", "content://com.azikar24.wormaceptor.provider/transactions"
                )
                true
            } catch (e: Exception) {
                log.warn("Failed to clear transactions", e)
                false
            }

            ApplicationManager.getApplication().invokeLater {
                if (success) {
                    cachedTransactions = emptyList()
                    notifyTransactionsUpdated(emptyList())
                }
                callback(success)
            }
        }
    }

    override fun isCaptureActive(callback: (Boolean, Int) -> Unit) {
        val device = getSelectedDevice()
        if (device == null) {
            callback(false, 0)
            return
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val result = executeAdbCommand(
                    "-s", device,
                    "shell", "content", "query",
                    "--uri", "content://com.azikar24.wormaceptor.provider/status"
                )
                val active = result.contains("capturing=true")
                val count = Regex("count=(\\d+)").find(result)?.groupValues?.get(1)?.toIntOrNull() ?: 0

                captureActive = active
                transactionCount = count

                ApplicationManager.getApplication().invokeLater {
                    callback(active, count)
                    notifyCaptureStatusChanged(active, count)
                }
            } catch (e: Exception) {
                log.warn("Failed to get capture status", e)
                ApplicationManager.getApplication().invokeLater {
                    callback(false, 0)
                }
            }
        }
    }

    override fun addStateListener(listener: WormaCeptorService.StateListener) {
        listeners.add(listener)
    }

    override fun removeStateListener(listener: WormaCeptorService.StateListener) {
        listeners.remove(listener)
    }

    private fun fetchTransactionsFromDevice(): List<TransactionSummary> {
        val device = getSelectedDevice() ?: return emptyList()

        return try {
            val result = executeAdbCommand(
                "-s", device,
                "shell", "content", "query",
                "--uri", "content://com.azikar24.wormaceptor.provider/transactions"
            )
            parseTransactionsOutput(result)
        } catch (e: Exception) {
            log.warn("Failed to fetch transactions", e)
            emptyList()
        }
    }

    private fun fetchTransactionDetailFromDevice(id: String): TransactionDetail? {
        val device = getSelectedDevice() ?: return null

        return try {
            val result = executeAdbCommand(
                "-s", device,
                "shell", "content", "read",
                "--uri", "content://com.azikar24.wormaceptor.provider/transaction/$id"
            )
            parseTransactionDetail(result)
        } catch (e: Exception) {
            log.warn("Failed to fetch transaction detail", e)
            null
        }
    }

    private fun parseTransactionsOutput(output: String): List<TransactionSummary> {
        // Parse content provider query output format
        // Row: 0 id=xxx, method=GET, host=api.example.com, path=/users, ...
        val transactions = mutableListOf<TransactionSummary>()

        for (line in output.lines()) {
            if (!line.startsWith("Row:")) continue

            try {
                val fields = parseContentProviderRow(line)
                val transaction = TransactionSummary(
                    id = fields["id"] ?: continue,
                    method = fields["method"] ?: "GET",
                    host = fields["host"] ?: "",
                    path = fields["path"] ?: "/",
                    code = fields["code"]?.toIntOrNull(),
                    tookMs = fields["duration"]?.toLongOrNull(),
                    hasRequestBody = fields["has_request_body"]?.toBoolean() ?: false,
                    hasResponseBody = fields["has_response_body"]?.toBoolean() ?: false,
                    status = parseTransactionStatus(fields["status"]),
                    timestamp = fields["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis(),
                    contentType = fields["content_type"],
                    requestSize = fields["request_size"]?.toLongOrNull() ?: 0,
                    responseSize = fields["response_size"]?.toLongOrNull() ?: 0
                )
                transactions.add(transaction)
            } catch (e: Exception) {
                log.debug("Failed to parse transaction row: $line", e)
            }
        }

        return transactions.sortedByDescending { it.timestamp }
    }

    private fun parseContentProviderRow(line: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        // Format: Row: 0 key=value, key2=value2, ...
        val content = line.substringAfter("Row:").substringAfter(" ").trim()
        for (pair in content.split(", ")) {
            val (key, value) = pair.split("=", limit = 2).let {
                if (it.size == 2) it[0] to it[1] else continue
            }
            result[key] = value
        }
        return result
    }

    private fun parseTransactionStatus(status: String?): TransactionStatus {
        return when (status?.uppercase()) {
            "ACTIVE" -> TransactionStatus.ACTIVE
            "COMPLETED" -> TransactionStatus.COMPLETED
            "FAILED" -> TransactionStatus.FAILED
            else -> TransactionStatus.COMPLETED
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseTransactionDetail(output: String): TransactionDetail? {
        return try {
            // Assuming JSON output from content provider
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any> = gson.fromJson(output, type)

            val summary = TransactionSummary(
                id = data["id"]?.toString() ?: return null,
                method = data["method"]?.toString() ?: "GET",
                host = data["host"]?.toString() ?: "",
                path = data["path"]?.toString() ?: "/",
                code = (data["code"] as? Number)?.toInt(),
                tookMs = (data["duration"] as? Number)?.toLong(),
                hasRequestBody = data["has_request_body"] as? Boolean ?: false,
                hasResponseBody = data["has_response_body"] as? Boolean ?: false,
                status = parseTransactionStatus(data["status"]?.toString()),
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                contentType = data["content_type"]?.toString(),
                requestSize = (data["request_size"] as? Number)?.toLong() ?: 0,
                responseSize = (data["response_size"] as? Number)?.toLong() ?: 0
            )

            TransactionDetail(
                summary = summary,
                requestHeaders = (data["request_headers"] as? Map<String, List<String>>) ?: emptyMap(),
                requestBody = data["request_body"]?.toString(),
                responseHeaders = (data["response_headers"] as? Map<String, List<String>>) ?: emptyMap(),
                responseBody = data["response_body"]?.toString(),
                responseMessage = data["response_message"]?.toString(),
                protocol = data["protocol"]?.toString(),
                tlsVersion = data["tls_version"]?.toString(),
                error = data["error"]?.toString()
            )
        } catch (e: Exception) {
            log.warn("Failed to parse transaction detail", e)
            null
        }
    }

    private fun executeAdbCommand(vararg args: String): String {
        val adbPath = findAdbPath()
        val command = listOf(adbPath) + args.toList()

        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        val output = StringBuilder()
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            reader.lineSequence().forEach { line ->
                output.appendLine(line)
            }
        }

        val completed = process.waitFor(10, TimeUnit.SECONDS)
        if (!completed) {
            process.destroyForcibly()
            throw RuntimeException("ADB command timed out")
        }

        return output.toString()
    }

    private fun findAdbPath(): String {
        // Try to find ADB from Android SDK
        val androidHome = System.getenv("ANDROID_HOME")
            ?: System.getenv("ANDROID_SDK_ROOT")
            ?: System.getProperty("user.home") + "/Android/Sdk"

        val adbPaths = listOf(
            "$androidHome/platform-tools/adb",
            "$androidHome/platform-tools/adb.exe",
            "adb" // Fall back to PATH
        )

        for (path in adbPaths) {
            if (java.io.File(path).exists() || path == "adb") {
                return path
            }
        }

        return "adb"
    }

    private fun notifyDeviceChanged(serial: String?) {
        listeners.forEach { it.onDeviceChanged(serial) }
    }

    private fun notifyTransactionsUpdated(transactions: List<TransactionSummary>) {
        listeners.forEach { it.onTransactionsUpdated(transactions) }
    }

    private fun notifyCaptureStatusChanged(active: Boolean, count: Int) {
        listeners.forEach { it.onCaptureStatusChanged(active, count) }
    }
}

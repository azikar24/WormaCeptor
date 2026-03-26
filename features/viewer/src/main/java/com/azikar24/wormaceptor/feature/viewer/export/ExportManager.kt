package com.azikar24.wormaceptor.feature.viewer.export

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.core.ui.util.MAX_CLIPBOARD_SIZE
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.feature.viewer.ui.util.cleanupStaleFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Handles exporting network transactions as JSON files via the system share sheet. */
class ExportManager(
    private val context: Context,
    private val queryEngine: QueryEngine?,
    private val onMessage: (String) -> Unit = {},
) {

    /** Exports the given transactions as a JSON file and opens the system share sheet. */
    suspend fun exportTransactions(transactions: List<NetworkTransaction>) {
        withContext(Dispatchers.Main) {
            onMessage("Preparing export...")
        }

        withContext(Dispatchers.IO) {
            try {
                cleanupStaleFiles(File(context.cacheDir, "shared_bodies"))

                val jsonArray = serializeTransactions(transactions)
                val jsonContent = jsonArray.toString(2)

                if (jsonContent.length > MAX_CLIPBOARD_SIZE) {
                    shareAsFile(jsonContent)
                } else {
                    withContext(Dispatchers.Main) {
                        shareText(jsonContent)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onMessage("Export failed: ${e.message}")
                }
            }
        }
    }

    /** Serializes transactions to a JSONArray, visible for testing. */
    internal suspend fun serializeTransactions(transactions: List<NetworkTransaction>): JSONArray {
        val jsonArray = JSONArray()

        transactions.forEach { transaction ->
            val requestBody = transaction.request.bodyRef?.let { queryEngine?.getBody(it) }
            val responseBody = transaction.response?.bodyRef?.let { queryEngine?.getBody(it) }

            val jsonObject = JSONObject().apply {
                put("id", transaction.id.toString())
                put("timestamp", transaction.timestamp)
                put("status", transaction.status.name)
                put("durationMs", transaction.durationMs)
                put("request", serializeRequest(transaction.request, requestBody))
                transaction.response?.let { put("response", serializeResponse(it, responseBody)) }
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray
    }

    private fun shareText(content: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_SUBJECT, "WormaCeptor Transaction Export")
            }

            val chooser = Intent.createChooser(intent, "Export Transactions")
            if (context !is Activity) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            onMessage("Failed to share: ${e.message}")
        }
    }

    private suspend fun shareAsFile(content: String) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val cacheDir = File(context.cacheDir, "shared_bodies")
            cacheDir.mkdirs()
            val file = File(cacheDir, "wormaceptor_export_$timestamp.json")

            file.bufferedWriter().use { writer ->
                writer.write(content)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.wormaceptor.fileprovider",
                file,
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "WormaCeptor Transaction Export")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            withContext(Dispatchers.Main) {
                onMessage("Exporting as file (${formatBytes(content.length.toLong())})")
                val chooser = Intent.createChooser(intent, "Export Transactions")
                if (context !is Activity) {
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onMessage("Failed to export file: ${e.message}")
            }
        }
    }

    /** JSON serialization helpers. */
    companion object {

        private fun serializeRequest(
            request: Request,
            body: String?,
        ): JSONObject = JSONObject().apply {
            put("url", request.url)
            put("method", request.method)
            put("headers", JSONObject(request.headers.mapValues { it.value.joinToString(", ") }))
            put("bodySize", request.bodySize)
            if (body != null) put("body", parseJsonOrString(body))
        }

        private fun serializeResponse(
            response: Response,
            body: String?,
        ): JSONObject = JSONObject().apply {
            put("code", response.code)
            put("message", response.message)
            put("headers", JSONObject(response.headers.mapValues { it.value.joinToString(", ") }))
            put("bodySize", response.bodySize)
            if (body != null) put("body", parseJsonOrString(body))
            response.error?.let { put("error", it) }
            response.protocol?.let { put("protocol", it) }
            response.tlsVersion?.let { put("tlsVersion", it) }
        }

        /** Parses as JSON if valid, otherwise returns the raw string. Keeps export bodies nested. */
        internal fun parseJsonOrString(raw: String): Any {
            val trimmed = raw.trim()
            if (trimmed.startsWith("{")) {
                try {
                    return JSONObject(trimmed)
                } catch (_: Exception) {
                    // Not valid JSON object
                }
            }
            if (trimmed.startsWith("[")) {
                try {
                    return JSONArray(trimmed)
                } catch (_: Exception) {
                    // Not valid JSON array
                }
            }
            return raw
        }
    }
}

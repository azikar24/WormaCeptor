package com.azikar24.wormaceptor.feature.viewer.export

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.feature.viewer.ui.util.MAX_CLIPBOARD_SIZE
import com.azikar24.wormaceptor.feature.viewer.ui.util.formatBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportManager(private val context: Context) {

    suspend fun exportTransactions(transactions: List<NetworkTransaction>) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Preparing export...", Toast.LENGTH_SHORT).show()
        }

        withContext(Dispatchers.IO) {
            try {
                val jsonArray = JSONArray()

                transactions.forEach { transaction ->
                    // Fetch actual body content
                    val requestBody = transaction.request.bodyRef?.let { blobId ->
                        CoreHolder.queryEngine?.getBody(blobId)
                    }
                    val responseBody = transaction.response?.bodyRef?.let { blobId ->
                        CoreHolder.queryEngine?.getBody(blobId)
                    }

                    val jsonObject = JSONObject().apply {
                        put("id", transaction.id.toString())
                        put("timestamp", transaction.timestamp)
                        put("status", transaction.status.name)
                        put("durationMs", transaction.durationMs)

                        put(
                            "request",
                            JSONObject().apply {
                                put("url", transaction.request.url)
                                put("method", transaction.request.method)
                                put(
                                    "headers",
                                    JSONObject(transaction.request.headers.mapValues { it.value.joinToString(", ") }),
                                )
                                put("bodySize", transaction.request.bodySize)
                                if (requestBody != null) {
                                    put("body", requestBody)
                                }
                            },
                        )

                        transaction.response?.let { response ->
                            put(
                                "response",
                                JSONObject().apply {
                                    put("code", response.code)
                                    put("message", response.message)
                                    put(
                                        "headers",
                                        JSONObject(response.headers.mapValues { it.value.joinToString(", ") }),
                                    )
                                    put("bodySize", response.bodySize)
                                    if (responseBody != null) {
                                        put("body", responseBody)
                                    }
                                },
                            )
                        }
                    }
                    jsonArray.put(jsonObject)
                }

                val jsonContent = jsonArray.toString(2)

                if (jsonContent.length > MAX_CLIPBOARD_SIZE) {
                    // Content too large for Intent - share as file
                    shareAsFile(jsonContent)
                } else {
                    // Small enough - share as text
                    withContext(Dispatchers.Main) {
                        shareText(jsonContent)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun shareText(content: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_SUBJECT, "WormaCeptor Transaction Export")
            }

            context.startActivity(Intent.createChooser(intent, "Export Transactions"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun shareAsFile(content: String) {
        try {
            // Create temp file with timestamp - file I/O stays on IO thread
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val cacheDir = File(context.cacheDir, "shared_bodies")
            cacheDir.mkdirs()
            val file = File(cacheDir, "wormaceptor_export_$timestamp.json")

            // Write content using buffered writer for efficiency
            file.bufferedWriter().use { writer ->
                writer.write(content)
            }

            // Get content URI via FileProvider
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

            // Only UI operations on Main thread
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Exporting as file (${formatBytes(content.length.toLong())})",
                    Toast.LENGTH_SHORT,
                ).show()

                context.startActivity(Intent.createChooser(intent, "Export Transactions"))
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to export file: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

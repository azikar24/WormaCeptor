package com.azikar24.wormaceptor.feature.viewer.export

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ExportManager(private val context: Context) {

    suspend fun exportTransactions(transactions: List<NetworkTransaction>) {
        withContext(Dispatchers.IO) {
            try {
                val jsonArray = JSONArray()

                transactions.forEach { transaction ->
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
                                put("bodyRef", transaction.request.bodyRef)
                            },
                        )

                        transaction.response?.let { response ->
                            put(
                                "response",
                                JSONObject().apply {
                                    put("code", response.code)
                                    put(
                                        "headers",
                                        JSONObject(response.headers.mapValues { it.value.joinToString(", ") }),
                                    )
                                    put("bodyRef", response.bodyRef)
                                },
                            )
                        }
                    }
                    jsonArray.put(jsonObject)
                }

                val jsonContent = jsonArray.toString(2)

                withContext(Dispatchers.Main) {
                    shareText(jsonContent)
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
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_SUBJECT, "WormaCeptor Transaction Export")
            }

            context.startActivity(Intent.createChooser(intent, "Export Transactions"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

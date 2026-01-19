package com.azikar24.wormaceptor.feature.viewer.export

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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
                        
                        put("request", JSONObject().apply {
                            put("url", transaction.request.url)
                            put("method", transaction.request.method)
                            put("headers", JSONObject(transaction.request.headers.mapValues { it.value.joinToString(", ") }))
                            put("bodyRef", transaction.request.bodyRef)
                        })
                        
                        transaction.response?.let { response ->
                            put("response", JSONObject().apply {
                                put("code", response.code)
                                put("headers", JSONObject(response.headers.mapValues { it.value.joinToString(", ") }))
                                put("bodyRef", response.bodyRef)
                            })
                        }
                    }
                    jsonArray.put(jsonObject)
                }
                
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "wormaceptor_export_$timestamp.json"
                val file = File(context.cacheDir, fileName)
                file.writeText(jsonArray.toString(2))
                
                withContext(Dispatchers.Main) {
                    shareFile(file)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun shareFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            // Create chooser with both text and JSON options
            val jsonIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "WormaCeptor Transaction Export")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, file.readText())
                putExtra(Intent.EXTRA_SUBJECT, "WormaCeptor Transaction Export")
            }
            
            val chooser = Intent.createChooser(jsonIntent, "Export Transactions")
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(textIntent))
            
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
